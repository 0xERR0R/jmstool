package jmstool.controller;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
	public List<SimpleMessage> getMessages(@RequestParam MessageType messageType, @RequestParam long lastId,
			@RequestParam int maxCount) {

		Collection<SimpleMessage> result = null;
		if (messageType.equals(MessageType.INCOMING)) {
			result = incomingStorage.getMessagesAfter(lastId);
		} else {
			result = outgoingStorage.getMessagesAfter(lastId);
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
			Exceptions.sneak().run(() -> messageSender.send(message));
		}
	}

	@GetMapping(URL_API_QUEUES)
	public List<String> getOutgoingQueues() {
		return queueManager.getOutgoingQueues();
	}

	@PostMapping(path = URL_API_BULK_FILE, consumes = { "multipart/*" })
	public @ResponseBody Map<String, String> bulkSend(@RequestParam("file") MultipartFile file,
			@RequestParam("queue") String queue) throws IOException {
		Path tempFile = createTempFile(file);
		Files.write(tempFile, file.getBytes());

		logger.debug("processing archive file '{}', queue '{}'", file.getOriginalFilename(), queue);

		final AtomicInteger count = new AtomicInteger();
		try (FileSystem fs = FileSystems.newFileSystem(tempFile, null)) {
			Iterable<Path> rootDirectories = fs.getRootDirectories();
			for (Path path : rootDirectories) {
				try (Stream<Path> s = Files.walk(path)) {
					s.filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
							.peek(p -> logger.debug("iterating over file in archive '{}'", p))
							.peek(p -> count.incrementAndGet())
							.forEach(Exceptions.sneak().consumer(p -> messageSender.send(new SimpleMessage(p, queue))));
				}
			}
		}
		Files.delete(tempFile);

		return Collections.singletonMap("count", Integer.toString(count.get()));
	}

	private Path createTempFile(MultipartFile file) throws IOException {
		Path tempFile = Files.createTempFile(file.getOriginalFilename(), null, new FileAttribute[0]);
		return tempFile;
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

	@ExceptionHandler(BadRequestException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	protected void badRequestExceptionExceptionHandler() {
		logger.warn("Bad request was received");
	}
}
