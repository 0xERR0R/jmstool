package jmstool.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import jmstool.model.SimpleMessage;

public class LocalMessageStorageTest {
	private LocalMessageStorage sut = new LocalMessageStorage();

	@Test
	public void shouldReturnEmptyResultIfStorageIsEmpty() {
		assertThat(sut.getMessagesAfter(0).isEmpty());
	}

	@Test
	public void shouldReturnAllElementsIfIdIs0() {
		sut.addMessage(new SimpleMessage());
		sut.addMessage(new SimpleMessage());

		assertThat(sut.getMessagesAfter(0)).hasSize(2);
	}

	@Test
	public void shouldReturnAllElementsIfIdIsSmaler() {
		SimpleMessage message1 = new SimpleMessage();
		SimpleMessage message2 = new SimpleMessage();
		ReflectionTestUtils.setField(message1, "id", 25L);
		ReflectionTestUtils.setField(message2, "id", 35L);
		sut.addMessage(message1);
		sut.addMessage(message2);

		assertThat(sut.getMessagesAfter(7)).hasSize(2);
	}

	@Test
	public void shouldReturnEmptyListIfIdIsGreater() {
		SimpleMessage message1 = new SimpleMessage();
		SimpleMessage message2 = new SimpleMessage();
		ReflectionTestUtils.setField(message1, "id", 25L);
		ReflectionTestUtils.setField(message2, "id", 35L);
		sut.addMessage(message1);
		sut.addMessage(message2);

		assertThat(sut.getMessagesAfter(35)).hasSize(0);
	}

	@Test
	public void shouldReturnOneElement() {
		SimpleMessage message1 = new SimpleMessage();
		SimpleMessage message2 = new SimpleMessage();
		ReflectionTestUtils.setField(message1, "id", 25L);
		ReflectionTestUtils.setField(message2, "id", 35L);
		ReflectionTestUtils.setField(message2, "id", 45L);
		sut.addMessage(message1);
		sut.addMessage(message2);

		assertThat(sut.getMessagesAfter(44)).hasSize(1);
	}

	@Test
	public void testMaxSize() {
		SimpleMessage first = new SimpleMessage();
		sut.addMessage(first);

		assertThat(sut.getMessagesAfter(0)).contains(first);

		for (int i = 0; i < LocalMessageStorage.MAX_COUNT; i++) {
			sut.addMessage(new SimpleMessage());
		}

		// first message is not in the storage
		assertThat(sut.getMessagesAfter(0)).doesNotContain(first);
		assertThat(sut.getMessagesAfter(0)).hasSize(LocalMessageStorage.MAX_COUNT);
	}
}
