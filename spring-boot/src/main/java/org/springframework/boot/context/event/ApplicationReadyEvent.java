
package org.springframework.boot.context.event;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Event published as late as conceivably possible to indicate that the application is
 * ready to service requests. The source of the event is the {@link SpringApplication}
 * itself, but beware of modifying its internal state since all initialization steps will
 * have been completed by then.
 * 可能在很晚才发布的事件，指示这个应用程序已准备就绪，可以处理请求。
 * 事件的来源是Spring应用对象本身，但是请注意不要修改其内部状态，因为所有初始化步骤到那时都已完成。
 *
 * @author Stephane Nicoll
 * @since 1.3.0
 * @see ApplicationFailedEvent
 */
@SuppressWarnings("serial")
public class ApplicationReadyEvent extends SpringApplicationEvent {

	/**
	 * 可配置的应用上下文
	 */
	private final ConfigurableApplicationContext context;

	/**
	 * Create a new {@link ApplicationReadyEvent} instance.
	 * @param application the current application
	 * @param args the arguments the application is running with
	 * @param context the context that was being created
	 */
	public ApplicationReadyEvent(SpringApplication application, String[] args,
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
