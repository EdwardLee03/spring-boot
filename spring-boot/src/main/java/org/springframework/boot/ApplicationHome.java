
package org.springframework.boot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Provides access to the application home directory. Attempts to pick a sensible home for
 * both Jar Files, Exploded Archives and directly running applications.
 * 提供对应用程序主目录的访问。
 * 尝试为jar文件列表，分解的档案和直接运行的应用程序选择一个明智的主目录。
 *
 * @author Phillip Webb
 * @since 1.2.0
 */
public class ApplicationHome {

	/**
	 * 资源文件
	 */
	private final File source;

	/**
	 * 资源目录
	 */
	private final File dir;

	/**
	 * Create a new {@link ApplicationHome} instance.
	 */
	public ApplicationHome() {
		this(null);
	}

	/**
	 * Create a new {@link ApplicationHome} instance for the specified source class.
	 * @param sourceClass the source class or {@code null} 资源类
	 */
	public ApplicationHome(Class<?> sourceClass) {
		this.source = findSource((sourceClass != null) ? sourceClass : getStartClass());
		this.dir = findHomeDir(this.source);
	}

	/**
	 * 应用启动类。
	 */
	private Class<?> getStartClass() {
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			// 获取启动类
			return getStartClass(classLoader.getResources("META-INF/MANIFEST.MF"));
		}
		catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 获取启动类。
	 */
	private Class<?> getStartClass(Enumeration<URL> manifestResources) {
		while (manifestResources.hasMoreElements()) {
			try {
				InputStream inputStream = manifestResources.nextElement().openStream();
				try {
					// jar清单
					Manifest manifest = new Manifest(inputStream);
					// 启动类完全限定名称
					String startClass = manifest.getMainAttributes()
							.getValue("Start-Class");
					if (startClass != null) {
						return ClassUtils.forName(startClass,
								getClass().getClassLoader());
					}
				}
				finally {
					inputStream.close();
				}
			}
			catch (Exception ex) {
				// Swallow and continue
			}
		}
		return null;
	}

	private File findSource(Class<?> sourceClass) {
		try {
			ProtectionDomain domain = (sourceClass != null)
					? sourceClass.getProtectionDomain() : null;
			CodeSource codeSource = (domain != null) ? domain.getCodeSource() : null;
			URL location = (codeSource != null) ? codeSource.getLocation() : null;
			File source2 = (location != null) ? findSource(location) : null;
			if (source2 != null && source2.exists() && !isUnitTest()) {
				return source2.getAbsoluteFile();
			}
			return null;
		}
		catch (Exception ex) {
			return null;
		}
	}

	private boolean isUnitTest() {
		try {
			for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
				if (element.getClassName().startsWith("org.junit.")) {
					return true;
				}
			}
		}
		catch (Exception ex) {
			// Swallow and continue
		}
		return false;
	}

	private File findSource(URL location) throws IOException {
		URLConnection connection = location.openConnection();
		if (connection instanceof JarURLConnection) {
			return getRootJarFile(((JarURLConnection) connection).getJarFile());
		}
		return new File(location.getPath());
	}

	private File getRootJarFile(JarFile jarFile) {
		String name = jarFile.getName();
		int separator = name.indexOf("!/");
		if (separator > 0) {
			name = name.substring(0, separator);
		}
		return new File(name);
	}

	/**
	 * 查找应用程序的主目录。
	 */
	private File findHomeDir(File source) {
		File homeDir = source;
		homeDir = (homeDir != null) ? homeDir : findDefaultHomeDir();
		if (homeDir.isFile()) {
			homeDir = homeDir.getParentFile();
		}
		homeDir = (homeDir.exists() ? homeDir : new File("."));
		return homeDir.getAbsoluteFile();
	}

	private File findDefaultHomeDir() {
		String userDir = System.getProperty("user.dir");
		return new File(StringUtils.hasLength(userDir) ? userDir : ".");
	}

	/**
	 * Returns the underlying source used to find the home directory. This is usually the
	 * jar file or a directory. Can return {@code null} if the source cannot be
	 * determined.
	 * @return the underlying source or {@code null}
	 */
	public File getSource() {
		return this.source;
	}

	/**
	 * Returns the application home directory.
	 * @return the home directory (never {@code null})
	 */
	public File getDir() {
		return this.dir;
	}

	@Override
	public String toString() {
		return getDir().toString();
	}

}
