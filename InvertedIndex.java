import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
/**
 * @author Rhea Arora
 * InvertedIndex Project 2
 */
public class InvertedIndex {
	
	/**
	 * Stores word, file path, and position/location.
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;
	
	/**
	 * Stores file-path and count of words in the path
	 */
	private final TreeMap<String, Integer> wordCount;
	
	/**
	 * Initializes index as tree-map
	 */
	public InvertedIndex() {
		this.index = new TreeMap<>();
		this.wordCount = new TreeMap<>();
	}
	
	/**
	 * Stores a word, file path, and location into an inverted index data structure.
	 * 
	 * @param word the word stored in map
	 * @param path the file path where word is found
	 * @param position the position of word in file path
	 */
	public void add(String word, String path, int position) {
		index.putIfAbsent(word, new TreeMap<>());
		index.get(word).putIfAbsent(path, new TreeSet<>());
		if(index.get(word).get(path).add(position)) {
			wordCount.put(path, position);
		}
	}
	
	/**
	 * Merge the local index to current index
	 * 
	 * @param local inverted index stored from multi-threaded index builder
	 */
	public void addAll(InvertedIndex local) {
		for(String word : local.index.keySet()) {
			if(!this.index.containsKey(word)) {
				this.index.put(word, local.index.get(word));
			}
			else {
				for(String path : local.index.get(word).keySet()) {
					if(!this.index.get(word).containsKey(path)) {
						this.index.get(word).put(path, local.index.get(word).get(path));	
					}
					else {
						this.index.get(word).get(path).addAll(local.index.get(word).get(path));
					}
				}
			}
		}
		
		for(String location : local.wordCount.keySet()) {
			if(!this.wordCount.containsKey(location)) {
				this.wordCount.put(location, local.wordCount.get(location));
			}
			else {
				if(this.wordCount.get(location) < local.wordCount.get(location)) {
					this.wordCount.put(location, local.wordCount.get(location));
				}
			}
		}
	}

	
	/**
	 * Match index words to query words
	 * 
	 * @param stemmedQueries set of stemmed queries from Query class
	 * @return number of matches of index to query words
	 */
	public List<SearchResult> exactSearch(Collection<String> stemmedQueries) {
		ArrayList<SearchResult> resultList = new ArrayList<>();
		HashMap<String, SearchResult> lookup = new HashMap<>();
		for(String query : stemmedQueries) {
			if(index.containsKey(query)) {
				calcSearchResults(lookup, resultList, query);
			}
		}
		// sorted list of search results
		Collections.sort(resultList);
		return resultList;
	}
	
