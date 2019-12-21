
package org.springframework.boot;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Listener for the {@link SpringApplication} {@code run} method.
 * {@link SpringApplicationRunListener}s are loaded via the {@link SpringFactoriesLoader}
 * and should declare a public constructor that accepts a {@link SpringApplication}
 * instance and a {@code String[]} of arguments. A new
 * {@link SpringApplicationRunListener} instance will be created for each run.
 * Spring应用运行各阶段监视器，Spring应用run方法的监视器。
 * Spring应用运行各阶段监视器列表通过Spring工厂加载器(通用工厂加载机制)加载，
 * 应声明一个公共构造函数，其接受Spring应用实例和参数的字符串数组。
 * 每次运行都会创建一个新的Spring应用运行各阶段监视器实例。
 *
 * @author Phillip Webb
 * @author Dave Syer
 */
public interface SpringApplicationRunListener {

	/**
	 * Called immediately when the run method has first started. Can be used for very
	 * early initialization.
	 * 在首次启动run方法时立即调用，可用于非常早期的初始化。
	 */
	void starting();

	/**
	 * Called once the environment has been prepared, but before the
	 * {@link ApplicationContext} has been created.
	 * 一旦应用运行时环境已准备好就调用，但在应用上下文创建之前。
	 * @param environment the environment
	 */
	void environmentPrepared(ConfigurableEnvironment environment);

	/**
	 * Called once the {@link ApplicationContext} has been created and prepared, but
	 * before sources have been loaded.
	 * 一旦应用上下文已创建并准备好就调用，但在加载bean资源之前。
	 * @param context the application context
	 */
	void contextPrepared(ConfigurableApplicationContext context);

	/**
	 * Called once the application context has been loaded but before it has been
	 * refreshed.
	 * 一旦应用上下文已加载就调用，但在应用上下文刷新之前。
	 * @param context the application context
	 */
	void contextLoaded(ConfigurableApplicationContext context);

	/**
	 * Called immediately before the run method finishes.
	 * 在run方法完成之前立即调用。
	 * @param context the application context or null if a failure occurred before the
	 * context was created
	 * @param exception any run exception or null if run completed successfully.
	 */
	void finished(ConfigurableApplicationContext context, Throwable exception);

}
