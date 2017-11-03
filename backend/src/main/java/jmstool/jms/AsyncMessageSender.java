package jmstool.jms;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import jmstool.model.SimpleMessage;
import jmstool.storage.LocalMessageStorage;

/**
 * JMS Message sender, which buffers the messages in the queue and sends them
 * asynchronously.
 *
 */
@Component
public class AsyncMessageSender implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final AtomicInteger errorCounter = new AtomicInteger();

	@Autowired
	protected JmsTemplate jmsTemplate;

	@Resource(name = "outgoingStorage")
	protected LocalMessageStorage outgoingStorage;

	private final BlockingQueue<SimpleMessage> pendingMessages = new LinkedBlockingQueue<>();

	public final static class Stats {
		private final int pendingCount;
		private final int totalErrorCount;

		public Stats(int pendingCount, int totalErrorCount) {
			this.pendingCount = pendingCount;
			this.totalErrorCount = totalErrorCount;
		}

		public int getPendingCount() {
			return pendingCount;
		}

		public int getTotalErrorCount() {
			return totalErrorCount;
		}
	}

	public Stats getStats() {
		return new Stats(pendingMessages.size(), errorCounter.get());
	}

	public void send(SimpleMessage message) throws InterruptedException {
		pendingMessages.put(message);
	}

	@Override
	public void run() {
		logger.debug("starting async message sender");
		while (!Thread.interrupted()) {
			try {
				SimpleMessage message = pendingMessages.take();
				sendMessage(message);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("execution was interrupted");
			} catch (Exception e) {
				logger.error("sending of JMS message failed", e);
				errorCounter.incrementAndGet();
			}
		}
		logger.debug("stopping async message sender");

	}

	private void sendMessage(SimpleMessage message) {
		jmsTemplate.send(message.getQueue(), new SimpleMessageTextCreator(message));
		outgoingStorage.addMessage(SimpleMessage.of(message));
	}
}
