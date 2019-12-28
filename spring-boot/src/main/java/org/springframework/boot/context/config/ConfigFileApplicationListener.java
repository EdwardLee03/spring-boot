
package org.springframework.boot.context.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.springframework.beans.BeansException;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.boot.bind.PropertySourcesPropertyValues;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.EnumerableCompositePropertySource;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertySourcesLoader;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;

/**
 * {@link EnvironmentPostProcessor} that configures the context environment by loading
 * properties from well known file locations. By default properties will be loaded from
 * 'application.properties' and/or 'application.yml' files in the following locations:
 * 应用运行时环境后置处理器，通过从知名文件位置加载属性集来配置应用上下文环境。
 * 默认情况下，属性集将从'application.properties'或'application.yml'文件中的以下位置加载：
 * <ul>
 * <li>classpath:/</li>
 * 类路径下的根目录
 * <li>file:./</li>
 * 文件当前目录
 * <li>classpath:/config/</li>
 * 类路径下的config目录
 * <li>file:./config/:</li>
 * 文件当前目录下的config目录
 * </ul>
 * <p>
 * Alternative search locations and names can be specified using
 * {@link #setSearchLocations(String)} and {@link #setSearchNames(String)}.
 * 可以使用setSearchLocations(String)和setSearchNames(String)指定其他搜索位置和名称。
 * <p>
 * Additional files will also be loaded based on active profiles. For example if a 'web'
 * profile is active 'application-web.properties' and 'application-web.yml' will be
 * considered.
 * 根据活动的配置文件集加载其他文件。
 * <p>
 * The 'spring.config.name' property can be used to specify an alternative name to load
 * and the 'spring.config.location' property can be used to specify alternative search
 * locations or specific files.
 * 'spring.config.name'属性可用于指定要加载的替代名称，'spring.config.location'属性可用于指定替代搜索位置或特定文件集。
 * <p>
 * Configuration properties are also bound to the {@link SpringApplication}. This makes it
 * possible to set {@link SpringApplication} properties dynamically, like the sources
 * ("spring.main.sources" - a CSV list) the flag to indicate a web environment
 * ("spring.main.web_environment=true") or the flag to switch off the banner
 * ("spring.main.show_banner=false").
 * 配置属性集也能绑定到Spring应用对象。
 * 这样就可以动态地设置Spring应用对象的属性集('spring.main'前缀)。
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @author Eddú Meléndez
 */
