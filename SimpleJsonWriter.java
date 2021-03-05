import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using tabs.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2020
 */
public class SimpleJsonWriter {

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {
		Iterator<Integer> iterator = elements.iterator();

		writer.write("[");

		if (iterator.hasNext()) {
			// The first element is just a newline, tab, and the number itself.
			writer.write("\n");
			indent(iterator.next(), writer, level + 1);
		}
		while (iterator.hasNext()) {
			// indent using a tab character; specifies writer to use & level is # of times
			// to write the tab symbol
			writer.write(",");
			writer.write("\n");
			indent(iterator.next(), writer, level + 1);
		}

		writer.write("\n");
		indent(writer, level);
		writer.write("]");

	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {
		writer.write("{");

		// Cited/Source from Geeks for Geeks
		Iterator<Map.Entry<String, Integer>> iterator = elements.entrySet().iterator(); // using entrySet to retrieve
																						// both keys and values

		if (iterator.hasNext()) {
			// newline, tab, and number
			Map.Entry<String, Integer> entry = iterator.next();
			writer.write("\n");
			indent(entry.getKey(), writer, level + 1);
			writer.write(": " + entry.getValue());
		}

		while (iterator.hasNext()) {
			writer.write(",");
			writer.write("\n");

			Map.Entry<String, Integer> entry = iterator.next();

			String key = entry.getKey();
			indent(key, writer, level + 1);
			writer.write(": " + entry.getValue());
		}
		writer.write("\n}");

	}

	/**
	 * Writes the elements as a pretty JSON object with a nested array. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {

		writer.write("{");

		// Iterator<Entry<String, HashSet<Integer>>> iterator --> var for simplicity
		var iterator = elements.entrySet().iterator();

		if (iterator.hasNext()) {
			var entry = iterator.next();
			String key = entry.getKey();
			writer.write("\n");
			indent(key, writer, level + 1);
			writer.write(": ");
			asArray(entry.getValue(), writer, level + 1);
		}

		while (iterator.hasNext()) {
			// Map.Entry<String, HashSet<Integer>> entry replaced with "var" keyword

			writer.write(",");
			writer.write("\n");
			var entry = iterator.next();
			String key = entry.getKey();
			indent(key, writer, level + 1);
			writer.write(": ");

			asArray(entry.getValue(), writer, level + 1);

		}
		indent(writer, level);
		writer.write("\n}");

	}

	/**
	 * @param elements Inverted Index Data Structure
	 * @param path     filepath
	 * @throws IOException file
	 */
	public static void asInvertedIndex(Map<String, ? extends Map<String, ? extends Collection<Integer>>> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asInvertedIndex(elements, writer, 0);
		}
	}

	/**
	 * @param elements InvertedInde
	 * @param writer   writer object
	 * @param level    indent
	 * @throws IOException file
	 */
	public static void asInvertedIndex(Map<String, ? extends Map<String, ? extends Collection<Integer>>> elements,
			Writer writer, int level) throws IOException {
		writer.write("{");

		var iterator = elements.entrySet().iterator();

		if (iterator.hasNext()) {
			writer.write("\n");
			var entry = iterator.next();

			String key = entry.getKey();
			indent(key, writer, level + 1);
			writer.write(": ");

			asNestedArray(entry.getValue(), writer, level + 1);
		}

		while (iterator.hasNext()) {
			writer.write(",");
			writer.write("\n");

			var entry = iterator.next();

			String key = entry.getKey();
			indent(key, writer, level + 1);
			writer.write(": ");

			asNestedArray(entry.getValue(), writer, level + 1);

		}
		indent(writer, level);
		writer.write("\n");
		writer.write("}");
	}

	
	/**
	 * Write queries to path.
	 * 
	 * @param queries set of stemmed words
	 * @param path     filepath
	 * @throws IOException file error to write to
	 */
	public static void asQuery(Map<String, ? extends Collection<InvertedIndex.SearchResult>> queries, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			queryOutput(queries, writer, 0);
		}
	}

	/**
	 * Outer layer of query output.
	 *
	 * @param queries each query
	 * @param writer writer to use
	 * @param level indent level
	 * @throws IOException IOException
	 */
	public static void queryOutput(Map<String, ? extends Collection<InvertedIndex.SearchResult>> queries, Writer writer, int level) throws IOException {
			var iterator = queries.keySet().iterator();
			writer.write("{\n");

			if(iterator.hasNext()) {
				String query = iterator.next();
				queryArrayOutput(queries, query, writer, level);
			}
			
			while(iterator.hasNext()) {
				String query = iterator.next();
				writer.write(",\n");
				queryArrayOutput(queries, query, writer, level);
			}
			writer.write("\n}");
		}

	
	/**
	 * Inner layer of query output, including where, count, and score.
	 * 
	 * @param queries stemmed queries map
	 * @param query word 
	 * @param writer writer to use
	 * @param level indent
	 * @throws IOException file error unable to read
	 */
	public static void queryArrayOutput(Map<String, ? extends Collection<InvertedIndex.SearchResult>> queries, String query, Writer writer, int level) throws IOException {
		
		indent(query, writer, level+1);
		writer.write(": [");
		
		Iterator<InvertedIndex.SearchResult> search_iterator = queries.get(query).iterator();
		
		if (search_iterator.hasNext()) {
			writer.write("\n");
			outputQueryInfo(search_iterator.next(), writer, level);
			writer.write("}");
		}
		while(search_iterator.hasNext()) { 
			writer.write(",");
			writer.write("\n");
			outputQueryInfo(search_iterator.next(), writer, level);
			writer.write("}");
		}
		writer.write("\n");
		indent(writer, level+1);
		writer.write("]");
	}
	/**
	 * Helper method to output location, count, and score, and takes care of repeated code.
	 * 
	 * @param result Search result object
	 * @param writer writer to use
	 * @param level initial indent
	 * @throws IOException file unable to read
	 */
	public static void outputQueryInfo(InvertedIndex.SearchResult result, Writer writer, int level) throws IOException {
		indent(writer, level+2);
		writer.write("{\n");
		
		indent("where", writer, level+3);
		writer.write(": ");
		indent(result.getLocation(), writer, level);
	
		writer.write(",\n");
		
		indent("count", writer, level+3);
		writer.write(": " + result.getMatches());
		
		writer.write(",\n");
		
		indent("score", writer, level+3);
		writer.write(": " + result.getScoreString());
		
		writer.write("\n");
		indent(writer, level+2);
	}
	/**
	 * Indents using a tab character by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times  the number of times to write a tab symbol
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(Writer writer, int times) throws IOException {
		for (int i = 0; i < times; i++) {
			writer.write('\t');
		}
	}

	/**
	 * Indents and then writes the integer element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if an IO error occurs
	 *
	 * @see #indent(Writer, int)
	 */
	public static void indent(Integer element, Writer writer, int times) throws IOException {
		indent(writer, times);
		writer.write(element.toString());
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if an IO error occurs
	 *
	 * @see #indent(Writer, int)
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		indent(writer, times);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes a map entry in pretty JSON format.
	 *
	 * @param entry  the nested entry to write
	 * @param writer the writer to use
	 * @param level  the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeEntry(Entry<String, Integer> entry, Writer writer, int level) throws IOException {
		writer.write('\n');
		indent(entry.getKey(), writer, level);
		writer.write(": ");
		writer.write(entry.getValue().toString());
	}

	/*
	 * These methods are provided for you. No changes are required.
	 */

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<String, Integer> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<String, Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Path path)
			throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static String asNestedArray(Map<String, ? extends Collection<Integer>> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asNestedArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}
}
