package jmstool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
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
public class QueueManager implements CommandLineRunner, DisposableBean {
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
			logger.info("registering listener for incoming queue '{}'", queue);

			// lookup to fail fast
			new JndiLocatorDelegate().lookup(queue, Queue.class);
			DefaultMessageListenerContainer c = createContainer(queue);
			containers.add(c);
			c.start();
		}

		for (final String queue : outgoingQueues) {
			logger.info("lookup outgoing queue '{}'", queue);
			new JndiLocatorDelegate().lookup(queue, Queue.class);
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

	@Override
	public void destroy() throws Exception {
		for (DefaultMessageListenerContainer c : containers) {
			c.shutdown();
		}
	}
}
