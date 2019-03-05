package jmstool.jms;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolutionException;

import jmstool.model.SimpleMessage;
import jmstool.storage.LocalMessageStorage;

@RunWith(MockitoJUnitRunner.class)
public class AsyncMessageSenderTest {

	@Mock
	private JmsTemplate jmsTemplate;

	@Mock
	private LocalMessageStorage storage;

	private final AsyncMessageSender sut = new AsyncMessageSender();

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	@Before
	public void setUp() {
		sut.jmsTemplate = jmsTemplate;
		sut.outgoingStorage = storage;
	}

	@After
	public void tearDown() throws InterruptedException {
		executor.shutdownNow();
		executor.awaitTermination(5, SECONDS);
	}

	@Test
	public void sendMessage() throws InterruptedException {
		sut.send(new SimpleMessage("test", "queue"));

		// one message is pending
		assertThat(sut.getStats().getPendingCount()).isEqualTo(1);

		// start AsyncMessageSender asynchronous in a new Thread
		executor.execute(sut);

		await().until(() -> sut.getStats().getPendingCount() == 0);

		verify(jmsTemplate).send(eq("queue"), any());
		verify(storage).addMessage(argThat( //
				message -> message.getQueue().equals("queue") && //
						message.getText().equals("test")));

		assertThat(sut.getStats().getPendingCount()).isEqualTo(0);
	}

	@Test
	public void jmsExceptionIncreasesTotalErrorCount() throws InterruptedException {
		doThrow(new DestinationResolutionException("test")).when(jmsTemplate).send(anyString(), any());
		sut.send(new SimpleMessage("test", "queue"));
		executor.execute(sut);

		await().until(() -> sut.getStats().getTotalErrorCount() == 1);
	}
}
