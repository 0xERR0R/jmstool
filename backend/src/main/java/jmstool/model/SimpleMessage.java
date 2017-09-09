package jmstool.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

/**
 * JMS message with text and properties. Each message hat unique ID
 *
 */
public class SimpleMessage implements Serializable, Comparable<SimpleMessage> {
	private static final long serialVersionUID = 1L;
	private static final AtomicLong SEQ = new AtomicLong(1);

	private final Long id = SEQ.getAndIncrement();
	private final LocalDateTime timestamp = LocalDateTime.now();
	private String text;
	private String queue;

	private Map<String, String> props = new HashMap<String, String>();

	public static SimpleMessage of(SimpleMessage message) {
		SimpleMessage copy = new SimpleMessage();
		copy.props = new HashMap<>(message.props);
		copy.text = message.text;
		copy.queue = message.queue;
		return copy;
	}

	@DateTimeFormat(iso = ISO.DATE_TIME)
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getText() {
		return text;
	}

	public long getId() {
		return id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public SimpleMessage() {
	}

	public SimpleMessage(String text, String queue) {
		this(text, queue, Collections.<String, String>emptyMap());
	}
	
	public SimpleMessage(Path content, String queue) throws IOException {
			byte[] raw = Files.readAllBytes(content);
			String message = new String(raw, Charset.forName("UTF-8"));
			this.text = message;
			this.queue = queue;
	}

	public SimpleMessage(String text, String queue, Map<String, String> props) {
		this.text = text;
		this.queue = queue;
		this.props = props;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public Map<String, String> getProps() {
		return props;
	}

	public void setProps(Map<String, String> props) {
		this.props = props;
	}

	@Override
	public int hashCode() {
		return Objects.hash(queue, text, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SimpleMessage other = (SimpleMessage) obj;

		return Objects.equals(this.id, other.id) && Objects.equals(this.timestamp, other.timestamp)
				&& Objects.equals(this.text, other.text) && Objects.equals(this.queue, other.queue);
	}

	@Override
	public int compareTo(SimpleMessage o) {
		return this.id.compareTo(o.id);
	}

}
