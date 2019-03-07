package jmstool.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class MessageResult implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "list of messages")
	private final List<SimpleMessage> messages;

	@ApiModelProperty(notes = "Id of last message")
	private final Long lastId;

	@JsonCreator
	public MessageResult(@JsonProperty("messages") List<SimpleMessage> messages, @JsonProperty("lastId") Long lastId) {
		this.messages = messages;
		this.lastId = lastId;
	}

	public List<SimpleMessage> getMessages() {
		return messages;
	}

	public Long getLastId() {
		return lastId;
	}
	

}
