package jmstool.jms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.mockrunner.mock.jms.JMSMockObjectFactory;

import jmstool.model.SimpleMessage;

@RunWith(MockitoJUnitRunner.class)
public class SimpleMessageTextCreatorTest {

	private Session mockSession;

	@Before
	public void setUp() throws JMSException {
		JMSMockObjectFactory f = new JMSMockObjectFactory();
		mockSession = f.createMockConnectionFactory().createConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void convertTextMessageWithoutUserProperties() throws JMSException {
		SimpleMessage m = new SimpleMessage("test", "queue1");
		SimpleMessageTextCreator sut = new SimpleMessageTextCreator(m);

		TextMessage result = (TextMessage) sut.createMessage(mockSession);

		assertThat(result.getText()).isEqualTo("test");
		assertThat(Collections.list(result.getPropertyNames())).isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void convertTextMessageWithUserProperties() throws JMSException {
		Map<String, String> props = new HashMap<>();
		props.put("mykey1", "myval1");
		props.put("mykey2", "myval2");
		SimpleMessage m = new SimpleMessage("test", "queue1", props);
		SimpleMessageTextCreator sut = new SimpleMessageTextCreator(m);

		TextMessage result = (TextMessage) sut.createMessage(mockSession);

		assertThat(result.getText()).isEqualTo("test");
		assertThat(result.getStringProperty("mykey1")).isEqualTo("myval1");
		assertThat(result.getStringProperty("mykey2")).isEqualTo("myval2");
		assertThat(Collections.list(result.getPropertyNames())).hasSize(2);
	}
}
