package jmstool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.stereotype.Component;

import jmstool.jms.JmsMessageListener;
import jmstool.storage.LocalMessageStorage;

/**
 * Performs registration of incoming and outgoing queues.
 *
 */
@Component
public class QueueManager implements CommandLineRunner {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${jmstool.incomingQueues:}")
	protected List<String> incomingQueues = new ArrayList<>();

	@Value("${jmstool.outgoingQueues:}")
	protected List<String> outgoingQueues = new ArrayList<>();

	@Value("${jmstool.showMessagePropertiesForIncomingMessages:}")
	protected List<String> incomingMessagesProperties = new ArrayList<>();

	@Autowired
	protected ConnectionFactory cf;

	@Resource(name = "incomingStorage")
	protected LocalMessageStorage incomingLocalStorage;

	private final Collection<DefaultMessageListenerContainer> containers = new ArrayList<>();

	@Override
	public void run(String... arg0) throws Exception {

		for (final String queue : incomingQueues) {
			registerIncomingQueue(queue);
		}

		for (final String queue : outgoingQueues) {
			registerOutgoingQueue(queue);
		}
	}

	private void registerOutgoingQueue(final String queue) throws NamingException {
		logger.info("lookup outgoing queue '{}'", queue);
		lookupQueue(queue);
	}

	private void registerIncomingQueue(final String queue) throws NamingException {
		logger.info("registering listener for incoming queue '{}'", queue);

		// lookup to fail fast
		lookupQueue(queue);
		DefaultMessageListenerContainer c = createContainer(queue);
		containers.add(c);
		c.start();
	}

	private void lookupQueue(String queue) throws NamingException {
		try {
			new JndiLocatorDelegate().lookup(queue, Queue.class);
		} catch (NamingException e) {
			logger.error("unable to lookup the queue '{}'", queue, e);
			throw e;
		}
	}

	private DefaultMessageListenerContainer createContainer(String queue) {
		DefaultMessageListenerContainer c = new DefaultMessageListenerContainer();
		c.setConnectionFactory(cf);
		c.setDestinationResolver(new JndiDestinationResolver());
		c.setDestinationName(queue);
		c.setMessageListener(new JmsMessageListener(queue, incomingLocalStorage, incomingMessagesProperties));
		c.setAutoStartup(false);
		c.afterPropertiesSet();
		return c;
	}

	public List<String> getOutgoingQueues() {
		return Collections.unmodifiableList(outgoingQueues);
	}

	@PreDestroy
	public void shutdown() {
		logger.debug("destroying running containers on shutdown");
		for (DefaultMessageListenerContainer c : containers) {
			c.shutdown();
		}
	}

	public Map<String, Boolean> getListenerStatus() {
		return containers.stream().collect(Collectors.toMap(c -> c.getDestinationName(), c -> c.isRunning()));

	}

	private MessageListenerContainer findContainerForQueue(String queue) {
		Optional<DefaultMessageListenerContainer> container = containers.stream()
				.filter(c -> c.getDestinationName().equals(queue)).findFirst();
		return container.orElseThrow(() -> new IllegalStateException("Couldn't find a listener for queuev" + queue));

	}

	public void stopListener(String queue) {
		logger.debug("stopping listener container for queue {}", queue);
		findContainerForQueue(queue).stop();
	}

	public void startListener(String queue) {
		logger.debug("starting listener container for queue {}", queue);
		findContainerForQueue(queue).start();
	}
}
