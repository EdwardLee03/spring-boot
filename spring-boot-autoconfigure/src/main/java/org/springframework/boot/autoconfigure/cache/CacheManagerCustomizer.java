/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.cache;

import org.springframework.cache.CacheManager;

/**
 * Callback interface that can be implemented by beans wishing to customize the cache
 * manager before it is fully initialized, in particular to tune its configuration.
 * 希望在缓存管理器完全初始化之前定制它的bean实现的回调接口，特别是调优它的配置时。
 *
 * @param <T> the type of the {@link CacheManager} Spring的中央缓存管理器SPI
 * @author Stephane Nicoll
 * @since 1.3.3
 */
public interface CacheManagerCustomizer<T extends CacheManager> {

	/**
	 * Customize the cache manager.
	 * 定制缓存管理器。
	 * @param cacheManager the {@code CacheManager} to customize
	 */
	void customize(T cacheManager);

}
