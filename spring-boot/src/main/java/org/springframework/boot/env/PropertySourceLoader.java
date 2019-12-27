
package org.springframework.boot.env;

import java.io.IOException;

import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Strategy interface located via {@link SpringFactoriesLoader} and used to load a
 * {@link PropertySource}.
 * 属性源加载器，通过Spring工厂加载器定位并加载属性源的策略接口。
 *
 * @author Dave Syer
 * @author Phillip Webb
 */
public interface PropertySourceLoader {

	/**
	 * Returns the file extensions that the loader supports (excluding the '.').
	 * 返回加载器支持的文件扩展名。
	 * @return the file extensions
	 */
	String[] getFileExtensions();

	/**
	 * Load the resource into a property source.
	 * 加载资源文件到属性源中。
	 * @param name the name of the property source
	 * @param resource the resource to load
	 * @param profile the name of the profile to load or {@code null}. The profile can be
	 * used to load multi-document files (such as YAML). Simple property formats should
	 * {@code null} when asked to load a profile.
	 * @return a property source or {@code null}
	 * @throws IOException if the source cannot be loaded
	 */
	PropertySource<?> load(String name, Resource resource, String profile)
			throws IOException;

}
