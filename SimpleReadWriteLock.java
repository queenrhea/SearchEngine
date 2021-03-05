import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Maintains a pair of associated locks, one for read-only operations and one
 * for writing. The read lock may be held simultaneously by multiple reader
 * threads, so long as there are no writers. The write lock is exclusive.
 *
 * @see SimpleLock
 *
 * @see Lock
 * @see ReadWriteLock
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2020
 */
public class SimpleReadWriteLock {

	/** The lock used for reading. */
	private final SimpleLock readerLock;

	/** The lock used for writing. */
	private final SimpleLock writerLock;

	/** The number of active readers. */
	private int readers;

	/** The number of active writers; */
	private int writers;

	/**
	 * The lock object used for synchronized access of readers and writers. For
	 * security reasons, a separate private final lock object is used.
	 *
	 * @see <a href=
	 *      "https://wiki.sei.cmu.edu/confluence/display/java/LCK00-J.+Use+private+final+lock+objects+to+synchronize+classes+that+may+interact+with+untrusted+code">
	 *      SEI CERT Oracle Coding Standard for Java</a>
	 */
	private Object lock;

	/**
	 * Initializes a new simple read/write lock.
	 */
	public SimpleReadWriteLock() {
		readerLock = new ReadLock();
		writerLock = new WriteLock();

		lock = new Object();

		readers = 0;
		writers = 0;
	}

	/**
	 * Returns the reader lock.
	 *
	 * @return the reader lock
	 */
	public SimpleLock readLock() {
		return readerLock;
	}

	/**
	 * Returns the writer lock.
	 *
	 * @return the writer lock
	 */
	public SimpleLock writeLock() {
		return writerLock;
	}

	/**
	 * Returns the number of active readers.
	 *
	 * @return the number of active readers
	 */
	public int readers() {
		synchronized (lock) {
			return readers;
		}
	}

	/**
	 * Returns the number of active writers.
	 *
	 * @return the number of active writers
	 */
	public int writers() {
		synchronized (lock) {
			return writers;
		}
	}

	/**
	 * Determines whether the thread running this code and the other thread are in
	 * fact the same thread.
	 *
	 * @param other the other thread to compare
	 * @return true if the thread running this code and the other thread are not
	 *         null and have the same ID
	 *
	 * @see Thread#getId()
	 * @see Thread#currentThread()
	 */
	public static boolean sameThread(Thread other) {
		return other != null && other.getId() == Thread.currentThread().getId();
	}

	/**
	 * Used to maintain simultaneous read operations.
	 */
	private class ReadLock implements SimpleLock {

		/**
		 * Waits until there are no active writers in the system, then increases the
		 * number of active readers.
		 */
		@Override
		public void lock() {
			synchronized (lock) {
				while (writers > 0) {
					try {
						lock.wait();
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}

				assert writers == 0;
				readers++;
			}
		}

		/**
		 * Decreases the number of active readers and notifies any waiting threads if
		 * necessary.
		 *
		 * @throws IllegalStateException if no readers to unlock
		 */
		@Override
		public void unlock() throws IllegalStateException {
			synchronized (lock) {
				try {
					readers--;
					if (readers <= 0) {
						lock.notifyAll();
					}
				}
				catch(IllegalStateException e) {
					throw new IllegalStateException();
				}
			}
		}
	}

	/**
	 * Used to maintain exclusive write operations.
	 */
	private class WriteLock implements SimpleLock {
		/**
		 * member to track which thread holds the write lock
		 */
		Thread writer;

		/**
		 * Waits until there are no active readers or writers in the system. Then,
		 * increases the number of active writers and tracks which thread holds the
		 * write lock currently.
		 */
		@Override
		public void lock() {
			synchronized (lock) {
				while (writers != 0 || readers != 0) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				writer = Thread.currentThread();
				writers++;
			}
		}

		/**
		 * Decreases the number of active writers, resets the thread that holds the
		 * write lock, and notifies any waiting threads if necessary.
		 *
		 * @throws IllegalStateException           if no writers to unlock
		 *
		 * @throws ConcurrentModificationException if unlock is called by a thread that
		 *                                         does not hold the write lock
		 */
		@Override
		public void unlock() throws IllegalStateException, ConcurrentModificationException {
			synchronized (lock) {
				if (writers <= 0) {
					throw new IllegalStateException();
				}

				if (!sameThread(writer)) {
					throw new ConcurrentModificationException();
				}

				assert writers == 1;
				writers--;
				writer = null;
				lock.notifyAll();
			}
		}
	}
}