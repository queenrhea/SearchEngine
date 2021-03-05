import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 * 
 * Driver Project 3
 */
public class Driver {
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index, and extends exact/partial search for queries and search results, in a
	 * multithreaded and thread safe version.
	*/
	
	/** The logger to use */
	private static Logger log = LogManager.getLogger("Driver");
	
	/**
	 * @param args of flag and value pairs used for program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();
		// initializes Argument Map for flags & parsing the command line arguments into a flag and value
		ArgumentMap argumentMap = new ArgumentMap(args);	
		// inverted index object initialization to call methods
		InvertedIndex invertedIndex;
		// inverted index builder object initialization
		InvertedIndexBuilder invertedIndexBuilder;		
		// query class object initialization
		QueryBuilderInterface query;
		// work queue object to store incoming requests
		WorkQueue queue = null;
		// initialize number of worker threads
		int threads = 0;
		
		if (argumentMap.hasFlag("-threads")) {
			try {
				// returns number of threads specified, else 5 threads is given as default value
				threads = argumentMap.getInteger("-threads", 5);
			}
			// testing for fraction or non-integer values
			catch(NumberFormatException e) {
				log.error("Thread value must be an integer.");
			}
			// if thread value given is negative
			if(threads <= 0) {
				threads = 5;
			}	
			// pass in number of threads
			queue = new WorkQueue(threads);
			// calls thread safe classes & passes in thread safe inverted index
			ThreadSafeInvertedIndex threadSafe = new ThreadSafeInvertedIndex();
			invertedIndex = threadSafe;
			invertedIndexBuilder = new MultiThreadedIndexBuilder(threadSafe, queue);
			query = new MultiThreadedQueryBuilder(threadSafe, queue);
		}
		else {
			invertedIndex = new InvertedIndex();
			invertedIndexBuilder = new InvertedIndexBuilder(invertedIndex);
			query = new QueryBuilder(invertedIndex);
		}	
		
		if (argumentMap.hasFlag("-path")) {
			if (argumentMap.getPath("-path") == null) {
				return;
			}
			Path path = argumentMap.getPath("-path");
			try {
				invertedIndexBuilder.checkPath(path);
			} catch (IOException e) {
				System.out.println("Cannot build index with given path: " + path);
			}
		}
		
		if (argumentMap.hasFlag("-index")) {
			Path output = argumentMap.getPath("-index", Path.of("index.json"));
			try {
				invertedIndex.output(output);
			}
			catch (Exception e) {
				System.out.println("Cannot output with given path: " + output);
			}
		}
		
		if (argumentMap.hasFlag("-counts")) {
			Path countsPath = argumentMap.getPath("-counts", Path.of("counts.json"));
			try {
				invertedIndex.wordCountOutput(countsPath);
			}
			catch (Exception e) {
				System.out.println("Cannot output word count with given path: " + countsPath);
			}
		}
				
		Path queryPath = argumentMap.getPath("-queries");
		if (argumentMap.hasFlag("-queries")) {
			if (queryPath == null) {
				System.out.println("Missing query path!");
			}
			else {
				try {
					query.parseQueries(queryPath, argumentMap.hasFlag("-exact"));
				} catch (IOException e) {
					System.out.println("Cannot query given path: " + queryPath);
				}	
			}
		}
		
		if (argumentMap.hasFlag("-results")) {
			Path resultsPath = argumentMap.getPath("-results", Path.of("results.json"));
			try {
				query.output(resultsPath);
			} catch (IOException e) {
				System.out.println("Unable to write to path " + resultsPath);
			}
		}
		
		if(queue != null) {
			queue.shutdown();
		}
		
		// calculate time elapsed and output
		Duration elapsed = Duration.between(start, Instant.now());
		double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}