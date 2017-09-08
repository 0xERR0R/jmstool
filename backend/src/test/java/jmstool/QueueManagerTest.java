package jmstool;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.mockrunner.jms.JMSTestModule;
import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockQueue;
import com.mockrunner.mock.jms.MockTextMessage;

import jmstool.model.SimpleMessage;
import jmstool.storage.LocalMessageStorage;

public class QueueManagerTest {
	private QueueManager sut;
	private JMSTestModule testModule;
	private MockQueue q1, q2;
	private LocalMessageStorage storage;

	@Before
	public void setUp() throws IllegalStateException, NamingException {
		sut = new QueueManager();
		// Mock for storage
		storage = mock(LocalMessageStorage.class);
		sut.incomingLocalStorage = storage;

		JMSMockObjectFactory f = new JMSMockObjectFactory();

		sut.cf = f.getMockQueueConnectionFactory();

		// create 2 queues
		q1 = f.getDestinationManager().createQueue("test1");
		q2 = f.getDestinationManager().createQueue("test2");

		// register both queues unter JNDI names
		SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		builder.bind("java:comp/env/q1", q1);
		builder.bind("java:comp/env/q2", q2);
		builder.activate();
		testModule = new JMSTestModule(f);
	}

	@Test(expected = NameNotFoundException.class)
	public void lookupIncommingQueueWithWrongNameThrowsException() throws Exception {
		sut.incomingQueues = Arrays.asList("java:comp/env/wrongName");
		sut.run("");
	}

	@Test
	public void emptyIncomingQueuesList() throws Exception {
		sut.incomingQueues = Collections.emptyList();
		sut.run("");
		testModule.verifyAllQueueSessionsClosed();
	}

	@Test
	public void incomingMessageShouldBeAddedToStorage() throws Exception {
		sut.incomingQueues = Arrays.asList("java:comp/env/q1", "java:comp/env/q2");
		sut.run("");
		testModule.verifyQueueConnectionStarted();

		// new message in queue q1
		q1.addMessage(new MockTextMessage("bla"));

		// the message was added to storage
		ArgumentCaptor<SimpleMessage> argument = ArgumentCaptor.forClass(SimpleMessage.class);
		verify(storage, timeout(5000)).addMessage(argument.capture());
		assertEquals("java:comp/env/q1", argument.getValue().getQueue());
		assertEquals("bla", argument.getValue().getText());

		// new message in queue q2
		q2.addMessage(new MockTextMessage("blup"));

		verify(storage, timeout(5000).times(2)).addMessage(argument.capture());
		assertEquals("java:comp/env/q2", argument.getValue().getQueue());
		assertEquals("blup", argument.getValue().getText());
	}

	@Test
	public void closeConsumersOnDestroy() throws Exception {
		sut.incomingQueues = Arrays.asList("java:comp/env/q1", "java:comp/env/q2");

		sut.run("");
		testModule.verifyQueueConnectionStarted();

		sut.destroy();

		// check if all connections were closed during shut down
		testModule.verifyAllQueueSessionsClosed();
	}

	@Test
	public void lookupOutgoingQueues() throws Exception {
		sut.outgoingQueues = Arrays.asList("java:comp/env/q1", "java:comp/env/q1");
		sut.run("");
	}

	@Test(expected = NameNotFoundException.class)
	public void lookupOutgoingQueuesWithWrongNameThrowsException() throws Exception {
		sut.outgoingQueues = Arrays.asList("java:comp/env/q1", "java:comp/env/wrong");
		sut.run("");
	}

}
