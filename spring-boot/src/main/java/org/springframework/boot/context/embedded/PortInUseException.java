
package org.springframework.boot.context.embedded;

/**
 * A {@code PortInUseException} is thrown when an embedded servlet container fails to
 * start due to a port already being in use.
 * 当嵌入式servlet容器由于端口已在使用中而无法启动时，抛出这个异常。
 *
 * @author Andy Wilkinson
 * @since 1.4.0
 */
public class PortInUseException extends EmbeddedServletContainerException {

	/**
	 * 正在使用的端口
	 */
	private final int port;

	/**
	 * Creates a new port in use exception for the given {@code port}.
	 * @param port the port that was in use
	 */
	public PortInUseException(int port) {
		super("Port " + port + " is already in use", null);
		this.port = port;
	}

	/**
	 * Returns the port that was in use.
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}

}
