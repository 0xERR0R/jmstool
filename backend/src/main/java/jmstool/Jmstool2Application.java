package jmstool;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.annotation.EnableJms;

import jmstool.jms.AsyncMessageSender;
import jmstool.storage.LocalMessageStorage;

/**
 * Main class to start the application
 *
 */
@SpringBootApplication
@EnableJms
public class Jmstool2Application extends SpringBootServletInitializer {

	static {
		// LocalDateTime should be serialized in a string, which can be read in
		// javascript
		System.setProperty("spring.jackson.serialization.write_dates_as_timestamps", "false");
	}

	@Bean
	public TaskExecutor taskExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Bean
	public CommandLineRunner asyncMessageSenderExecutorRunner(TaskExecutor executor, AsyncMessageSender sender) {
		return (args) -> executor.execute(sender);
	}

	@Bean
	public ConversionService conversionService() {
		// enables conversion of String in application.properties to list
		return new DefaultConversionService();
	}

	@Bean(name = "incomingStorage")
	public LocalMessageStorage incomingLocalMessagesStorage() {
		return new LocalMessageStorage();
	}

	@Bean(name = "outgoingStorage")
	public LocalMessageStorage outgoingLocalMessagesStorage() {
		return new LocalMessageStorage();
	}
}
