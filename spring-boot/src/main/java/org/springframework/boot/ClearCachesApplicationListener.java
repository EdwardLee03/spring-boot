
package org.springframework.boot;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ReflectionUtils;

/**
 * {@link ApplicationListener} to cleanup caches once the context is loaded.
 * 一旦应用上下文加载完成，这个应用监听器会清理缓存。
 *
 * @author Phillip Webb
 */
class ClearCachesApplicationListener
		implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// 接收到"应用上下文刷新完成"事件
		// 1.清理反射缓存(类型声明的方法和字段对象列表缓存)
		ReflectionUtils.clearCache();
		// 2.清理类加载器的缓存
		clearClassLoaderCaches(Thread.currentThread().getContextClassLoader());
	}

	/**
	 * 清理类加载器的缓存。
	 */
	private void clearClassLoaderCaches(ClassLoader classLoader) {
		if (classLoader == null) {
			return;
		}
		try {
			// 调用类加载器声明的清理缓存的方法对象
			Method clearCacheMethod = classLoader.getClass()
					.getDeclaredMethod("clearCache");
			clearCacheMethod.invoke(classLoader);
		}
		catch (Exception ex) {
			// Ignore
		}
		// 递归地调用父亲类加载器
		clearClassLoaderCaches(classLoader.getParent());
	}

}
