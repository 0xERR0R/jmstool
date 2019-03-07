package jmstool.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jmstool.model.SimpleMessage;

/**
 * Storage for messages. Messages are sorted by id.
 *
 */
public class LocalMessageStorage {

	protected static final int MAX_COUNT = 500;

	private final SortedMap<Long, SimpleMessage> storage = new TreeMap<>();

	public synchronized Collection<SimpleMessage> getMessagesAfter(long id) {
		if (storage.isEmpty()) {
			return Collections.emptySet();
		}

		return storage.entrySet().stream().filter(e -> e.getKey() > id).map(e -> e.getValue())
				.collect(Collectors.toSet());
	}

	public synchronized void addMessage(SimpleMessage message) {
		storage.put(message.getId(), message);
		if (storage.size() > MAX_COUNT) {
			storage.remove(storage.firstKey());
		}
	}

	public synchronized void clear() {
		storage.clear();
	}

	public synchronized int size() {
		return storage.size();
	}

	public synchronized long getLastId() {
		return storage.keySet().isEmpty() ? 0 : Collections.max(storage.keySet());
	}
}
