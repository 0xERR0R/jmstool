package jmstool;

import java.util.Properties;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
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

	public static void main(String[] args) {
		SpringApplication.run(Jmstool2Application.class, args);
	}

	@Bean
	public TaskExecutor taskExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Bean
	public CommandLineRunner schedulingRunner(TaskExecutor executor, AsyncMessageSender sender) {
		return new CommandLineRunner() {
			public void run(String... args) throws Exception {
				executor.execute(sender);
			}
		};
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Jmstool2Application.class)
				.properties("spring.jackson.serialization.write_dates_as_timestamps=false");
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
