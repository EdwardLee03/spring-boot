
package org.springframework.boot.context.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ErrorHandler;

/**
 * {@link SpringApplicationRunListener} to publish {@link SpringApplicationEvent}s.
 * 发布Spring应用事件的Spring应用运行监视器。
 * <p>
 * Uses an internal {@link ApplicationEventMulticaster} for the events that are fired
 * before the context is actually refreshed.
 * 将内部的应用事件广播器用于在实际刷新应用上下文之前触发的事件。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
public class EventPublishingRunListener implements SpringApplicationRunListener, Ordered {

	private static final Log logger = LogFactory.getLog(EventPublishingRunListener.class);

	/**
	 * Spring应用对象
	 */
	private final SpringApplication application;

	private final String[] args;

	/**
	 * 应用事件广播器
	 */
	private final SimpleApplicationEventMulticaster initialMulticaster;

	public EventPublishingRunListener(SpringApplication application, String[] args) {
		this.application = application;
		this.args = args;
		this.initialMulticaster = new SimpleApplicationEventMulticaster();
		for (ApplicationListener<?> listener : application.getListeners()) {
			this.initialMulticaster.addApplicationListener(listener);
		}
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void starting() {
		this.initialMulticaster
				.multicastEvent(new ApplicationStartedEvent(this.application, this.args));
	}

	@Override
	public void environmentPrepared(ConfigurableEnvironment environment) {
		// 应用运行时环境已准备就绪
		this.initialMulticaster.multicastEvent(new ApplicationEnvironmentPreparedEvent(
				this.application, this.args, environment));
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {
		// 应用上下文已准备就绪
	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
		for (ApplicationListener<?> listener : this.application.getListeners()) {
			if (listener instanceof ApplicationContextAware) {
				((ApplicationContextAware) listener).setApplicationContext(context);
			}
			context.addApplicationListener(listener);
		}
		// 应用上下文已完全准备好但未刷新
		this.initialMulticaster.multicastEvent(
				new ApplicationPreparedEvent(this.application, this.args, context));
	}

	@Override
	public void finished(ConfigurableApplicationContext context, Throwable exception) {
		SpringApplicationEvent event = getFinishedEvent(context, exception);
		if (context != null && context.isActive()) {
			// Listeners have been registered to the application context so we should
			// use it at this point if we can
			context.publishEvent(event);
		}
		else {
			// An inactive context may not have a multicaster so we use our multicaster to
			// call all of the context's listeners instead
			if (context instanceof AbstractApplicationContext) {
				for (ApplicationListener<?> listener : ((AbstractApplicationContext) context)
						.getApplicationListeners()) {
					this.initialMulticaster.addApplicationListener(listener);
				}
			}
			if (event instanceof ApplicationFailedEvent) {
				this.initialMulticaster.setErrorHandler(new LoggingErrorHandler());
			}
			this.initialMulticaster.multicastEvent(event);
		}
	}

	private SpringApplicationEvent getFinishedEvent(
			ConfigurableApplicationContext context, Throwable exception) {
		if (exception != null) {
			// 应用启动失败
			return new ApplicationFailedEvent(this.application, this.args, context,
					exception);
		}
		// 应用启动就绪
		return new ApplicationReadyEvent(this.application, this.args, context);
	}

	private static class LoggingErrorHandler implements ErrorHandler {

		@Override
		public void handleError(Throwable throwable) {
			logger.warn("Error calling ApplicationEventListener", throwable);
		}

	}

}
