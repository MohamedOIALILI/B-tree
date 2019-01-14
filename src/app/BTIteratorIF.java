package app;

public interface BTIteratorIF <K extends Comparable, V> {
    public boolean item(K key, V value);
}
