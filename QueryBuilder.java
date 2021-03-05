import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
/**
 * Reads queries from text file, stems queries, and output list of search results for query.
 * @author Rhea Arora
 */
public class QueryBuilder implements QueryBuilderInterface { 
	
	/** Stores query and results for comparison */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> queryMap;
	
	/** Accessing InvertedIndex methods through object initialization */
	private final InvertedIndex index;
	
	/**
	 * Constructor initializing queryMap and index. 
	 * 
	 * @param index map from inverted index class
	 */
	public QueryBuilder(InvertedIndex index) {
		this.queryMap = new TreeMap<>();
		this.index = index;
	}
	/**
	 * Reads query path and parses queries with helper method. 
	 * 
	 * @param path file-path to parse
	 * @param isExact boolean to check if exact search specified
	 * @throws IOException if path not found to read
	 */
	public void parseQueries(Path path, boolean isExact) throws IOException {
		QueryBuilderInterface.super.parseQueries(path, isExact);
	}
	
	/**
	 * Helper method to avoid repeated code, stems queries and adds in query map to compare with search results
	 * 
	 * @param line from query text file
	 * @param isExact boolean check if exact search specified 
	 */
	public void parseQueries(String line, boolean isExact) {
		TreeSet<String> stemmedQueryLines = TextFileStemmer.uniqueStems(line);
		String query = String.join(" ", stemmedQueryLines);
		if(!stemmedQueryLines.isEmpty() && (!queryMap.containsKey(query))) { 
			queryMap.put(query, index.outputSearchResults(stemmedQueryLines, isExact));
		}
	}
	
	/**
	 * Helper method to output stemmed queries and list of search results for query.
	 * 
	 * @param resultsPath takes in path to output to
	 * @throws IOException if path unable to output to
	 */
	public void output(Path resultsPath) throws IOException {
		SimpleJsonWriter.asQuery(queryMap, resultsPath);
	}
}