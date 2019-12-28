
package org.springframework.boot.context.event;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Event published as when a {@link SpringApplication} is starting up and the
 * {@link ApplicationContext} is fully prepared but not refreshed. The bean definitions
 * will be loaded and the {@link Environment} is ready for use at this stage.
 * 当Spring应用启动且应用上下文已完全准备好但未刷新时发布的事件。
 * 在这个阶段将加载所有bean定义，并且可以使用应用运行时环境。
 *
 * @author Dave Syer
 */
@SuppressWarnings("serial")
public class ApplicationPreparedEvent extends SpringApplicationEvent {

	/**
	 * 可配置的应用上下文
	 */
	private final ConfigurableApplicationContext context;

	/**
	 * Create a new {@link ApplicationPreparedEvent} instance.
	 * @param application the current application
	 * @param args the arguments the application is running with
	 * @param context the ApplicationContext about to be refreshed
	 */
	public ApplicationPreparedEvent(SpringApplication application, String[] args,
			ConfigurableApplicationContext context) {
		super(application, args);
		this.context = context;
	}

	/**
	 * Return the application context.
	 * 返回可配置的应用上下文。
	 * @return the context
	 */
	public ConfigurableApplicationContext getApplicationContext() {
		return this.context;
	}

}
