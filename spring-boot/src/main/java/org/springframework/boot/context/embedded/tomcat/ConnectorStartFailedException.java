
package org.springframework.boot.context.embedded.tomcat;

import org.apache.catalina.connector.Connector;

import org.springframework.boot.context.embedded.EmbeddedServletContainerException;

/**
 * A {@code ConnectorStartFailedException} is thrown when a Tomcat {@link Connector} fails
 * to start, for example due to a port clash or incorrect SSL configuration.
 * 当Tomcat连接器无法启动时，抛出这个异常。
 *
 * @author Andy Wilkinson
 * @since 1.4.1
 */
public class ConnectorStartFailedException extends EmbeddedServletContainerException {

	/**
	 * Tomcat连接器监听的端口
	 */
	private final int port;

	/**
	 * Creates a new {@code ConnectorStartFailedException} for a connector that's
	 * configured to listen on the given {@code port}.
	 * @param port the port
	 */
	public ConnectorStartFailedException(int port) {
		super("Connector configured to listen on port " + port + " failed to start",
				null);
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

}
