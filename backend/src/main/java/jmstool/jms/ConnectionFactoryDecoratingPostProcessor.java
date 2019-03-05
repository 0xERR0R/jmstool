package jmstool.jms;

import javax.jms.ConnectionFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.stereotype.Component;

/**
 * Creates {@link UserCredentialsConnectionFactoryAdapter} and {@link CachingConnectionFactory} wrappers for the
 * {@link ConnectionFactory}
 *
 */
@Component
public class ConnectionFactoryDecoratingPostProcessor implements BeanPostProcessor {

	@Value("${jmstool.useCachingConnectionFactory:true}")
	protected boolean useCachingConnectionFactory;

	@Value("${jmstool.connectionFactory.username:}")
	protected String username;

	@Value("${jmstool.connectionFactory.password:}")
	protected String password;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ConnectionFactory) {
			
			UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
			adapter.setTargetConnectionFactory((ConnectionFactory) bean);
			adapter.setUsername(username);
			adapter.setPassword(password);
			bean = adapter;

			if (useCachingConnectionFactory) {
				return new CachingConnectionFactory((ConnectionFactory) bean);
			}
		}
		return bean;
	}
}
