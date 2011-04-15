package com.profiprog.configinject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringValueResolver;

public class VariablesResolverPlaceholderConfigurer implements BeanFactoryPostProcessor {

	private static final Logger logger = LoggerFactory.getLogger(VariablesResolverPlaceholderConfigurer.class);

	private final VariableResolver variableResolver;

	private final StringValueResolver proxy = new StringValueResolver() {
		@Override
		public String resolveStringValue(String strVal) {
			try {
				String resolvedVal = variableResolver.resolveStringValue(strVal);
				if (!strVal.equals(resolvedVal))
					logger.info("Resolved values: " + strVal + " -> " + resolvedVal);
				return resolvedVal;
			} catch (RuntimeException e) {
				logger.error("Unable to resolve values in: " + strVal, e);
				return strVal;
			}
		}
	};

	public VariablesResolverPlaceholderConfigurer(VariableResolver variableResolver) {
		this.variableResolver = variableResolver;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(proxy);

		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String curName : beanNames) {
			// Check that we're not parsing our own bean definition,
			// to avoid failing on unresolvable placeholders in properties file
			// locations.
			BeanDefinition bd = beanFactory.getBeanDefinition(curName);
			try {
				if (!isVariableSource(bd.getBeanClassName()))
					visitor.visitBeanDefinition(bd);
			} catch (Exception ex) {
				throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex.getMessage());
			}
		}

		// New in Spring 2.5: resolve placeholders in alias target names and
		// aliases as well.
		beanFactory.resolveAliases(proxy);

		// New in Spring 3.0: resolve placeholders in embedded values such as
		// annotation attributes.
		beanFactory.addEmbeddedValueResolver(proxy);
	}

	private boolean isVariableSource(String beanClassName) throws ClassNotFoundException {
		return VariableSource.class.isAssignableFrom(Class.forName(beanClassName));
	}

}
