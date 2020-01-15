import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.nio.charset.StandardCharsets;

/**
 * Class for a space explorer.
 */
public class SpaceExplorer extends Thread {

	private Integer hashCount;
	public static Set<Integer> discoveredNodes;
	private CommunicationChannel channel;
	private AtomicInteger currentSolarSys;
	private boolean isRoot = false;

	/**
	 * Creates a {@code SpaceExplorer} object.
	 * 
	 * @param hashCount  number of times that a space explorer repeats the hash
	 *                   operation when decoding
	 * @param discovered set containing the IDs of the discovered solar systems
	 * @param channel    communication channel between the space explorers and the
	 *                   headquarters
	 */
	public SpaceExplorer(Integer hashCount2, Set<Integer> discovered, CommunicationChannel channel) {
		hashCount = hashCount2;
		discoveredNodes = discovered;
		this.channel = channel;
	}

	@Override
	public void run() {
		while (true) {
			//current solar system
			Message discoveredN = channel.getMessageHeadQuarterChannel();
			if (!discoveredN.equals(null) 
					&& !discoveredN.getData().equals(HeadQuarter.END)) {
				if (discoveredN.getData().equals(HeadQuarter.EXIT)) {
					return;
				}
				//vecin
				Message adj = channel.getMessageHeadQuarterChannel();
				Integer adjN = adj.getCurrentSolarSystem();
				Integer curentSys = discoveredN.getCurrentSolarSystem();
				if (!discoveredNodes.contains(adjN)) {
					//il prelucrez doar daca nu fost prelucrat deja
					String decoded = encryptMultipleTimes(adj.getData(), hashCount);
					Message nextHop = new Message(curentSys, adjN, decoded);
					channel.putMessageSpaceExplorerChannel(nextHop);
					//la final il adaug in lista de noduri prelucrate
					discoveredNodes.add(adjN);
				}

			}
		}

	}

	/**
	 * Applies a hash function to a string for a given number of times (i.e.,
	 * decodes a frequency).
	 * 
	 * @param input string to he hashed multiple times
	 * @param count number of times that the string is hashed
	 * @return hashed string (i.e., decoded frequency)
	 */
	private String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;
		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}

		return hashed;
	}

	/**
	 * Applies a hash function to a string (to be used multiple times when decoding
	 * a frequency).
	 * 
	 * @param input string to be hashed
	 * @return hashed string
	 */
	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			// convert to string
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
