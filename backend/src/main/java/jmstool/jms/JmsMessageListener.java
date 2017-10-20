package jmstool.jms;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinezoo.noexception.Exceptions;

import jmstool.model.SimpleMessage;
import jmstool.storage.LocalMessageStorage;

/**
 * Message listener for single JMS queue. Each new message will be added to
 * {@link LocalMessageStorage}
 *
 */
public class JmsMessageListener implements MessageListener {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final LocalMessageStorage storage;

	private final String queue;

	private final List<String> propertiesToExtract;

	public JmsMessageListener(String queue, LocalMessageStorage storage, List<String> propertiesToExtract) {
		super();
		this.queue = queue;
		this.storage = storage;
		this.propertiesToExtract = propertiesToExtract;
	}

	@Override
	public void onMessage(Message msg) {
		logger.debug("received new message from queue '{}'", queue);
		TextMessage textMessage = (TextMessage) msg;
		SimpleMessage message = new SimpleMessage(Exceptions.sneak().get(() -> textMessage.getText()), queue,
				getMessageProperties(msg));
		storage.addMessage(message);
	}

	public Map<String, String> getMessageProperties(Message msg) {
		Map<String, String> msgProps = new HashMap<>();
		@SuppressWarnings("unchecked")
		Enumeration<String> propertyNames = Exceptions.sneak().get(() -> msg.getPropertyNames());

		Collections.list(propertyNames).stream().filter(p -> propertiesToExtract.contains(p))
				.forEach(p -> msgProps.put(p, Exceptions.sneak().get(() -> msg.getStringProperty(p))));
		return msgProps;
	}
}
