
package org.springframework.boot.env;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Allows for customization of the application's {@link Environment} prior to the
 * application context being refreshed.
 * 允许在刷新应用上下文之前，自定义应用运行时环境。
 * <p>
 * EnvironmentPostProcessor implementations have to be registered in
 * {@code META-INF/spring.factories}, using the fully qualified name of this class as the
 * key.
 * 必须使用这个类的完全限定名称作为关键字，在META-INF/spring.factories中注册应用运行时环境后置处理器实现类。
 * <p>
 * {@code EnvironmentPostProcessor} processors are encouraged to detect whether Spring's
 * {@link org.springframework.core.Ordered Ordered} interface has been implemented or if
 * the {@link org.springframework.core.annotation.Order @Order} annotation is present and
 * to sort instances accordingly if so prior to invocation.
 * 鼓励应用运行时环境后置处理器检测是否已实现Ordered接口或是否存在@Order注解，
 * 并在调用之前对实例进行相应排序，如果有。
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @since 1.3.0
 */
public interface EnvironmentPostProcessor {

	/**
	 * Post-process the given {@code environment}.
	 * 对给定的应用运行时环境进行后置处理。
	 * @param environment the environment to post-process 后置处理的可配置的应用运行时环境
	 * @param application the application to which the environment belongs 环境所属的Spring应用对象
	 */
	void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application);

}
