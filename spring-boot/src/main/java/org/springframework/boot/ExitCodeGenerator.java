
package org.springframework.boot;

/**
 * Interface used to generate an 'exit code' from a running command line
 * {@link SpringApplication}. Can be used on exceptions as well as directly on beans.
 * 用于从运行的命令行的spring应用对象生成退出代码的接口。
 * 即可以用于异常，也可以直接用于beans。
 *
 * @author Dave Syer
 * @see SpringApplication#exit(org.springframework.context.ApplicationContext,
 * ExitCodeGenerator...)
 */
public interface ExitCodeGenerator {

	/**
	 * Returns the exit code that should be returned from the application.
	 * @return the exit code.
	 */
	int getExitCode();

}
