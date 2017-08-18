package jmstool.jms;

import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import jmstool.model.SimpleMessage;

/**
 * creates JMS message from {@link SimpleMessage}
 *
 */
public class SimpleMessageTextCreator implements MessageCreator {
	private final SimpleMessage message;

	private final SimpleMessageConverter messageConverter = new SimpleMessageConverter();

	SimpleMessageTextCreator(SimpleMessage message) {
		this.message = message;
	}

	@Override
	public Message createMessage(Session session) throws JMSException {
		Message jmsMessage = messageConverter.toMessage(message.getText(), session);
		for (Entry<String, String> e : message.getProps().entrySet()) {
			jmsMessage.setStringProperty(e.getKey(), e.getValue());
		}
		return jmsMessage;
	}
}