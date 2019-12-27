
package org.springframework.boot;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Interface used to indicate that a bean should <em>run</em> when it is contained within
 * a {@link SpringApplication}. Multiple {@link CommandLineRunner} beans can be defined
 * within the same application context and can be ordered using the {@link Ordered}
 * interface or {@link Order @Order} annotation.
 * 用于指示被包含在Spring应用中的bean应该运行的接口。
 * 可以在相同应用上下文中定义多个命令行运行器beans，并使用Ordered接口或@Order注解对其进行排序。
 * <p>
 * If you need access to {@link ApplicationArguments} instead of the raw String array
 * consider using {@link ApplicationRunner}.
 * 如果需要访问应用run方法参数列表对象，而不是原始的字符串数组，请考虑使用应用运行器。
 *
 * @author Dave Syer
 * @see ApplicationRunner
 */
public interface CommandLineRunner {

	/**
	 * Callback used to run the bean.
	 * 用于运行bean的回调。
	 * @param args incoming main method arguments
	 * @throws Exception on error
	 */
	void run(String... args) throws Exception;

}
