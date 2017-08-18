package jmstool;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.mockrunner.jms.JMSTestModule;
import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockQueue;
import com.mockrunner.mock.jms.MockTextMessage;

import jmstool.model.SimpleMessage;
import jmstool.storage.LocalMessageStorage;

public class QueueManagerTest {
	private QueueManager qm;
	private JMSTestModule m;
	private MockQueue q1, q2;
	private LocalMessageStorage storage;

	@Before
	public void setUp() throws IllegalStateException, NamingException {
		SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		qm = new QueueManager();
		storage = Mockito.mock(LocalMessageStorage.class);
		JMSMockObjectFactory f = new JMSMockObjectFactory();

		qm.cf = f.getMockQueueConnectionFactory();
		qm.incomingLocalStorage = storage;
		q1 = f.getDestinationManager().createQueue("test1");
		q2 = f.getDestinationManager().createQueue("test2");
		builder.bind("java:comp/env/q1", q1);
		builder.bind("java:comp/env/q2", q2);
		builder.activate();
		m = new JMSTestModule(f);
	}

	@Test(expected = NameNotFoundException.class)
	public void queueIsNotBoundToJndiContext() throws Exception {
		qm.incomingQueues = Arrays.asList("java:comp/env/wrongName");
		qm.run("");
	}

	@Test
	public void emptyIncomingQueuesList() throws Exception {
		qm.incomingQueues = Collections.emptyList();
		qm.run("");
		m.verifyAllQueueSessionsClosed();
	}

	@Test
	public void incomingMessageShouldBeAddedToStorage() throws Exception {
		qm.incomingQueues = Arrays.asList("java:comp/env/q1", "java:comp/env/q2");
		qm.run("");
		m.verifyQueueConnectionStarted();

		q1.addMessage(new MockTextMessage("bla"));
		Thread.sleep(500);
		ArgumentCaptor<SimpleMessage> argument = ArgumentCaptor.forClass(SimpleMessage.class);
		Mockito.verify(storage).addMessage(argument.capture());
		assertEquals("java:comp/env/q1", argument.getValue().getQueue());
		assertEquals("bla", argument.getValue().getText());

		q2.addMessage(new MockTextMessage("blup"));
		Thread.sleep(500);
		Mockito.verify(storage, Mockito.times(2)).addMessage(argument.capture());
		assertEquals("java:comp/env/q2", argument.getValue().getQueue());
		assertEquals("blup", argument.getValue().getText());
	}

	@Test
	public void closeConsumersOnDestroy() throws Exception {
		qm.incomingQueues = Arrays.asList("java:comp/env/q1", "java:comp/env/q2");

		qm.run("");
		m.verifyQueueConnectionStarted();

		qm.destroy();

		m.verifyAllQueueSessionsClosed();
	}

	@Test
	public void lookupOutgoingQueues() throws Exception {
		qm.outgoingQueues = Arrays.asList("java:comp/env/q1", "java:comp/env/q1");
		qm.run("");
	}

	@Test(expected = NameNotFoundException.class)
	public void lookupOutgoingQueuesWrongName() throws Exception {
		qm.outgoingQueues = Arrays.asList("java:comp/env/q1", "java:comp/env/wrong");
		qm.run("");
	}

}
