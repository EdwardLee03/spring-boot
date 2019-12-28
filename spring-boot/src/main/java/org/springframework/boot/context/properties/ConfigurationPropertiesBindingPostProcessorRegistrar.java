
package org.springframework.boot.context.properties;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link ImportBeanDefinitionRegistrar} for binding externalized application properties
 * to {@link ConfigurationProperties} beans.
 * 导入bean定义注册者，用于将外部的应用属性集绑定到带外部化配置属性集注解的beans。
 *
 * @author Dave Syer
 * @author Phillip Webb
 */
public class ConfigurationPropertiesBindingPostProcessorRegistrar
		implements ImportBeanDefinitionRegistrar {

	/**
	 * The bean name of the {@link ConfigurationPropertiesBindingPostProcessor}.
	 * 绑定者bean的名称
	 */
	public static final String BINDER_BEAN_NAME = ConfigurationPropertiesBindingPostProcessor.class
			.getName();

	/**
	 * 元数据bean的名称
	 */
	private static final String METADATA_BEAN_NAME = BINDER_BEAN_NAME + ".store";

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
			BeanDefinitionRegistry registry) {
		// 保证单例
		if (!registry.containsBeanDefinition(BINDER_BEAN_NAME)) {
			// 配置对象bean工厂的元数据
			BeanDefinitionBuilder meta = BeanDefinitionBuilder
					.genericBeanDefinition(ConfigurationBeanFactoryMetaData.class);
			// 配置属性集绑定的后置处理器
			BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(
					ConfigurationPropertiesBindingPostProcessor.class);
			bean.addPropertyReference("beanMetaDataStore", METADATA_BEAN_NAME);
			// 注册配置属性集绑定的后置处理器的bean定义
			registry.registerBeanDefinition(BINDER_BEAN_NAME, bean.getBeanDefinition());
			registry.registerBeanDefinition(METADATA_BEAN_NAME, meta.getBeanDefinition());
		}
	}

}
