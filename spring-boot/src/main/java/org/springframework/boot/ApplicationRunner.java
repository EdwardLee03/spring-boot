
package org.springframework.boot;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Interface used to indicate that a bean should <em>run</em> when it is contained within
 * a {@link SpringApplication}. Multiple {@link ApplicationRunner} beans can be defined
 * within the same application context and can be ordered using the {@link Ordered}
 * interface or {@link Order @Order} annotation.
 * 用于指示被包含在Spring应用中的bean应该运行的接口。
 * 可以在相同应用上下文中定义多个应用运行器beans，并使用Ordered接口或@Order注解对其进行排序。
 *
 * @author Phillip Webb
 * @since 1.3.0
 * @see CommandLineRunner
 */
public interface ApplicationRunner {

	/**
	 * Callback used to run the bean.
	 * 用于运行bean的回调。
	 * @param args incoming application arguments
	 * @throws Exception on error
	 */
	void run(ApplicationArguments args) throws Exception;

}
