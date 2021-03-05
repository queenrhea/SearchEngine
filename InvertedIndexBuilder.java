import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Builds in-memory inverted index.
 * @author Rhea Arora
 */
public class InvertedIndexBuilder {

	/** The default stemmer algorithm. */
	private static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/** Initialize inverted index class to implement in-memory index */
	private final InvertedIndex invertedIndex;

	/**
	 * Constructor to initialize invertedIndex class object
	 * 
	 * @param invertedIndex index from inverted index class
	 */
	public InvertedIndexBuilder(InvertedIndex invertedIndex) {
		this.invertedIndex = invertedIndex;
	}

	/**
	 * Check whether the given file-path from the user is a file or directory, and build the index based on a directory or file.
	 * 
	 * @param value -path flag value taken as input and checks if file or directory to traverse through
	 * @throws IOException if path not found
	 */
	public void checkPath(Path value) throws IOException {
		// if a directory, stems words in each file in directory and builds inverted index
		if (Files.isDirectory(value)) {
			for (Path file : TextFileFinder.list(value)) {
				buildIndex(file); 
			}
		}
		// if a file, stems words in the file and builds the inverted index
		else {
			buildIndex(value);
		}
	}

	/**
	 * Instance type method.
	 * 
	 * @param path to build index from
	 * @throws IOException if path not found
	 */
	public void buildIndex(Path path) throws IOException {
		buildIndex(path, this.invertedIndex);
	}
	
	
	/**
	 * Reads file, parses each line, and adds components (word, path, position) in index
	 * 
	 * @param path file-path to stem and add to inverted index
	 * @param invertedIndex add to index
	 * @throws IOException if path not found or cannot be read
	 */
	public static void buildIndex(Path path, InvertedIndex invertedIndex) throws IOException { 
		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		String file = path.toString();
		int position = 1;
		String line = null;

		try (
				BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
			) 
		{
			while ((line = reader.readLine()) != null) {
				for(String word : TextParser.parse(line)) {
					word = stemmer.stem(word).toString();
					invertedIndex.add(word, file, position);
					position++;
				}
			}
		}
	}
}