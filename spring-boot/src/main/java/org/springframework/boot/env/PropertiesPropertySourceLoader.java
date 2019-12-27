
package org.springframework.boot.env;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Strategy to load '.properties' files into a {@link PropertySource}.
 * 加载'.properties'文件集到属性源的策略。
 *
 * @author Dave Syer
 * @author Phillip Webb
 */
public class PropertiesPropertySourceLoader implements PropertySourceLoader {

	@Override
	public String[] getFileExtensions() {
		// 文件扩展名：properties、xml
		return new String[] { "properties", "xml" };
	}

	@Override
	public PropertySource<?> load(String name, Resource resource, String profile)
			throws IOException {
		if (profile == null) {
			// 加载资源属性
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			if (!properties.isEmpty()) {
				// 属性集的属性源
				return new PropertiesPropertySource(name, properties);
			}
		}
		return null;
	}

}