public class ConfigFileApplicationListener
		implements EnvironmentPostProcessor, SmartApplicationListener, Ordered {

	/**
	 * 默认的属性源列表
	 */
	private static final String DEFAULT_PROPERTIES = "defaultProperties";

	// Note the order is from least to most specific (last one wins)
	/**
	 * 默认的搜索路径集
	 * <pre>
	 * classpath:/ - 类路径下的根目录
	 * classpath:/config/ - 类路径下的config目录
	 * file:./ - 文件当前目录
	 * file:./config/ - 文件当前目录下的config目录
	 * </pre>
	 */
	private static final String DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/,file:./,file:./config/";

	/**
	 * 属性文件的默认名称
	 */
	private static final String DEFAULT_NAMES = "application";

	/**
	 * The "active profiles" property name.
	 * 活动的配置文件集的属性名
	 */
	public static final String ACTIVE_PROFILES_PROPERTY = "spring.profiles.active";

	/**
	 * The "includes profiles" property name.
	 * 包含的配置文件集的属性名
	 */
	public static final String INCLUDE_PROFILES_PROPERTY = "spring.profiles.include";

	/**
	 * The "config name" property name.
	 * 配置名称的属性名
	 */
	public static final String CONFIG_NAME_PROPERTY = "spring.config.name";

	/**
	 * The "config location" property name.
	 * 配置路径的属性名
	 *
	 */
	public static final String CONFIG_LOCATION_PROPERTY = "spring.config.location";

	/**
	 * The default order for the processor.
	 * 处理器的默认顺序
	 */
	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

	/**
	 * Name of the application configuration {@link PropertySource}.
	 * 应用配置属性源的名称
	 */
	public static final String APPLICATION_CONFIGURATION_PROPERTY_SOURCE_NAME = "applicationConfigurationProperties";

	private final DeferredLog logger = new DeferredLog();

	/**
	 * 配置文件的搜索路径列表
	 */
	private String searchLocations;

	/**
	 * 配置文件的名称列表
	 */
	private String names;

	private int order = DEFAULT_ORDER;

	/**
	 * 类型转换服务
	 */
	private final ConversionService conversionService = new DefaultConversionService();

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		// 应用运行时环境准备就绪事件，应用上下文准备就绪事件
		return ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(eventType)
				|| ApplicationPreparedEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public boolean supportsSourceType(Class<?> aClass) {
		return true;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationEnvironmentPreparedEvent) {
			// 处理应用运行时环境准备就绪事件
			onApplicationEnvironmentPreparedEvent(
					(ApplicationEnvironmentPreparedEvent) event);
		}
		if (event instanceof ApplicationPreparedEvent) {
			// 处理应用上下文准备就绪事件
			onApplicationPreparedEvent(event);
		}
	}

	/**
	 * 处理应用运行时环境准备就绪事件
	 */
	private void onApplicationEnvironmentPreparedEvent(
			ApplicationEnvironmentPreparedEvent event) {
		// 加载应用运行时环境的后置处理器
		List<EnvironmentPostProcessor> postProcessors = loadPostProcessors();
		postProcessors.add(this);
		// 排序
		AnnotationAwareOrderComparator.sort(postProcessors);
		for (EnvironmentPostProcessor postProcessor : postProcessors) {
			// 对应用运行时环境进行后置处理
			postProcessor.postProcessEnvironment(event.getEnvironment(),
					event.getSpringApplication());
		}
	}

	/**
	 * 加载应用运行时环境的后置处理器。
	 */
	List<EnvironmentPostProcessor> loadPostProcessors() {
		return SpringFactoriesLoader.loadFactories(EnvironmentPostProcessor.class,
				getClass().getClassLoader());
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		// 对应用运行时环境进行后置处理
		// 添加属性源列表
		addPropertySources(environment, application.getResourceLoader());
		// 配置忽视的bean信息
		configureIgnoreBeanInfo(environment);
		// 绑定到spring应用对象
		bindToSpringApplication(environment, application);
	}

	/**
	 * 配置忽视的bean信息。
	 */
	private void configureIgnoreBeanInfo(ConfigurableEnvironment environment) {
		if (System.getProperty(
				CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME) == null) {
			RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(environment,
					"spring.beaninfo.");
			Boolean ignore = resolver.getProperty("ignore", Boolean.class, Boolean.TRUE);
			// 通过系统属性存储配置
			System.setProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME,
					ignore.toString());
		}
	}

	/**
	 * 处理应用上下文准备就绪事件。
	 */
	private void onApplicationPreparedEvent(ApplicationEvent event) {
		this.logger.replayTo(ConfigFileApplicationListener.class);
		// 添加属性源有序的bean工厂后置处理器
		addPostProcessors(((ApplicationPreparedEvent) event).getApplicationContext());
	}

	/**
	 * Add config file property sources to the specified environment.
	 * 将配置文件属性源列表添加到指定的应用运行时环境。
	 * @param environment the environment to add source to
	 * @param resourceLoader the resource loader
	 * @see #addPostProcessors(ConfigurableApplicationContext)
	 */
	protected void addPropertySources(ConfigurableEnvironment environment,
			ResourceLoader resourceLoader) {
		// 在应用运行时环境的属性源列表的尾部添加系统环境属性源和随机值的属性源
		RandomValuePropertySource.addToEnvironment(environment);
		// 加载候选的属性源并配置活动的配置文件集
		new Loader(environment, resourceLoader).load();
	}

	/**
	 * Bind the environment to the {@link SpringApplication}.
	 * 绑定可配置的应用运行时环境到Spring应用对象。
	 * @param environment the environment to bind
	 * @param application the application to bind to
	 */
	protected void bindToSpringApplication(ConfigurableEnvironment environment,
			SpringApplication application) {
		// 属性集配置工厂
		PropertiesConfigurationFactory<SpringApplication> binder = new PropertiesConfigurationFactory<SpringApplication>(
				application);
		binder.setTargetName("spring.main");
		binder.setConversionService(this.conversionService);
		// 属性源列表
		binder.setPropertySources(environment.getPropertySources());
		try {
			// 绑定属性集到Spring应用对象
			binder.bindPropertiesToTarget();
		}
		catch (BindException ex) {
			throw new IllegalStateException("Cannot bind to SpringApplication", ex);
		}
	}

	/**
	 * Add appropriate post-processors to post-configure the property-sources.
	 * 添加合适的后置处理器以后配置属性源列表。
	 * @param context the context to configure
	 */
	protected void addPostProcessors(ConfigurableApplicationContext context) {
		// 添加属性源有序的bean工厂后置处理器
		context.addBeanFactoryPostProcessor(
				new PropertySourceOrderingPostProcessor(context));
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	// setter

	/**
	 * Set the search locations that will be considered as a comma-separated list. Each
	 * search location should be a directory path (ending in "/") and it will be prefixed
	 * by the file names constructed from {@link #setSearchNames(String) search names} and
	 * profiles (if any) plus file extensions supported by the properties loaders.
	 * Locations are considered in the order specified, with later items taking precedence
	 * (like a map merge).
	 * @param locations the search locations
	 */
	public void setSearchLocations(String locations) {
		Assert.hasLength(locations, "Locations must not be empty");
		this.searchLocations = locations;
	}

	/**
	 * Sets the names of the files that should be loaded (excluding file extension) as a
	 * comma-separated list.
	 * @param names the names to load
	 */
	public void setSearchNames(String names) {
		Assert.hasLength(names, "Names must not be empty");
		this.names = names;
	}

	/**
	 * {@link BeanFactoryPostProcessor} to re-order our property sources below any
	 * {@code @PropertySource} items added by the {@link ConfigurationClassPostProcessor}.
	 * bean工厂后置处理器，对属性源列表重新排序。
	 */
	private class PropertySourceOrderingPostProcessor
			implements BeanFactoryPostProcessor, Ordered {

		/**
		 * 可配置的应用上下文
		 */
		private ConfigurableApplicationContext context;

		PropertySourceOrderingPostProcessor(ConfigurableApplicationContext context) {
			this.context = context;
		}

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE;
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
				throws BeansException {
			// 对属性源列表进行重排序
			reorderSources(this.context.getEnvironment());
		}

		private void reorderSources(ConfigurableEnvironment environment) {
			//
			ConfigurationPropertySources
					.finishAndRelocate(environment.getPropertySources());
			// 默认的属性源列表
			PropertySource<?> defaultProperties = environment.getPropertySources()
					.remove(DEFAULT_PROPERTIES);
			if (defaultProperties != null) {
				environment.getPropertySources().addLast(defaultProperties);
			}
		}

	}

	/**
	 * Loads candidate property sources and configures the active profiles.
	 * 加载候选的属性源并配置活动的配置文件集。
	 */
	private class Loader {

		private final Log logger = ConfigFileApplicationListener.this.logger;

		/**
		 * 可配置的应用运行时环境
		 */
		private final ConfigurableEnvironment environment;

		/**
		 * 资源加载器
		 */
		private final ResourceLoader resourceLoader;

		/**
		 * 属性源列表加载器
		 */
		private PropertySourcesLoader propertiesLoader;

		/**
		 * 配置文件集
		 */
		private Queue<Profile> profiles;

		/**
		 * 已处理的属性源列表
		 */
		private List<Profile> processedProfiles;

		/**
		 * 活动的配置文件集标识
		 */
		private boolean activatedProfiles;

		Loader(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
			this.environment = environment;
			this.resourceLoader = (resourceLoader != null) ? resourceLoader
					: new DefaultResourceLoader();
		}

		public void load() {
			// 属性源列表加载器
			this.propertiesLoader = new PropertySourcesLoader();
			this.activatedProfiles = false;
			this.profiles = Collections.asLifoQueue(new LinkedList<Profile>());
			this.processedProfiles = new LinkedList<Profile>();

			// Pre-existing active profiles set via Environment.setActiveProfiles()
			// are additional profiles and config files are allowed to add more if
			// they want to, so don't call addActiveProfiles() here.
			// 初始化活动的配置文件集
			Set<Profile> initialActiveProfiles = initializeActiveProfiles();
			// 添加未处理的活动的配置文件集
			this.profiles.addAll(getUnprocessedActiveProfiles(initialActiveProfiles));
			if (this.profiles.isEmpty()) {
				for (String defaultProfileName : this.environment.getDefaultProfiles()) {
					// 默认的配置文件
					Profile defaultProfile = new Profile(defaultProfileName, true);
					if (!this.profiles.contains(defaultProfile)) {
						this.profiles.add(defaultProfile);
					}
				}
			}

			// The default profile for these purposes is represented as null. We add it
			// last so that it is first out of the queue (active profiles will then
			// override any settings in the defaults when the list is reversed later).
			this.profiles.add(null);

			while (!this.profiles.isEmpty()) {
				Profile profile = this.profiles.poll();
				for (String location : getSearchLocations()) {
					// 遍历搜索路径列表
					if (!location.endsWith("/")) {
						// location is a filename already, so don't search for more
						// filenames
						// 从搜索路径加载配置文件
						load(location, null, profile);
					}
					else {
						for (String name : getSearchNames()) {
							// 从搜索名称加载配置文件
							load(location, name, profile);
						}
					}
				}
				this.processedProfiles.add(profile);
			}

			// 添加配置属性源列表
			addConfigurationProperties(this.propertiesLoader.getPropertySources());
		}

		/**
		 * 初始化活动的配置文件集。
		 */
		private Set<Profile> initializeActiveProfiles() {
			if (!this.environment.containsProperty(ACTIVE_PROFILES_PROPERTY)
					&& !this.environment.containsProperty(INCLUDE_PROFILES_PROPERTY)) {
				return Collections.emptySet();
			}
			// Any pre-existing active profiles set via property sources (e.g. System
			// properties) take precedence over those added in config files.
			// 绑定spring配置文件集
			SpringProfiles springProfiles = bindSpringProfiles(
					this.environment.getPropertySources());
			// 活动的配置文件集
			Set<Profile> activeProfiles = new LinkedHashSet<Profile>(
					springProfiles.getActiveProfiles());
			activeProfiles.addAll(springProfiles.getIncludeProfiles());
			maybeActivateProfiles(activeProfiles);
			return activeProfiles;
		}

		/**
		 * Return the active profiles that have not been processed yet. If a profile is
		 * enabled via both {@link #ACTIVE_PROFILES_PROPERTY} and
		 * {@link ConfigurableEnvironment#addActiveProfile(String)} it needs to be
		 * filtered so that the {@link #ACTIVE_PROFILES_PROPERTY} value takes precedence.
		 * <p>
		 * Concretely, if the "cloud" profile is enabled via the environment, it will take
		 * less precedence that any profile set via the {@link #ACTIVE_PROFILES_PROPERTY}.
		 * @param initialActiveProfiles the profiles that have been enabled via
		 * {@link #ACTIVE_PROFILES_PROPERTY}
		 * @return the unprocessed active profiles from the environment to enable
		 */
		private List<Profile> getUnprocessedActiveProfiles(
				Set<Profile> initialActiveProfiles) {
			List<Profile> unprocessedActiveProfiles = new ArrayList<Profile>();
			for (String profileName : this.environment.getActiveProfiles()) {
				Profile profile = new Profile(profileName);
				if (!initialActiveProfiles.contains(profile)) {
					unprocessedActiveProfiles.add(profile);
				}
			}
			// Reverse them so the order is the same as from getProfilesForValue()
			// (last one wins when properties are eventually resolved)
			Collections.reverse(unprocessedActiveProfiles);
			return unprocessedActiveProfiles;
		}

		/**
		 * 加载配置属性源。
		 */
		private void load(String location, String name, Profile profile) {
			// 配置分组
			String group = "profile=" + ((profile != null) ? profile : "");
			if (!StringUtils.hasText(name)) {
				// Try to load directly from the location
				// 尝试从指定路径加载配置属性源
				loadIntoGroup(group, location, profile);
			}
			else {
				// Search for a file with the given name
				for (String ext : this.propertiesLoader.getAllFileExtensions()) {
					if (profile != null) {
						// Try the profile-specific file
						// 尝试从配置文件加载配置属性源
						loadIntoGroup(group, location + name + "-" + profile + "." + ext,
								null);
						for (Profile processedProfile : this.processedProfiles) {
							if (processedProfile != null) {
								loadIntoGroup(group, location + name + "-"
										+ processedProfile + "." + ext, profile);
							}
						}
						// Sometimes people put "spring.profiles: dev" in
						// application-dev.yml (gh-340). Arguably we should try and error
						// out on that, but we can be kind and load it anyway.
						loadIntoGroup(group, location + name + "-" + profile + "." + ext,
								profile);
					}
					// Also try the profile-specific section (if any) of the normal file
					loadIntoGroup(group, location + name + "." + ext, profile);
				}
			}
		}

		/**
		 * 从指定路径加载配置属性源。
		 */
		private PropertySource<?> loadIntoGroup(String identifier, String location,
				Profile profile) {
			try {
				// 进行加载配置属性源到指定分组
				return doLoadIntoGroup(identifier, location, profile);
			}
			catch (Exception ex) {
				throw new IllegalStateException(
						"Failed to load property source from location '" + location + "'",
						ex);
			}
		}

		/**
		 * 进行加载配置属性源到指定分组。
		 */
		private PropertySource<?> doLoadIntoGroup(String identifier, String location,
				Profile profile) throws IOException {
			// 资源文件
			Resource resource = this.resourceLoader.getResource(location);
			PropertySource<?> propertySource = null;
			StringBuilder msg = new StringBuilder();
			if (resource != null && resource.exists()) {
				// 应用配置名称
				String name = "applicationConfig: [" + location + "]";
				// 应用配置分组
				String group = "applicationConfig: [" + identifier + "]";
				// 从资源文件加载配置属性源
				propertySource = this.propertiesLoader.load(resource, group, name,
						(profile != null) ? profile.getName() : null);
				if (propertySource != null) {
					// 已加载的配置文件属性源
					msg.append("Loaded ");
					handleProfileProperties(propertySource);
				}
				else {
					msg.append("Skipped (empty) ");
				}
			}
			else {
				msg.append("Skipped ");
			}
			// 配置文件
			msg.append("config file ");
			msg.append(getResourceDescription(location, resource));
			if (profile != null) {
				// 配置环境
				msg.append(" for profile ").append(profile);
			}
			if (resource == null || !resource.exists()) {
				msg.append(" resource not found");
				this.logger.trace(msg);
			}
			else {
				this.logger.debug(msg);
			}
			return propertySource;
		}

		private String getResourceDescription(String location, Resource resource) {
			String resourceDescription = "'" + location + "'";
			if (resource != null) {
				try {
					resourceDescription = String.format("'%s' (%s)",
							resource.getURI().toASCIIString(), location);
				}
				catch (IOException ex) {
					// Use the location as the description
				}
			}
			return resourceDescription;
		}

		private void handleProfileProperties(PropertySource<?> propertySource) {
			// 绑定属性源到spring配置文件集
			SpringProfiles springProfiles = bindSpringProfiles(propertySource);
			maybeActivateProfiles(springProfiles.getActiveProfiles());
			addProfiles(springProfiles.getIncludeProfiles());
		}

		/**
		 * 绑定属性源到spring配置文件集。
		 */
		private SpringProfiles bindSpringProfiles(PropertySource<?> propertySource) {
			// 可变的属性源列表
			MutablePropertySources propertySources = new MutablePropertySources();
			// 在表头添加属性源
			propertySources.addFirst(propertySource);
			// 绑定属性源列表到spring配置文件集
			return bindSpringProfiles(propertySources);
		}

		/**
		 * 绑定属性源列表到spring配置文件集。
		 */
		private SpringProfiles bindSpringProfiles(PropertySources propertySources) {
			// spring配置文件集
			SpringProfiles springProfiles = new SpringProfiles();
			RelaxedDataBinder dataBinder = new RelaxedDataBinder(springProfiles,
					"spring.profiles");
			dataBinder.bind(new PropertySourcesPropertyValues(propertySources, false));
			springProfiles.setActive(resolvePlaceholders(springProfiles.getActive()));
			springProfiles.setInclude(resolvePlaceholders(springProfiles.getInclude()));
			return springProfiles;
		}

		/**
		 * 解析配置的属性值中包含的占位符。
		 */
		private List<String> resolvePlaceholders(List<String> values) {
			List<String> resolved = new ArrayList<String>();
			for (String value : values) {
				resolved.add(this.environment.resolvePlaceholders(value));
			}
			return resolved;
		}

		private void maybeActivateProfiles(Set<Profile> profiles) {
			if (this.activatedProfiles) {
				if (!profiles.isEmpty()) {
					this.logger.debug("Profiles already activated, '" + profiles
							+ "' will not be applied");
				}
				return;
			}
			if (!profiles.isEmpty()) {
				addProfiles(profiles);
				// 活动的配置文件集
				this.logger.debug("Activated profiles "
						+ StringUtils.collectionToCommaDelimitedString(profiles));
				this.activatedProfiles = true;
				removeUnprocessedDefaultProfiles();
			}
		}

		private void removeUnprocessedDefaultProfiles() {
			for (Iterator<Profile> iterator = this.profiles.iterator(); iterator
					.hasNext();) {
				if (iterator.next().isDefaultProfile()) {
					iterator.remove();
				}
			}
		}

		/**
		 * 添加配置文件集。
		 */
		private void addProfiles(Set<Profile> profiles) {
			for (Profile profile : profiles) {
				this.profiles.add(profile);
				// 应用运行时环境中是否包含活动的配置文件
				if (!environmentHasActiveProfile(profile.getName())) {
					// If it's already accepted we assume the order was set
					// intentionally
					// 添加配置文件到表头
					prependProfile(this.environment, profile);
				}
			}
		}

		/**
		 * 应用运行时环境中是否包含活动的配置文件。
		 */
		private boolean environmentHasActiveProfile(String profile) {
			for (String activeProfile : this.environment.getActiveProfiles()) {
				if (activeProfile.equals(profile)) {
					return true;
				}
			}
			return false;
		}

		private void prependProfile(ConfigurableEnvironment environment,
				Profile profile) {
			Set<String> profiles = new LinkedHashSet<String>();
			// 初始化应用运行时环境中的活动的配置文件集
			environment.getActiveProfiles(); // ensure they are initialized
			// But this one should go first (last wins in a property key clash)
			// 添加配置文件到表头
			profiles.add(profile.getName());
			profiles.addAll(Arrays.asList(environment.getActiveProfiles()));
			environment.setActiveProfiles(profiles.toArray(new String[profiles.size()]));
		}

		private Set<String> getSearchLocations() {
			Set<String> locations = new LinkedHashSet<String>();
			// User-configured settings take precedence, so we do them first
			if (this.environment.containsProperty(CONFIG_LOCATION_PROPERTY)) {
				for (String path : asResolvedSet(
						this.environment.getProperty(CONFIG_LOCATION_PROPERTY), null)) {
					if (!path.contains("$")) {
						path = StringUtils.cleanPath(path);
						if (!ResourceUtils.isUrl(path)) {
							path = ResourceUtils.FILE_URL_PREFIX + path;
						}
					}
					locations.add(path);
				}
			}
			locations.addAll(
					asResolvedSet(ConfigFileApplicationListener.this.searchLocations,
							DEFAULT_SEARCH_LOCATIONS));
			return locations;
		}

		private Set<String> getSearchNames() {
			if (this.environment.containsProperty(CONFIG_NAME_PROPERTY)) {
				return asResolvedSet(this.environment.getProperty(CONFIG_NAME_PROPERTY),
						null);
			}
			return asResolvedSet(ConfigFileApplicationListener.this.names, DEFAULT_NAMES);
		}

		private Set<String> asResolvedSet(String value, String fallback) {
			List<String> list = Arrays.asList(StringUtils.trimArrayElements(
					StringUtils.commaDelimitedListToStringArray((value != null)
							? this.environment.resolvePlaceholders(value) : fallback)));
			// 反转列表
			Collections.reverse(list);
			// 去重
			return new LinkedHashSet<String>(list);
		}

		/**
		 * 添加可变的配置属性源列表。
		 */
		private void addConfigurationProperties(MutablePropertySources sources) {
			List<PropertySource<?>> reorderedSources = new ArrayList<PropertySource<?>>();
			for (PropertySource<?> item : sources) {
				reorderedSources.add(item);
			}
			// 添加配置属性源列表
			addConfigurationProperties(
					new ConfigurationPropertySources(reorderedSources));
		}

		/**
		 * 添加配置属性源列表。
		 */
		private void addConfigurationProperties(
				ConfigurationPropertySources configurationSources) {
			MutablePropertySources existingSources = this.environment
					.getPropertySources();
			if (existingSources.contains(DEFAULT_PROPERTIES)) {
				// 在默认的配置属性源之前添加
				existingSources.addBefore(DEFAULT_PROPERTIES, configurationSources);
			}
			else {
				// 添加到表尾
				existingSources.addLast(configurationSources);
			}
		}

	}

	/**
	 * 配置文件
	 */
	private static class Profile {

		/**
		 * 配置文件名称
		 */
		private final String name;

		/**
		 * 是否为默认的配置文件
		 */
		private final boolean defaultProfile;

		Profile(String name) {
			this(name, false);
		}

		Profile(String name, boolean defaultProfile) {
			Assert.notNull(name, "Name must not be null");
			this.name = name;
			this.defaultProfile = defaultProfile;
		}

		public String getName() {
			return this.name;
		}

		public boolean isDefaultProfile() {
			return this.defaultProfile;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null || obj.getClass() != getClass()) {
				return false;
			}
			return ((Profile) obj).name.equals(this.name);
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		@Override
		public String toString() {
			return this.name;
		}

	}

	/**
	 * Holds the configuration {@link PropertySource}s as they are loaded can relocate
	 * them once configuration classes have been processed.
	 * 配置属性源列表。
	 */
	static class ConfigurationPropertySources
			extends EnumerablePropertySource<Collection<PropertySource<?>>> {

		/**
		 * 配置属性源容器
		 */
		private final Collection<PropertySource<?>> sources;

		/**
		 * 配置属性源名称容器
		 */
		private final String[] names;

		ConfigurationPropertySources(Collection<PropertySource<?>> sources) {
			super(APPLICATION_CONFIGURATION_PROPERTY_SOURCE_NAME, sources);
			this.sources = sources;
			List<String> names = new ArrayList<String>();
			for (PropertySource<?> source : sources) {
				if (source instanceof EnumerablePropertySource) {
					names.addAll(Arrays.asList(
							((EnumerablePropertySource<?>) source).getPropertyNames()));
				}
			}
			this.names = names.toArray(new String[names.size()]);
		}

		@Override
		public Object getProperty(String name) {
			for (PropertySource<?> propertySource : this.sources) {
				Object value = propertySource.getProperty(name);
				if (value != null) {
					return value;
				}
			}
			return null;
		}

		public static void finishAndRelocate(MutablePropertySources propertySources) {
			String name = APPLICATION_CONFIGURATION_PROPERTY_SOURCE_NAME;
			ConfigurationPropertySources removed = (ConfigurationPropertySources) propertySources
					.get(name);
			if (removed != null) {
				for (PropertySource<?> propertySource : removed.sources) {
					if (propertySource instanceof EnumerableCompositePropertySource) {
						EnumerableCompositePropertySource composite = (EnumerableCompositePropertySource) propertySource;
						for (PropertySource<?> nested : composite.getSource()) {
							propertySources.addAfter(name, nested);
							name = nested.getName();
						}
					}
					else {
						propertySources.addAfter(name, propertySource);
					}
				}
				propertySources.remove(APPLICATION_CONFIGURATION_PROPERTY_SOURCE_NAME);
			}
		}

		@Override
		public String[] getPropertyNames() {
			return this.names;
		}

	}

	/**
	 * Holder for {@code spring.profiles} properties.
	 * Spring配置文件集。
	 */
	static final class SpringProfiles {

		/**
		 * 活动的配置文件集
		 */
		private List<String> active = new ArrayList<String>();

		/**
		 * 包含的配置文件集
		 */
		private List<String> include = new ArrayList<String>();

		public List<String> getActive() {
			return this.active;
		}

		public void setActive(List<String> active) {
			this.active = active;
		}

		public List<String> getInclude() {
			return this.include;
		}

		public void setInclude(List<String> include) {
			this.include = include;
		}

		Set<Profile> getActiveProfiles() {
			return asProfileSet(this.active);
		}

		Set<Profile> getIncludeProfiles() {
			return asProfileSet(this.include);
		}

		private Set<Profile> asProfileSet(List<String> profileNames) {
			List<Profile> profiles = new ArrayList<Profile>();
			for (String profileName : profileNames) {
				profiles.add(new Profile(profileName));
			}
			// 反转
			Collections.reverse(profiles);
			return new LinkedHashSet<Profile>(profiles);
		}

	}

}
