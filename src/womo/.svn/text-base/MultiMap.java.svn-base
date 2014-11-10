package womo;

import java.util.*;

/**
 * A Map that can contain several entries for the same key. Will store Data in 
 * a HashMap<K, ArrayList<V>>. So there is an ordering for multiple entries of one key!
 * Methods like "entrySet" or "values" are not fast. The method "size" returns the value-count, 
 * not the key-count. The methods "get" and "remove" return the first entry for the key only. 
 * To get all entries for a key use "getList".
 * This implementation is optimized for 1 to 3 entries per key.
 * @author Florian Jostock
 *
 */
public class MultiMap<K,V> implements Map<K,V> {

	private HashMap<K, ArrayList<V>> contents = new HashMap<K, ArrayList<V>>();
	
	public void clear() {
		contents.clear();
	}

	public boolean containsKey(Object key) {
		return contents.containsKey(key);
	}

	public boolean containsValue(Object val) {
		return this.values().contains(val);
	}

	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> set = new HashSet<Map.Entry<K, V>>();
		for (K key : this.keySet()){
			for (V val: this.getList(key)){
				Map.Entry<K, V> entry = new MultiMap.Entry<K, V>(key, val);
				set.add(entry);
			}
		}
		return set;
	}

	/**
	 * returns the first entry for this key or null
	 */
	public V get(Object key) {
		if (contents.get(key) == null) return null;
		return contents.get(key).get(0);
	}
	
	/**
	 * return a List of all entries that were saved with this key. They are returned
	 * in the same Order they were added. If there are nor entries, a list with
	 * length 0 is returned, no null.
	 * @param key Key for wich we search entries.
	 * @return List of all entries that were added with this key.
	 */
	public List<V> getList(Object key){
		List<V> l = contents.get(key);
		if (l == null){l = new ArrayList<V>(0);}
		return l;
	}

	public boolean isEmpty() {
		return contents.isEmpty();
	}

	public Set<K> keySet() {
		return contents.keySet();
	}

	public V put(K key, V val) {
		if (contents.get(key) == null) {
			ArrayList<V> list = new ArrayList<V>(3);
			list.add(val);
			contents.put(key, list);
		} else {
			contents.get(key).add(val);
		}
		return val;
	}

	public void putAll(Map<? extends K,? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()){
			this.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * removes all entries for this key and returns the first
	 * entry for this key (or null).
	 */
	public V remove(Object key) {
		V val = this.get(key);
		contents.remove(key);
		return val;
	}

	/**
	 * returns the value-count, not the key-count, this 
	 * is the same value as values().size() would return, but faster.
	 */
	public int size() {
		int size = 0;
		for (ArrayList<V> vals : contents.values()){
			size += vals.size();
		}	
		return size;
	}

	public Collection<V> values() {
		Collection<V> coll = new ArrayList<V>(contents.size()*2);
		for (ArrayList<V> vals : contents.values()){
			coll.addAll(vals);
		}
		return coll;
	}
	
	private static class Entry<KE, VE> implements Map.Entry<KE, VE>{
		
		private KE key;
		private VE val;
		
		protected Entry(KE key, VE val){
			this.key = key;
			this.val = val;
		}

		public KE getKey() {
			return key;
		}

		public VE getValue() {
			return val;
		}

		public VE setValue(VE value) {
			return val = value;
		}
		
	}

}
