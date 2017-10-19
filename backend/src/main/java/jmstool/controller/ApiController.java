package jmstool.controller;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

import jmstool.BadRequestException;
import jmstool.QueueManager;
import jmstool.jms.AsyncMessageSender;
import jmstool.jms.AsyncMessageSender.Stats;
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

	public final static String URL_API_MESSAGES = "/api/messages";

	public final static String URL_API_SEND = "/api/send";

	public final static String URL_API_QUEUES = "/api/queues";

	public final static String URL_API_PROPERTIES = "/api/properties";

	public final static String URL_API_BULK_FILE = "/api/bulkFile";

	public final static String URL_API_WORK_IN_PROGRESS = "/api/workInProgress";

	public final static String URL_API_STATUS_LISTENER = "/api/statusListener";

	public final static String URL_API_STOP_LISTENER = "/api/stopListener";

	public final static String URL_API_START_LISTENER = "/api/startListener";

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
	public List<SimpleMessage> getMessages(@RequestParam MessageType messageType, @RequestParam long lastId,
			@RequestParam int maxCount) {

		Collection<SimpleMessage> result = null;
		switch (messageType) {
		case INCOMING:
			result = incomingStorage.getMessagesAfter(lastId);
			break;
		case OUTGOING:
			result = outgoingStorage.getMessagesAfter(lastId);
			break;
		}

		// sort and limit
		return result.stream().sorted(Collections.reverseOrder()).limit(maxCount).collect(Collectors.toList());
	}

	@GetMapping(URL_API_PROPERTIES)
	public List<String> getNewMessageProperties() {
		return userMessageProperties;
	}

	@PostMapping(URL_API_SEND)
	public void sendMessage(@RequestParam Optional<Integer> count, @RequestBody SimpleMessage message) {

		final int total = count.orElse(1);

		for (int i = 0; i < total; i++) {
			logger.debug("sending new message '{}' to queue '{}' with props '{}' count {}/{} ", message.getText(),
					message.getQueue(), message.getProps(), i + 1, total);
			messageSender.send(message);
		}

	}

	@GetMapping(URL_API_QUEUES)
	public List<String> getOutgoingQueues() {
		return queueManager.getOutgoingQueues();
	}

	@PostMapping(path = URL_API_BULK_FILE, consumes = { "multipart/*" })
	public @ResponseBody Map<String, String> bulkSend(@RequestParam("file") MultipartFile file,
			@RequestParam("queue") String queue) throws IOException, InterruptedException {
		Path tempFile = Files.createTempFile(file.getOriginalFilename(), null, new FileAttribute[0]);
		Files.write(tempFile, file.getBytes(), new OpenOption[0]);

		logger.debug("processing archive file '{}', queue '{}'", file.getOriginalFilename(), queue);

		final AtomicInteger count = new AtomicInteger();
		for (Path path : FileSystems.newFileSystem(tempFile, null).getRootDirectories()) {
			Files.walk(path).filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
					.peek(p -> logger.debug("iterating over file in archive '{}'", p))
					.peek(p -> count.incrementAndGet())
					.forEach(p -> messageSender.send(createSimpleMessageFromPath(p, queue)));
		}

		Files.delete(tempFile);

		return Collections.singletonMap("count", Integer.toString(count.get()));
	}

	@GetMapping(URL_API_WORK_IN_PROGRESS)
	public Stats serverWorkInProgress() {
		return messageSender.getStats();
	}

	@GetMapping(URL_API_STATUS_LISTENER)
	public Map<String, Boolean> getStatusListener() {
		return queueManager.getListenerStatus();
	}

	@PostMapping(URL_API_STOP_LISTENER)
	public void stopListener(@RequestParam String queue) {
		queueManager.stopListener(queue);
	}

	@PostMapping(URL_API_START_LISTENER)
	public void startListener(@RequestParam String queue) {
		queueManager.startListener(queue);
	}

	private SimpleMessage createSimpleMessageFromPath(Path path, String queue) {
		try {
			return new SimpleMessage(path, queue);
		} catch (IOException e) {
			logger.error("can't create message from file '{}'", path, e);
			throw new RuntimeException(e);
		}
	}

	@ExceptionHandler(BadRequestException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	protected void badRequestExceptionExceptionHandler() {
	}
}
