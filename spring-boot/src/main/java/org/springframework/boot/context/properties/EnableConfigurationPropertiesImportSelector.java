
package org.springframework.boot.context.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Import selector that sets up binding of external properties to configuration classes
 * (see {@link ConfigurationProperties}). It either registers a
 * {@link ConfigurationProperties} bean or not, depending on whether the enclosing
 * {@link EnableConfigurationProperties} explicitly declares one. If none is declared then
 * a bean post processor will still kick in for any beans annotated as external
 * configuration. If one is declared then it a bean definition is registered with id equal
 * to the class name (thus an application context usually only contains one
 * {@link ConfigurationProperties} bean of each unique type).
 * 导入选择器，设置外部属性与配置类的绑定关系。
 * 它是否注册一个带外部化配置属性集注解的bean，具体取决于所封装的EnableConfigurationProperties是否显式声明一个。
 * 如果未声明任何bean，那么对于任何标注为外部化配置的beans，bean后置处理器仍将启动。
 * 如果声明了一个，则将其ID等于类名的bean定义进行注册。
 *
 * @author Dave Syer
 * @author Christian Dupuis
 * @author Stephane Nicoll
 */
class EnableConfigurationPropertiesImportSelector implements ImportSelector {

	@Override
	public String[] selectImports(AnnotationMetadata metadata) {
		// bean的外部化配置属性集注解属性
		MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(
				EnableConfigurationProperties.class.getName(), false);
		Object[] type = (attributes != null) ? (Object[]) attributes.getFirst("value")
				: null;
		if (type == null || type.length == 0) {
			// 外部化配置属性集绑定的后置处理器的注册者
			return new String[] {
					ConfigurationPropertiesBindingPostProcessorRegistrar.class
							.getName() };
		}
		// 外部化配置属性集的bean注册者，外部化配置属性集绑定的后置处理器的注册者
		return new String[] { ConfigurationPropertiesBeanRegistrar.class.getName(),
				ConfigurationPropertiesBindingPostProcessorRegistrar.class.getName() };
	}

	/**
	 * {@link ImportBeanDefinitionRegistrar} for configuration properties support.
	 * 外部化配置属性集的bean注册者，导入bean定义的注册者。
	 */
	public static class ConfigurationPropertiesBeanRegistrar
			implements ImportBeanDefinitionRegistrar {

		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata,
				BeanDefinitionRegistry registry) {
			// bean的外部化配置属性集注解属性
			MultiValueMap<String, Object> attributes = metadata
					.getAllAnnotationAttributes(
							EnableConfigurationProperties.class.getName(), false);
			// 配置注册的类型列表
			List<Class<?>> types = collectClasses(attributes.get("value"));
			for (Class<?> type : types) {
				// bean定义的名称前缀
				String prefix = extractPrefix(type);
				// bean定义的名称
				String name = (StringUtils.hasText(prefix) ? prefix + "-" + type.getName()
						: type.getName());
				if (!registry.containsBeanDefinition(name)) {
					// 注册bean定义
					registerBeanDefinition(registry, type, name);
				}
			}
		}

		/**
		 * 抽取bean定义的名称前缀。
		 */
		private String extractPrefix(Class<?> type) {
			// bean的外部化配置属性集注解对象
			ConfigurationProperties annotation = AnnotationUtils.findAnnotation(type,
					ConfigurationProperties.class);
			if (annotation != null) {
				// 属性的名称前缀
				return annotation.prefix();
			}
			return "";
		}

		private List<Class<?>> collectClasses(List<Object> list) {
			ArrayList<Class<?>> result = new ArrayList<Class<?>>();
			for (Object object : list) {
				for (Object value : (Object[]) object) {
					if (value instanceof Class && value != void.class) {
						result.add((Class<?>) value);
					}
				}
			}
			return result;
		}

		/**
		 * 注册bean定义。
		 */
		private void registerBeanDefinition(BeanDefinitionRegistry registry,
				Class<?> type, String name) {
			// bean定义构建者
			BeanDefinitionBuilder builder = BeanDefinitionBuilder
					.genericBeanDefinition(type);
			// bean定义对象
			AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
			// 注册bean定义
			registry.registerBeanDefinition(name, beanDefinition);

			// bean的外部化配置属性集注解对象
			ConfigurationProperties properties = AnnotationUtils.findAnnotation(type,
					ConfigurationProperties.class);
			Assert.notNull(properties,
					"No " + ConfigurationProperties.class.getSimpleName()
							+ " annotation found on  '" + type.getName() + "'.");
		}

	}

}
