
package org.springframework.boot.context.event;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

/**
 * Event published as early as conceivably possible as soon as a {@link SpringApplication}
 * has been started - before the {@link Environment} or {@link ApplicationContext} is
 * available, but after the {@link ApplicationListener}s have been registered. The source
 * of the event is the {@link SpringApplication} itself, but beware of using its internal
 * state too much at this early stage since it might be modified later in the lifecycle.
 * 一旦启动Spring应用，便会尽快发布事件。
 * 在应用运行时环境或应用上下文可用之前，但在应用监视器已注册之后。
 * 事件的来源是Spring应用对象本身，但是要提防在其早期阶段过多使用其内部状态，
 * 因为它可能会在生命周期的后期进行修改。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 1.5.0
 */
@SuppressWarnings("serial")
public class ApplicationStartingEvent extends SpringApplicationEvent {

	/**
	 * Create a new {@link ApplicationStartingEvent} instance.
	 * @param application the current application
	 * @param args the arguments the application is running with
	 */
	public ApplicationStartingEvent(SpringApplication application, String[] args) {
		super(application, args);
	}

}
