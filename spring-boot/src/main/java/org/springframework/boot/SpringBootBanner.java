
package org.springframework.boot;

import java.io.PrintStream;

import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.core.env.Environment;

/**
 * Default Banner implementation which writes the 'Spring' banner.
 * 默认的横幅实现，写入Spring广告横幅。
 *
 * @author Phillip Webb
 */
class SpringBootBanner implements Banner {

	/**
	 * Spring横幅
	 */
	private static final String[] BANNER = { "",
			"  .   ____          _            __ _ _",
			" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\",
			"( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\",
			" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )",
			"  '  |____| .__|_| |_|_| |_\\__, | / / / /",
			" =========|_|==============|___/=/_/_/_/" };

	/**
	 * Spring Boot框架广告宣传文案
	 */
	private static final String SPRING_BOOT = " :: Spring Boot :: ";

	/**
	 * 表带线尺寸
	 */
	private static final int STRAP_LINE_SIZE = 42;

	@Override
	public void printBanner(Environment environment, Class<?> sourceClass,
			PrintStream printStream) {
		// 1.打印横幅
		for (String line : BANNER) {
			printStream.println(line);
		}
		// Spring Boot框架版本
		String version = SpringBootVersion.getVersion();
		version = (version != null) ? " (v" + version + ")" : "";
		String padding = "";
		while (padding.length() < STRAP_LINE_SIZE
				- (version.length() + SPRING_BOOT.length())) {
			padding += " ";
		}

		// 2.打印Spring Boot广告宣传文案
		printStream.println(AnsiOutput.toString(AnsiColor.GREEN, SPRING_BOOT,
				AnsiColor.DEFAULT, padding, AnsiStyle.FAINT, version));
		printStream.println();
	}

}
