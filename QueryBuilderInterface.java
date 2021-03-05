import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Queen
 * QueryBuilder Interface combining common methods.
 */
public interface QueryBuilderInterface {

	/**
	 * Reads query path and parses queries with helper method. 
	 * 
	 * @param path file-path to parse
	 * @param isExact boolean to check if exact search specified
	 * @throws IOException if path not found to read
	 */
	public default void parseQueries(Path path, boolean isExact) throws IOException {
		try (
				BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
			) 
		{ 
			String line = null;
			while ((line = reader.readLine()) != null) {
				parseQueries(line, isExact);
			}
		}
	}
	
	/**
	 * Helper method to avoid repeated code, stems queries and adds in query map to compare with search results
	 * 
	 * @param line from query text file
	 * @param isExact boolean check if exact search specified 
	 */
	public void parseQueries(String line, boolean isExact);
	
	/**
	 * Helper method to output stemmed queries and list of search results for query.
	 * 
	 * @param resultsPath takes in path to output to
	 * @throws IOException if path unable to output to
	 */
	public void output(Path resultsPath) throws IOException;
}
