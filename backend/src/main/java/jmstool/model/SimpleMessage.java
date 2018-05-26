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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * JMS message with text and properties. Each message has unique ID
 *
 */
public final class SimpleMessage implements Serializable, Comparable<SimpleMessage> {
	private static final long serialVersionUID = 1L;
	private static final AtomicLong SEQ = new AtomicLong(1);

	@ApiModelProperty(notes = "Message ID")
	private final Long id = SEQ.getAndIncrement();
	
	@ApiModelProperty(notes = "Receive/Send time")
	private final transient LocalDateTime timestamp = LocalDateTime.now();
	
	@ApiModelProperty(notes = "Message text")
	private final String text;
	
	@ApiModelProperty(notes = "Message destination queue")
	private final String queue;

	@ApiModelProperty(notes = "Message properties")
	private final Map<String, String> props;

	public static SimpleMessage of(SimpleMessage message) {
		return new SimpleMessage(message.text, message.queue, new HashMap<>(message.props));
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

	public SimpleMessage(String text, String queue) {
		this(text, queue, Collections.<String, String>emptyMap());
	}

	public SimpleMessage(Path content, String queue) throws IOException {
		this(new String(Files.readAllBytes(content), Charset.forName("UTF-8")), queue);
	}

	@JsonCreator
	public SimpleMessage( //
			@JsonProperty("text") String text, //
			@JsonProperty("queue") String queue, //
			@JsonProperty("props") Map<String, String> props) {
		this.text = text;
		this.queue = queue;
		this.props = props;
	}

	public String getQueue() {
		return queue;
	}

	public Map<String, String> getProps() {
		return props;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, queue, text, props);
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

		return Objects.equals(this.id, other.id) && Objects.equals(this.text, other.text)
				&& Objects.equals(this.queue, other.queue) && Objects.equals(this.props, other.props);
	}

	@Override
	public int compareTo(SimpleMessage o) {
		return this.id.compareTo(o.id);
	}
}
