package jmstool.jms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collection;
import java.util.Collections;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;

import com.mockrunner.mock.jms.MockTextMessage;

import jmstool.model.SimpleMessage;
import jmstool.storage.LocalMessageStorage;

public class JmsMessageListenerTest {

	private JmsMessageListener sut;
	private LocalMessageStorage storage;

	@Before
	public void setUp() {
		storage = new LocalMessageStorage();
		sut = new JmsMessageListener("myQueue", storage, Collections.singletonList("propA"));
	}

	@Test
	public void shouldAddNewMessageToStorage() throws JMSException {
		TextMessage message = new MockTextMessage("my awesome message");

		sut.onMessage(message);

		Collection<SimpleMessage> resultInStorage = storage.getMessagesAfter(0);
		assertThat(resultInStorage).hasSize(1);
		SimpleMessage first = resultInStorage.iterator().next();

		assertThat(first.getText()).isEqualTo("my awesome message");
		assertThat(first.getProps()).isEmpty();
	}

	@Test
	public void shouldExtractOnlySpecifiedProperties() throws JMSException {
		TextMessage message = new MockTextMessage("test");
		message.setStringProperty("propA", "valA");

		// this should be ignored
		message.setStringProperty("propB", "valB");

		sut.onMessage(message);
		Collection<SimpleMessage> resultInStorage = storage.getMessagesAfter(0);
		assertThat(resultInStorage).hasSize(1);
		SimpleMessage first = resultInStorage.iterator().next();
		assertThat(first.getText()).isEqualTo("test");
		assertThat(first.getQueue()).isEqualTo("myQueue");
		assertThat(first.getProps()).hasSize(1).contains(entry("propA", "valA"));
	}
}
