import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Utility class for parsing and stemming text and text files into collections
 * of stemmed words.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2020
 *
 * @see TextParser
 */
public class TextFileStemmer {
	/** The default stemmer algorithm used by this class. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Returns a list of cleaned and stemmed words parsed from the provided line.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a list of cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static ArrayList<String> listStems(String line, Stemmer stemmer) {
		ArrayList<String> stemmed_list = new ArrayList<String>();
		stemLine(line, stemmer, stemmed_list);
		return stemmed_list;
	}

	/**
	 * Returns a list of cleaned and stemmed words parsed from the provided line
	 * using the default stemmer.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @return a list of cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see #DEFAULT
	 * @see #listStems(String, Stemmer)
	 */
	public static ArrayList<String> listStems(String line) {
		return listStems(line, new SnowballStemmer(DEFAULT));
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words,
	 * and then adds those words to a set.
	 *
	 * @param inputFile the input file to parse
	 * @return a sorted set of stems from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #uniqueStems(String)
	 * @see TextParser#parse(String)
	 */
	public static ArrayList<String> listStems(Path inputFile) throws IOException {
		ArrayList<String> set = new ArrayList<String>();
		fileParse(inputFile, set);
		return set;
		
	}

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed
	 * from the provided line using the default stemmer.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see #DEFAULT
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static TreeSet<String> uniqueStems(String line) {
		return uniqueStems(line, new SnowballStemmer(DEFAULT));
	}

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed
	 * from the provided line.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static TreeSet<String> uniqueStems(String line, Stemmer stemmer) {
		TreeSet<String> stemmed_set = new TreeSet<String>();
		stemLine(line, stemmer, stemmed_set);
		return stemmed_set;
	}
	

	/**
	 * @param line takes in each line to be parsed
	 * @param stemmer uses stemmer object to stem
	 * @param stems collection list or set to store stemmed words
	 */
	public static void stemLine(String line, Stemmer stemmer, Collection<String> stems) {
		String[] parsed = TextParser.parse(line);
		for(String word : parsed) {
			stems.add(stemmer.stem(word).toString());
		}
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words,
	 * and then adds those words to a set.
	 *
	 * @param inputFile the input file to parse
	 * @return a sorted set of stems from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #uniqueStems(String)
	 * @see TextParser#parse(String)
	 */
	public static TreeSet<String> uniqueStems(Path inputFile) throws IOException {
		TreeSet<String> set = new TreeSet<String>();
		fileParse(inputFile, set);
		return set;
	}
	
	/**
	 * Helper method that creates buffered reader, parses through file with stemmer object, and stems each line
	 * 
	 * @param inputFile file from user
	 * @param set add parsed words
	 * @throws IOException file error
	 */
	public static void fileParse(Path inputFile, Collection<String> set) throws IOException {	
		try (
				BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);
			) 
		{
			// stemmer object
			Stemmer stemmer = new SnowballStemmer(DEFAULT);
			String line = null;
			// only 1 line "in memory" at a time
			while ((line = reader.readLine()) != null) {
				stemLine(line, stemmer, set);	
			}
		}	
	}
}