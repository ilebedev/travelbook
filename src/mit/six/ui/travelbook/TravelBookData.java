package mit.six.ui.travelbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TravelBookData {
	ArrayList<Entry> entries;
	private static int counter = 1;

	static TravelBookData db = null;

	public static TravelBookData getDB() {
		if (null == db) {
			db = new TravelBookData();
		}
		return db;
	}

	private class timestampComparator implements Comparator<Entry> {
		public int compare(Entry arg0, Entry arg1) {
			return arg0.getTimestamp().compareTo(arg1.getTimestamp());
		}
	}

	private TravelBookData() {
		entries = new ArrayList<Entry>();
		//for (int i=0; i<20; i++){
		//	entries.add(new Entry());
		//}
	}

	public void add(Entry entry) {
		entry.setID(counter);
		counter += 1;

		entries.add(entry);
	}

	public void edit(Entry entry) {
		Entry old = getEntry(entry.getID());
		if (null != old) {
			entries.set(entries.indexOf(old), entry);
		}
	}
	
	public int get_ordering(Entry e){
		return entries.indexOf(e); 
	}
	
	public Entry get_by_ordering(int i){
		return entries.get(i);
	}

	public void delete(Entry entry) {
		entries.remove(entry);
		//for (Entry e : entries) {
		//	if (e.getID() == entry.getID()) {
		//		entries.remove(e);
		//	}
		//}
		
		// TODO: undo mechanism
		// remove e from entries
		// give e a "deleted" property to correctly display it
		// add e to undoable deletions
	}

	public Entry getEntry(int _id) {
		for (Entry e : entries) {

			if (e.getID() == _id) {
				return e;
			}
		}

		return null;
	}

	public Entry getEntry(long t) {
		for (Entry e : entries) {
			if (e.getTimestamp().getTime() == t) {
				return e;
			}
		}

		return null;
	}

	public ArrayList<Entry> getEntries() {
		// TODO: deletion mechanism
		// remove expired undoable deletions
		// report the union of undoable deletions and entries

		// sort by time..
		Collections.sort(entries, new timestampComparator());

		return entries;
	}

}
