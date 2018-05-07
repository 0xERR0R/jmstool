package jmstool.jms;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
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

	public static final String JMS_IBM_CHARACTER_SET = "JMS_IBM_Character_Set";

	private final LocalMessageStorage storage;

	private final String queue;

	private final List<String> propertiesToExtract;

	private final String encoding;

	public JmsMessageListener(String queue, LocalMessageStorage storage, List<String> propertiesToExtract,
			String encoding) {
		super();
		this.queue = queue;
		this.storage = storage;
		this.propertiesToExtract = propertiesToExtract;
		this.encoding = encoding;
	}

	@Override
	public void onMessage(Message msg) {
		logger.debug("received new message from queue '{}'", queue);
		String text = Exceptions.sneak().get(() -> extractTextFromMessage(msg));
		SimpleMessage message = new SimpleMessage(text, queue, getMessageProperties(msg));
		storage.addMessage(message);
	}

	private String extractTextFromMessage(Message msg) throws JMSException {
		String text = null;

		if (msg instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) msg;
			text = Exceptions.sneak().get(() -> textMessage.getText());
		} else if (msg instanceof BytesMessage) {
			BytesMessage bytesMessage = (BytesMessage) msg;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int n = 0;
			byte[] buf = new byte[(int) (bytesMessage.getBodyLength() > Integer.MAX_VALUE ? 1024
					: bytesMessage.getBodyLength())];
			while ((n = bytesMessage.readBytes(buf)) >= 0) {
				out.write(buf, 0, n);
			}
			byte[] bytes = out.toByteArray();

			text = new String(bytes, Charset.forName(encodingFromMessageOrDefault(bytesMessage)));
		} else {
			text = String.format("Unsupported message type: '%s'", msg.getClass().getName());
			logger.warn(text);
		}
		return text;
	}

	/**
	 * @see com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET
	 */
	private String encodingFromMessageOrDefault(BytesMessage bytesMessage) {
		String charset = Exceptions.log(logger)
				.get(Exceptions.sneak().supplier(() -> bytesMessage.getStringProperty(JMS_IBM_CHARACTER_SET)))
				.orElse(encoding);
		return charset;
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
