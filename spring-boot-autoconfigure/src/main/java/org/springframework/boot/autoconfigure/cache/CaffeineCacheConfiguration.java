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

import java.util.List;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Caffeine cache configuration.
 * Caffeine本地内存缓存配置。
 *
 * @author Eddú Meléndez
 * @since 1.4.0
 */
@Configuration
@ConditionalOnClass({ Caffeine.class, CaffeineCacheManager.class })
@ConditionalOnMissingBean(CacheManager.class)
@Conditional({ CacheCondition.class })
class CaffeineCacheConfiguration {

	/**
	 * 缓存抽象的配置属性项
	 */
	private final CacheProperties cacheProperties;

	/**
	 * 缓存管理器的定制者列表
	 */
	private final CacheManagerCustomizers customizers;

	/**
	 * Caffeine本地内存缓存实例对象
	 */
	private final Caffeine<Object, Object> caffeine;

	/**
	 * Caffeine本地内存缓存的构建者的配置规范
	 */
	private final CaffeineSpec caffeineSpec;

	/**
	 * Caffeine本地内存缓存加载器
	 */
	private final CacheLoader<Object, Object> cacheLoader;

	CaffeineCacheConfiguration(CacheProperties cacheProperties,
			CacheManagerCustomizers customizers,
			ObjectProvider<Caffeine<Object, Object>> caffeine,
			ObjectProvider<CaffeineSpec> caffeineSpec,
			ObjectProvider<CacheLoader<Object, Object>> cacheLoader) {
		this.cacheProperties = cacheProperties;
		this.customizers = customizers;
		this.caffeine = caffeine.getIfAvailable();
		this.caffeineSpec = caffeineSpec.getIfAvailable();
		this.cacheLoader = cacheLoader.getIfAvailable();
	}

	@Bean
	public CaffeineCacheManager cacheManager() {
		// Caffeine本地内存缓存管理器
		CaffeineCacheManager cacheManager = createCacheManager();
		List<String> cacheNames = this.cacheProperties.getCacheNames();
		if (!CollectionUtils.isEmpty(cacheNames)) {
			cacheManager.setCacheNames(cacheNames);
		}
		// 定制缓存管理器
		return this.customizers.customize(cacheManager);
	}

	private CaffeineCacheManager createCacheManager() {
		// 创建Caffeine本地内存缓存管理器
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		setCacheBuilder(cacheManager);
		if (this.cacheLoader != null) {
			// 设置缓存加载器
			cacheManager.setCacheLoader(this.cacheLoader);
		}
		return cacheManager;
	}

	private void setCacheBuilder(CaffeineCacheManager cacheManager) {
		// 1.缓存抽象的配置属性项
		String specification = this.cacheProperties.getCaffeine().getSpec();
		if (StringUtils.hasText(specification)) {
			cacheManager.setCacheSpecification(specification);
		}
		// 2.Caffeine本地内存缓存的构建者配置的规范
		else if (this.caffeineSpec != null) {
			cacheManager.setCaffeineSpec(this.caffeineSpec);
		}
		// 3.Caffeine本地内存缓存实例对象
		else if (this.caffeine != null) {
			cacheManager.setCaffeine(this.caffeine);
		}
	}

}