	/**
	 * Match if word stem starts with query word
	 * 
	 * @param stemmedQueries set of stemmed queries from Query class
	 * @return number of matches of index to query words
	 */
	public List<SearchResult> partialSearch(Collection<String> stemmedQueries) {
		ArrayList<SearchResult> list = new ArrayList<>();
		HashMap<String, SearchResult> lookup = new HashMap<>();
		for(String query : stemmedQueries) {
			for(String word : index.tailMap(query).keySet()) {
				if(!word.startsWith(query)) {
					break;
				}
				calcSearchResults(lookup, list, word);
			}
		}
		// sorted list of search results
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Helper method, takes out repeated code, to calculate score and create search result
	 * 
	 * @param lookup Store Search Results by location
	 * @param resultList stores individual search results
	 * @param query word
	 */
	private void calcSearchResults(HashMap<String, SearchResult> lookup, ArrayList<SearchResult> resultList, String query) {
		for (String location : index.get(query).keySet()) {
			if(!lookup.containsKey(location)) {
				SearchResult searchResult = new SearchResult(location);
				lookup.put(location, searchResult);
				resultList.add(searchResult);
			}
			
			lookup.get(location).update(query);
		}
	}
	
	/**
	 * Convenience method that executes exact or partial search to return sorted list of matches
	 * 
	 * @param stemmedQueries stemmed queries
	 * @param isExact boolean to check if user specifies exact search
	 * @return executes exact or partial search depending on flag specified and returns matches
	 */
	public List<SearchResult> outputSearchResults(Collection<String> stemmedQueries, boolean isExact) {
		if (isExact) {
			return exactSearch(stemmedQueries);
		}
		else {
			return partialSearch(stemmedQueries);
		}
	}
	
	/**
	 * Output inverted index
	 * 
	 * @param path output file 
	 * @throws IOException if path not found
	 */
	public void output(Path path) throws IOException {
		SimpleJsonWriter.asInvertedIndex(index, path);
	}
	
	/**
	 * Output word count
	 * 
	 * @param path output file 
	 * @throws IOException if path is not found and cannot be output to
	 */
	public void wordCountOutput(Path path) throws IOException {
		SimpleJsonWriter.asObject(wordCount, path);
	}


	/**
	 * Checks if index contains given word
	 * 
	 * @param word of index
	 * @return if index contains word
	 */
	public boolean containsWords(String word) {
		return index.containsKey(word);
	}

	/**
	 * Checks if index contains file location for given word
	 * 
	 * @param word of index
	 * @param location file-path of index
	 * @return if index contains location
	 */
	public boolean containsLocations(String word, String location) {
		if (containsWords(word)) {
			return index.get(word).containsKey(location);
		}
		return false;

	}

	/**
	 * Checks if index contains position of given word from file-path
	 * 
	 * @param word of index
	 * @param location file-path of index
	 * @param position of word in index
	 * @return if index contains position
	 */
	public boolean containsPositions(String word, String location, int position) {
		if (containsLocations(word, location)) {
			return index.get(word).get(location).contains(position);
		}
		return false;
	}

	/**
	 * Number of words stored
	 * 
	 * @return size of words
	 */
	public int wordsSize() {
		return getWords().size();
	}

	/**
	 * Number of file-paths
	 * 
	 * @param word of index
	 * @return size of locations
	 */
	public int locationsSize(String word) {
		return getLocations(word).size();
	}

	/**
	 * Number of positions for words
	 * 
	 * @param word of index
	 * @param location file-path of index
	 * @return size of positions
	 */
	public int positionsSize(String word, String location) {
		return getPositions(word, location).size();
	}

	/**
	 * Gets words in the set
	 * 
	 * @return keys of tree-map
	 */
	public Set<String> getWords() {
		return Collections.unmodifiableSet(index.keySet());
	}

	/**
	 * Gets file-path of word
	 * 
	 * @param word find location of word
	 * @return if the word exists, return safely the inner keyset, otherwise return
	 *         empty set
	 */
	public Set<String> getLocations(String word) {
		if (index.containsKey(word)) {
			return Collections.unmodifiableSet(index.get(word).keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * Gets position of word in file
	 * 
	 * @param word     check if word in index
	 * @param location check if file-path in index
	 * @return position of word in location
	 */
	public Set<Integer> getPositions(String word, String location) {
		if (index.containsKey(word)) {
			if (index.get(word).containsKey(location)) {
				return Collections.unmodifiableSet(index.get(word).get(location));
			}
		}
		return Collections.emptySet();
	}
	
	/**
	 * Overriding toString()
	 * 
	 * @return index as a string
	 */
	@Override
	public String toString() {
		return index.toString();
	}
	
	/**
	 * @author Rhea Arora
	 * Makes a SearchResult storing the location, number of words, matches to query, and score
	 */
	public class SearchResult implements Comparable<SearchResult> { 
		
		/** File-path location where word is found */
		private final String location;
		
		/** Matches of queries to words in index map */
		private int matches;
		
		/** Score calculated by matches divided by word count */
		private double score;

		/** Initializes SearchResults Class
		 * 
		 * @param location file-path location initialization
		 */
		public SearchResult(String location) {
			this.location = location;
			this.matches = 0;
			this.score = 0;
		}

		/**
		 * Helper method, gets location.
		 *
		 * @return a String - the location
		 */
		public String getLocation() {
			return this.location;
		}
		
		/**
		 * Helper method, gets number of matches.
		 * 
		 * @return an Integer - matches
		 */
		public Integer getMatches() {
			return this.matches;
		}
		
		/**
		 * Helper method, gets the score as a double
		 *
		 * @return double - the score
		 */
		public double getScore() {
			return this.score;
		}

		/**
		 * Helper method, gets the score as a string for SimpleJsonWriter
		 *
		 * @return String - the score
		 */
		public String getScoreString() {
			DecimalFormat score_formatter = new DecimalFormat("0.00000000");
			return score_formatter.format(this.score);
		}
		
		/**
		 * Update the matches and score if duplicates found in file path
		 * 
		 * @param query word from index
		 */
		private void update(String query) {
			this.matches += index.get(query).get(location).size(); 
			this.score = (double) matches / wordCount.get(location);
		}
		
		@Override
		public int compareTo(SearchResult search) {
			if(Double.compare(search.getScore(), this.score) == 0) {
				if(Integer.compare(search.matches, this.matches) == 0) {
					return location.compareToIgnoreCase(search.getLocation());
				}
				else {
					return Integer.compare(search.matches, this.matches);
				}
			}
			else {
				return Double.compare(search.getScore(), this.score);
			}	
		}
	}
}