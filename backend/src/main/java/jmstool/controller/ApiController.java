package jmstool.controller;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.machinezoo.noexception.Exceptions;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jmstool.BadRequestException;
import jmstool.QueueManager;
import jmstool.jms.AsyncMessageSender;
import jmstool.jms.AsyncMessageSender.Stats;
import jmstool.model.MessageResult;
import jmstool.model.MessageType;
import jmstool.model.SimpleMessage;
import jmstool.storage.LocalMessageStorage;

/**
 * REST controllers for the web app.
 *
 */
@RestController
public class ApiController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String URL_API_MESSAGES = "/api/messages";

	public static final String URL_API_SEND = "/api/send";

	public static final String URL_API_QUEUES = "/api/queues";

	public static final String URL_API_PROPERTIES = "/api/properties";

	public static final String URL_API_BULK_FILE = "/api/bulkFile";

	public static final String URL_API_WORK_IN_PROGRESS = "/api/workInProgress";

	public static final String URL_API_STATUS_LISTENER = "/api/statusListener";

	public static final String URL_API_STOP_LISTENER = "/api/stopListener";

	public static final String URL_API_START_LISTENER = "/api/startListener";

	@Autowired
	private AsyncMessageSender messageSender;

	@Autowired
	private QueueManager queueManager;

	@Resource(name = "outgoingStorage")
	private LocalMessageStorage outgoingStorage;

	@Resource(name = "incomingStorage")
	private LocalMessageStorage incomingStorage;

	@Value("${jmstool.userMessageProperties:}")
	private List<String> userMessageProperties = new ArrayList<>();

	@GetMapping(URL_API_MESSAGES)
	@ApiOperation(value = "List of incoming or outgoing messages", tags = "read messages")
	public MessageResult getMessages(
			@ApiParam(value = "message type", allowableValues = "INCOMING, OUTGOING", required = true) @RequestParam MessageType messageType, //
			@ApiParam(value = "start id of message") @RequestParam(defaultValue = "0") Long lastId, //
			@ApiParam(value = "Max count of messages to fetch") @RequestParam(defaultValue = "100") int maxCount) {

		LocalMessageStorage storage = messageType.equals(MessageType.INCOMING) ? incomingStorage : outgoingStorage;
		Collection<SimpleMessage> result = storage.getMessagesAfter(lastId);


		// sort and limit
		List<SimpleMessage> messages = result.stream().sorted(Collections.reverseOrder()).limit(maxCount)
				.collect(Collectors.toList());
		return new MessageResult(messages, storage.getLastId());
	}

	@GetMapping(URL_API_PROPERTIES)
	@ApiOperation(value = "List of properties which can be used for outgoing messages", tags = "information")
	public List<String> getNewMessageProperties() {
		return userMessageProperties;
	}

	@PostMapping(URL_API_SEND)
	@ApiOperation(value = "send new message", tags = "send messages")
	public void sendMessage( //
			@ApiParam(value = "send message x times") @RequestParam(defaultValue = "1") int count, //
			@ApiParam(value = "message to send") @RequestBody SimpleMessage message) {
		validateQueue(message.getQueue());

		for (int i = 0; i < count; i++) {
			logger.debug("sending new message '{}' to queue '{}' with props '{}' count {}/{} ", message.getText(),
					message.getQueue(), message.getProps(), i + 1, count);
			Exceptions.sneak().run(() -> messageSender.send(message));
		}
	}

	@GetMapping(URL_API_QUEUES)
	@ApiOperation(value = "List of outgoing queues", tags = "information")
	public List<String> getOutgoingQueues() {
		return queueManager.getOutgoingQueues();
	}

	@PostMapping(path = URL_API_BULK_FILE, consumes = { "multipart/*" })
	@ApiOperation(value = "send all messages in a zip file", tags = "send messages")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns count of enqueued messages to be processed asynchronously"),
			@ApiResponse(code = 400, message = "File type or content is wrong; wrong queue name") })
	public @ResponseBody int bulkSend(
			@ApiParam(value = "zip archive with messages") @RequestParam("file") MultipartFile file, //
			@ApiParam(value = "destination queue") @RequestParam("queue") String queue) throws IOException {
		validateQueue(queue);
		Path tempFile = createTempFile(file);
		Files.write(tempFile, file.getBytes());

		logger.debug("processing archive file '{}', queue '{}'", file.getOriginalFilename(), queue);

		final AtomicInteger count = new AtomicInteger();
		try {
			try (FileSystem fs = FileSystems.newFileSystem(tempFile, null)) {
				Iterable<Path> rootDirectories = fs.getRootDirectories();
				for (Path path : rootDirectories) {
					try (Stream<Path> s = Files.walk(path)) {
						s.filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
								.peek(p -> logger.debug("iterating over file in archive '{}'", p))
								.peek(p -> count.incrementAndGet()).forEach(Exceptions.sneak()
										.consumer(p -> messageSender.send(new SimpleMessage(p, queue))));
					}
				}
			}
		} catch (Exception e) {
			throw new BadRequestException("can't process file");
		} finally {
			Files.delete(tempFile);
		}

		return count.get();
	}

	private void validateQueue(String queue) {
		if (!queueManager.getOutgoingQueues().contains(queue)) {
			throw new BadRequestException("Unknown queue '" + queue + "'");
		}
	}

	private Path createTempFile(MultipartFile file) throws IOException {
		return Files.createTempFile(file.getOriginalFilename(), null);
	}

	@GetMapping(URL_API_WORK_IN_PROGRESS)
	@ApiOperation(value = "Information about current work load status", tags = "information")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Returns count of errors and messages to send") })
	public Stats serverWorkInProgress() {
		return messageSender.getStats();
	}

	@GetMapping(URL_API_STATUS_LISTENER)
	@ApiOperation(value = "Status of JMS listeners", tags = "control listener")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Running status of each JMS listener") })
	public Map<String, Boolean> getStatusListener() {
		return queueManager.getListenerStatus();
	}

	@PostMapping(URL_API_STOP_LISTENER)
	@ApiOperation(value = "Stop JMS listener for the queue", tags = "control listener")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Listener was stopped"),
			@ApiResponse(code = 400, message = "Wrong queue name") })
	public void stopListener(@RequestParam String queue) {
		queueManager.stopListener(queue);
	}

	@PostMapping(URL_API_START_LISTENER)
	@ApiOperation(value = "Start JMS listener for the queue", tags = "control listener")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Listener was started"),
			@ApiResponse(code = 400, message = "Wrong queue name") })
	public void startListener(@RequestParam String queue) {
		queueManager.startListener(queue);
	}

	@ExceptionHandler(BadRequestException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	protected void badRequestExceptionExceptionHandler() {
		logger.warn("Bad request was received");
	}
}
