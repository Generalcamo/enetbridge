package enetbridge;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class TrackingList<T> extends AbstractList<T> {
	public TrackingList(List<T> parent) {
		this.parent = parent;

		added.addAll(parent);
	}

	@Override
	public T get(int index) {
		return parent.get(index);
	}

	@Override
	public int size() {
		return parent.size();
	}

	@Override
	public T set(int index, T element) {
		added.add(element);
		T ret = parent.set(index, element);
		if (ret != null) removed.add(ret);

		return ret;
	}

	@Override
	public void add(int index, T element) {
		parent.add(index, element);
		added.add(element);
	}

	@Override
	public T remove(int index) {
		T ret = parent.remove(index);
		if (ret != null) removed.add(ret);

		return ret;
	}

	public List<T> getAdded() {
		return added;
	}

	public List<T> getRemoved() {
		return removed;
	}

	private final List<T> parent;
	private final List<T> added = new ArrayList<T>();
	private final List<T> removed = new ArrayList<T>();
}
