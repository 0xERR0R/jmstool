package jmstool.jms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;

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
		TextMessage message = mock(TextMessage.class);
		when(message.getText()).thenReturn("my awesome message");
		sut.onMessage(message);

		Collection<SimpleMessage> resultInStorage = storage.getMessagesAfter(0);
		assertThat(resultInStorage).hasSize(1);
		assertThat(resultInStorage).first().hasFieldOrPropertyWithValue("text", "my awesome message");
	}
}
