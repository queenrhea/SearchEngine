import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Multi-threaded version of QueryBuilder class that uses worker threads to handle individual queries.
 * @author Rhea Arora
 */
public class MultiThreadedQueryBuilder implements QueryBuilderInterface {
	
	/** Stores query and search results for comparison */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> queryMap;
	
	/** Accessing InvertedIndex methods through object initialization */
	private final ThreadSafeInvertedIndex index;

	/** Search inverted index from a file of multiple word queries */
	private final WorkQueue queue; 
	
	/**
	 * Constructor of MultiThreadedQueryBuilder Class.
	 * 
	 * @param index map from inverted index class
	 * @param queue storing requests and passed in from Driver
	 */
	public MultiThreadedQueryBuilder(ThreadSafeInvertedIndex index, WorkQueue queue) {
		this.index = index;
		this.queryMap = new TreeMap<>();
		this.queue = queue;
	}
	
	@Override
	public void parseQueries(Path path, boolean isExact) throws IOException {
		QueryBuilderInterface.super.parseQueries(path, isExact);
		queue.finish();
	}
	
	@Override
	public void parseQueries(String line, boolean isExact) {
		queue.execute(new Task(line, isExact));
	}
	
	/**
	 * Performs task of query parsing and specifies work in queue
	 */
	private class Task implements Runnable {
		/** Stemming each line from file */
		private String line;
		/** Boolean  to check for exact search or partial search */
		private boolean isExact;
		
		/**
		 * Constructor of private Task class.
		 * 
		 * @param line parse individual line
		 * @param isExact boolean to check exact or partial search
		 */
		public Task(String line, boolean isExact) {
			this.line = line;
			this.isExact = isExact;
		}
		
		@Override
		public void run() {
			// efficient & thread-safe version
			TreeSet<String> stemmedQueryLines = TextFileStemmer.uniqueStems(line);	
			String query = String.join(" ", stemmedQueryLines);
			// test for negative case 
			synchronized (queryMap) {
				if (query.isEmpty() || queryMap.containsKey(query)) {
					// if do not want to continue, return
					return;
				}
			}
			// get search results 
			List<InvertedIndex.SearchResult> output = index.outputSearchResults(stemmedQueryLines, isExact);
			synchronized(queryMap) { 
				// put search results in map
				queryMap.put(query, output);
			}
		}
	}

	@Override
	public void output(Path resultsPath) throws IOException {
		SimpleJsonWriter.asQuery(queryMap, resultsPath);
	}
}