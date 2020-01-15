import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that implements the channel used by headquarters and space explorers to
 * communicate.
 */
public class CommunicationChannel {

	public LinkedBlockingQueue<Message> fromSpaceExpToHQ;
	public LinkedBlockingQueue<Message> fromHQToSpaceExp;
	static long threadID = -1;
	private final ReentrantLock spaceExp;
	private final ReentrantLock headQ;

	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
		fromSpaceExpToHQ = new LinkedBlockingQueue<>();
		fromHQToSpaceExp = new LinkedBlockingQueue<>();
		spaceExp = new ReentrantLock();
		headQ = new ReentrantLock();
	}

	/**
	 * Puts a message on the space explorer channel (i.e., where space explorers
	 * write to and headquarters read from).
	 * 
	 * @param message message to be put on the channel
	 */
	public void putMessageSpaceExplorerChannel(Message message) {
		// put in fromSpaceExptoHQ
		try {
			fromSpaceExpToHQ.put(message);
		} catch (InterruptedException e) {

		}

	}

	/**
	 * Gets a message from the space explorer channel (i.e., where space explorers
	 * write to and headquarters read from).
	 * 
	 * @return message from the space explorer channel
	 */
	public Message getMessageSpaceExplorerChannel() {
		Message msg = null;
		try {
			msg = fromSpaceExpToHQ.take();
		} catch (InterruptedException e) {
		}
		return msg;

	}

	/**
	 * Puts a message on the headquarters channel (i.e., where headquarters write to
	 * and space explorers read from).
	 * 
	 * @param message message to be put on the channel
	 */
	public void putMessageHeadQuarterChannel(Message message) {
		//fiecare thread da lock 
		headQ.lock();
		if (!message.getData().equals(HeadQuarter.END) 
				&& !message.getData().equals(HeadQuarter.EXIT)) {
			try {
				fromHQToSpaceExp.put(message);
			} catch (InterruptedException e) {
			} finally {
				//nu s-a terminat lista deci eliberez ambele lockuri
				if (headQ.getHoldCount() == 2) {
					headQ.unlock();
					headQ.unlock();
				}
			}
		} else {
			try {
				fromHQToSpaceExp.put(message);
			} catch (InterruptedException e) {

			} finally {
				headQ.unlock();
			}

		}
	}

	/**
	 * Gets a message from the headquarters channel (i.e., where headquarters write
	 * to and space explorer read from).
	 * 
	 * @return message from the header quarter channel
	 */
	public Message getMessageHeadQuarterChannel() {
		Message msg = null;
		spaceExp.lock();
		try {
			msg = fromHQToSpaceExp.take();
		} catch (InterruptedException e) {
		}
		if (!msg.getData().equals(HeadQuarter.END) 
				&& !msg.getData().equals(HeadQuarter.EXIT)) {
			//nu s-a terminat lista deci eliberez ambele lockuri
			if (spaceExp.getHoldCount() == 2) {
				spaceExp.unlock();
				spaceExp.unlock();
			}

		} else {
			spaceExp.unlock();
		}
		return msg;
	}
}
