package jmstool.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class SimpleMessageTest {
	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(SimpleMessage.class).verify();
	}

	@Test
	public void copyShouldHaveAnotherId() {
		SimpleMessage m = new SimpleMessage("test", "queue");
		SimpleMessage copy = SimpleMessage.of(m);

		assertThat(copy.getId()).isNotEqualTo(m.getId());
		assertThat(copy.getText()).isEqualTo(m.getText());
		assertThat(copy.getQueue()).isEqualTo(m.getQueue());
	}
}
