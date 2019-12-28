
package org.springframework.boot.context.event;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationEvent;

/**
 * Base class for {@link ApplicationEvent} related to a {@link SpringApplication}.
 * 与Spring应用相关的应用事件基类。
 *
 * @author Phillip Webb
 */
@SuppressWarnings("serial")
public abstract class SpringApplicationEvent extends ApplicationEvent {

	/**
	 * 方法参数列表
	 */
	private final String[] args;

	public SpringApplicationEvent(SpringApplication application, String[] args) {
		// Spring应用对象
		super(application);
		this.args = args;
	}

	/**
	 * 返回Spring应用对象。
	 */
	public SpringApplication getSpringApplication() {
		return (SpringApplication) getSource();
	}

	public final String[] getArgs() {
		return this.args;
	}

}
