import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Rhea Arora
 * Multi-Threaded version of building index.
 */
public class MultiThreadedIndexBuilder extends InvertedIndexBuilder {
	/** Initialize inverted index class to implement in-memory index */
	private final ThreadSafeInvertedIndex index; 
	
	/** Work queue object to save work's state */
	private final WorkQueue queue;
	
	/** The logger to use */
	private static Logger log = LogManager.getLogger("MultiThreadedIndexBuilder");
	
	/**
	 * Constructor that takes in thread safe index and number of threads; defines work queue.
	 * 
	 * @param index thread safe version of index class
	 * @param queue storing requests and passed in from Driver
	 */
	public MultiThreadedIndexBuilder(ThreadSafeInvertedIndex index,  WorkQueue queue) {
		super(index);
		this.index = index;
		this.queue = queue;
	}

	@Override
	public void checkPath(Path value) throws IOException {
		super.checkPath(value);
		queue.finish();
	}
	
	@Override
	public void buildIndex(Path path) throws IOException {
		 queue.execute(new Task(path));
	}
	
	/**
	 * @author Rhea Arora
	 * Additional class to specify work in queue for main thread 
	 */
	private class Task implements Runnable {
		/** Path object */
		private Path path;

		/**
		 * Constructor of Task Class.
		 * 
		 * @param path file to build index from 
		 */
		public Task(Path path) {
			this.path = path;
		}
		
		@Override
		public void run() {
			try {
				// To prevent constant blocking and inefficiency, using local data of the index, then merging the shared data to the index is necessary.
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.buildIndex(path, local);
				index.addAll(local);
			} catch (IOException e) {
				log.error("Could not build index" + path);
			}		
		}
	}
}