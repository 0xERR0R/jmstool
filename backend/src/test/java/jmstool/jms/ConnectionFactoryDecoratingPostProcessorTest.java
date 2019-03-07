package jmstool.jms;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import javax.jms.ConnectionFactory;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.jms.connection.CachingConnectionFactory;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionFactoryDecoratingPostProcessorTest {
    
    @Mock
    private ConnectionFactory connectionFactory;

    private final ConnectionFactoryDecoratingPostProcessor sut = new ConnectionFactoryDecoratingPostProcessor();

	@Test
    public void testNonCaching() {
        sut.useCachingConnectionFactory = false;
        ConnectionFactory result = (ConnectionFactory) sut.postProcessAfterInitialization(connectionFactory, "mock");
        assertThat(result, not(instanceOf(CachingConnectionFactory.class)));
    }
}