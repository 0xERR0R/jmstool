package jmstool.jms;

import javax.jms.ConnectionFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * if enabled, creates a {@link CachingConnectionFactory} wrapper for the
 * {@link ConnectionFactory}
 * 
 *
 */
@Component
@ConditionalOnProperty(prefix = "jmstool", value = "useCachingConnectionFactory", havingValue = "true", matchIfMissing = false)
public class CachingConnectionFactoryDecoratingPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ConnectionFactory) {
			return new CachingConnectionFactory((ConnectionFactory) bean);
		}
		return bean;
	}
}
