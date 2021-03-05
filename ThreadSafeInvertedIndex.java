import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
/**
 * Creates thread-safe version of InvertedIndex Class.
 * @author Rhea Arora
 *
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/** The lock used to protect concurrent access */
	private final SimpleReadWriteLock lock;
	
	/** Constructor initializing lock object */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new SimpleReadWriteLock();
	}
	
	@Override
	public void add(String word, String path, int position) {
		lock.writeLock().lock();
		try {
			super.add(word, path, position);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex local) {
		lock.writeLock().lock();
		try {
			super.addAll(local);	
		}
		finally {
			lock.writeLock().unlock();
		}
		
	}
	
	@Override
	public List<SearchResult> exactSearch(Collection<String> stemmedQueries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(stemmedQueries);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public List<SearchResult> partialSearch(Collection<String> stemmedQueries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(stemmedQueries);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public void output(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.output(path);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public void wordCountOutput(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.wordCountOutput(path);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean containsWords(String word) {
		lock.readLock().lock();
		try {
			return super.containsWords(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean containsLocations(String word, String location) {
		lock.readLock().lock();
		try {
			return super.containsLocations(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean containsPositions(String word, String location, int position) {
		lock.readLock().lock();
		try {
			return super.containsPositions(word, location, position);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public int wordsSize() {
		lock.readLock().lock();
		try {
			return super.wordsSize();
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public int locationsSize(String word) {
		lock.readLock().lock();
		try {
			return super.locationsSize(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public int positionsSize(String word, String location) {
		lock.readLock().lock();
		try {
			return super.positionsSize(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Set<String> getWords() {
		lock.readLock().lock();
		try {
			return super.getWords();
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Set<String> getLocations(String word) {
		lock.readLock().lock();
		try {
			return super.getLocations(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Set<Integer> getPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.getPositions(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		}
		finally {
			lock.readLock().unlock();
		}
	}
}