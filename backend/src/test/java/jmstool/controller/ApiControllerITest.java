package jmstool.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.annotation.Resource;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mockrunner.jms.DestinationManager;
import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockQueue;
import com.mockrunner.mock.jms.MockQueueConnectionFactory;
import com.mockrunner.mock.jms.MockTextMessage;

import static org.mockito.Mockito.*;

import jmstool.storage.LocalMessageStorage;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.jms.jndi-name = java:comp/env/jms/cf",
		"jmstool.incomingQueues = java:comp/env/jms/in1, java:comp/env/jms/in2",
		"jmstool.outgoingQueues =  java:comp/env/jms/out1,java:comp/env/jms/out2",
		"jmstool.userMessageProperties = MYPROP, OTHERPROP" })
@AutoConfigureMockMvc
public class ApiControllerITest {
	@Autowired
	private MockMvc mockMvc;

	private static MockQueue qIn1, qIn2, qOut1, qOut2;

	@Resource(name = "outgoingStorage")
	private LocalMessageStorage outgoingStorage;

	@BeforeClass
	public static void setUpOnce() throws IllegalStateException, NamingException {
		SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		JMSMockObjectFactory f = new JMSMockObjectFactory();

		MockQueueConnectionFactory connectionFactory = f.getMockQueueConnectionFactory();
		DestinationManager destinationManager = f.getDestinationManager();
		qIn1 = spy(destinationManager.createQueue("test1"));
		qIn2 = spy(destinationManager.createQueue("test2"));
		qOut1 = spy(destinationManager.createQueue("test3"));
		qOut2 = spy(destinationManager.createQueue("test4"));
		builder.bind("java:comp/env/jms/cf", connectionFactory);
		builder.bind("java:comp/env/jms/in1", qIn1);
		builder.bind("java:comp/env/jms/in2", qIn2);
		builder.bind("java:comp/env/jms/out1", qOut1);
		builder.bind("java:comp/env/jms/out2", qOut2);
		builder.activate();
	}
	
	@Before
	public void setUp()
	{
		outgoingStorage.clear();
		qIn1.clear();
		qIn2.clear();
		qOut1.clear();
		qOut2.clear();
		reset(qIn1);
		reset(qIn2);
		reset(qOut1);
		reset(qOut2);
	}

	@Test
	public void shouldReturnQueuesAsArray() throws Exception {

		mockMvc.perform(get("/api/queues")).andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(content().json("['java:comp/env/jms/out1','java:comp/env/jms/out2']"));
	}

	@Test
	public void shouldReturnUserProperties() throws Exception {
		mockMvc.perform(get("/api/properties")).andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(content().json("['MYPROP', 'OTHERPROP']"));
	}

	@Test
	public void sendMessageWithProperties() throws Exception {
		// storage is empty
		assertThat(outgoingStorage.getMessagesAfter(0)).hasSize(0);

		// send message via REST
		mockMvc.perform(post("/api/send").content( //
				"{\"queue\":\"java:comp/env/jms/out1\"," + //
						"\"text\": \"messageText\"," + //
						"\"props\": {\"MYPROP\":\"MYVALUE\"}}")
				.contentType("application/json")).andExpect(status().isOk());

		verify(qOut1, timeout(5000)).addMessage(any());
		
		// Message was sent with the outgoing queue
		List<MockTextMessage> receivedMessageList = qOut1.getCurrentMessageList();
		assertThat(receivedMessageList).hasSize(1);
		MockTextMessage receivedMessage = receivedMessageList.get(0);
		assertThat(receivedMessage.getText()).isEqualTo("messageText");
		assertThat(receivedMessage.getStringProperty("MYPROP")).isEqualTo("MYVALUE");

		// Storage has now one message
		assertThat(outgoingStorage.getMessagesAfter(0)).hasSize(1);
	}
	
	@Test
	public void sendMessageWithCount() throws Exception {
		// storage is empty
		assertThat(outgoingStorage.getMessagesAfter(0)).hasSize(0);

		// send message via REST
		mockMvc.perform(post("/api/send?count=3").content( //
				"{\"queue\":\"java:comp/env/jms/out1\"," + //
						"\"text\": \"messageText\"," + //
						"\"props\": {}}")
				.contentType("application/json")).andExpect(status().isOk());
		
		verify(qOut1, timeout(5000).times(3)).addMessage(any());

		// Message was sent with the outgoing queue
		List<MockTextMessage> receivedMessageList = qOut1.getCurrentMessageList();
		assertThat(receivedMessageList).hasSize(3);
		assertThat(receivedMessageList.get(0).getText()).isEqualTo("messageText");
		assertThat(receivedMessageList.get(1).getText()).isEqualTo("messageText");
		assertThat(receivedMessageList.get(2).getText()).isEqualTo("messageText");

		// Storage has now 3 messages
		assertThat(outgoingStorage.getMessagesAfter(0)).hasSize(3);
	}

	@Test
	public void getMessagesShouldReturnEmptyResult() throws Exception {
		mockMvc.perform(get("/api/messages") //
				.param("messageType", "INCOMING") //
				.param("lastId", "0") //
				.param("maxCount", "200")) //
				.andExpect(status().isOk()) //
				.andExpect(content().contentType("application/json;charset=UTF-8")) //
				.andExpect(content().json("[]"));
	}

	@Test
	public void getMessagesShouldReturnAllMessages() throws Exception {

		qIn1.addMessage(new MockTextMessage("message in queue1"));
		verify(qIn1, timeout(5000)).addMessage(any());
		
		// sleep here to guarantee the message order
		Thread.sleep(50);
		
		qIn2.addMessage(new MockTextMessage("message in queue2"));
		verify(qIn2, timeout(5000)).addMessage(any());
		
		mockMvc.perform(get("/api/messages") //
				.param("messageType", "INCOMING") //
				.param("lastId", "0") //
				.param("maxCount", "200")) //
				.andExpect(status().isOk()) //
				.andExpect(content().contentType("application/json;charset=UTF-8")) //
				.andExpect(jsonPath("$", hasSize(2))) //
				.andExpect(jsonPath("$[0].text", is("message in queue2")))
				.andExpect(jsonPath("$[0].queue", is("java:comp/env/jms/in2")))
				.andExpect(jsonPath("$[1].text", is("message in queue1")))
				.andExpect(jsonPath("$[1].queue", is("java:comp/env/jms/in1")));
	}
}
