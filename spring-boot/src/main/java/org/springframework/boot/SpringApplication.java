
package org.springframework.boot;

import java.lang.reflect.Constructor;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.diagnostics.FailureAnalyzers;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * Classes that can be used to bootstrap and launch a Spring application from a Java main
 * method. By default class will perform the following steps to bootstrap your
 * application:
 * 用于从main方法引导和启动Spring应用的类。
 * 默认情况下，这个类将执行以下步骤来引导您的应用：
 *
 * <ul>
 * <li>Create an appropriate {@link ApplicationContext} instance (depending on your
 * classpath)</li>
 * 创建一个适当的应用上下文实例(取决于您的类路径)
 * <li>Register a {@link CommandLinePropertySource} to expose command line arguments as
 * Spring properties</li>
 * 注册一个命令行属性源以将命令行参数暴露为Spring属性
 * <li>Refresh the application context, loading all singleton beans</li>
 * 刷新应用上下文，加载所有单例beans
 * <li>Trigger any {@link CommandLineRunner} beans</li>
 * 触发任何的命令行运行器beans
 * </ul>
 *
 * In most circumstances the static {@link #run(Object, String[])} method can be called
 * directly from your {@literal main} method to bootstrap your application:
 * 在大多数情况下，可以直接从您的main方法中调用静态的{@link #run(Object, String[])}方法来引导您的应用：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAutoConfiguration
 * public class MyApplication  {
 *
 *   // ... Bean definitions
 *
 *   public static void main(String[] args) throws Exception {
 *     SpringApplication.run(MyApplication.class, args);
 *   }
 * }
 * </pre>
 *
 * <p>
 * For more advanced configuration a {@link SpringApplication} instance can be created and
 * customized before being run:
 * 对于更高级的配置，可以在运行之前创建和定制{@link SpringApplication}实例：
 *
 * <pre class="code">
 * public static void main(String[] args) throws Exception {
 *   SpringApplication app = new SpringApplication(MyApplication.class);
 *   // ... customize app settings here
 *   app.run(args)
 * }
 * </pre>
 *
 * {@link SpringApplication}s can read beans from a variety of different sources. It is
 * generally recommended that a single {@code @Configuration} class is used to bootstrap
 * your application, however, any of the following sources can also be used:
 * Spring应用可以从各种不同的来源读取beans。
 * 通常建议，使用单个@Configuration配置类来引导您的应用，但是，也可以使用以下任何来源：
 *
 * <ul>
 * <li>{@link Class} - A Java class to be loaded by {@link AnnotatedBeanDefinitionReader}
 * </li>
 * 配置类 - 由{@link AnnotatedBeanDefinitionReader}加载的Java类
 * <li>{@link Resource} - An XML resource to be loaded by {@link XmlBeanDefinitionReader},
 * or a groovy script to be loaded by {@link GroovyBeanDefinitionReader}</li>
 * 资源文件 - 由{@link XmlBeanDefinitionReader}加载的XML资源，或由{@link GroovyBeanDefinitionReader}加载的groovy脚本
 * <li>{@link Package} - A Java package to be scanned by
 * {@link ClassPathBeanDefinitionScanner}</li>
 * 包路径 - 由{@link ClassPathBeanDefinitionScanner}扫描的Java包
 * <li>{@link CharSequence} - A class name, resource handle or package name to loaded as
 * appropriate. If the {@link CharSequence} cannot be resolved to class and does not
 * resolve to a {@link Resource} that exists it will be considered a {@link Package}.</li>
 * 字符序列 - 要加载的类名，资源句柄或包名。如果字符序列无法解析为类，并且无法解析为存在的资源，则将其视为Java包。
 * </ul>
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Christian Dupuis
 * @author Stephane Nicoll
 * @author Jeremy Rickard
 * @author Craig Burke
 * @author Michael Simons
 * @author Ethan Rubinson
 * @see #run(Object, String[])
 * @see #run(Object[], String[])
 * @see #SpringApplication(Object...)
 */
public class SpringApplication {

	// 应用上下文的类名

	/**
	 * The class name of application context that will be used by default for non-web
	 * environments.
	 * 默认的非web环境的应用上下文的类名。
	 * AnnotationConfigApplicationContext：注解配置的应用上下文
	 */
	public static final String DEFAULT_CONTEXT_CLASS = "org.springframework.context."
			+ "annotation.AnnotationConfigApplicationContext";

	/**
	 * The class name of application context that will be used by default for web
	 * environments.
	 * 默认的web环境的应用上下文的类名。
	 * AnnotationConfigEmbeddedWebApplicationContext：注解配置的嵌入式的web应用上下文
	 */
	public static final String DEFAULT_WEB_CONTEXT_CLASS = "org.springframework."
			+ "boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext";

	/**
	 * web应用环境的类名列表。
	 * ConfigurableWebApplicationContext：可配置的web应用上下文
	 */
	private static final String[] WEB_ENVIRONMENT_CLASSES = { "javax.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext" };

	/**
	 * Default banner location.
	 * 默认的横幅文件位置
	 */
	public static final String BANNER_LOCATION_PROPERTY_VALUE = SpringApplicationBannerPrinter.DEFAULT_BANNER_LOCATION;

	/**
	 * Banner location property key.
	 * 横幅文件位置的属性键
	 */
	public static final String BANNER_LOCATION_PROPERTY = SpringApplicationBannerPrinter.BANNER_LOCATION_PROPERTY;

	private static final String SYSTEM_PROPERTY_JAVA_AWT_HEADLESS = "java.awt.headless";

	private static final Log logger = LogFactory.getLog(SpringApplication.class);

	/**
	 * bean资源对象集合
	 */
	private final Set<Object> sources = new LinkedHashSet<Object>();

	/**
	 * 定义main入口方法的应用主类
	 */
	private Class<?> mainApplicationClass;

	private Banner.Mode bannerMode = Banner.Mode.CONSOLE;

	/**
	 * 记录应用启动信息的开关
	 */
	private boolean logStartupInfo = true;

	/**
	 * 添加命令行参数的属性源
	 */
	private boolean addCommandLineProperties = true;

	/**
	 * 以编程方式输出横幅
	 */
	private Banner banner;

	/**
	 * 资源加载器
	 */
	private ResourceLoader resourceLoader;

	/**
	 * bean名称生成器
	 */
	private BeanNameGenerator beanNameGenerator;

	/**
	 * 可配置的应用运行时环境
	 */
	private ConfigurableEnvironment environment;

	/**
	 * 可配置的应用上下文类
	 */
	private Class<? extends ConfigurableApplicationContext> applicationContextClass;

	/**
	 * web应用环境标识
	 */
	private boolean webEnvironment;

	private boolean headless = true;

	/**
	 * 注册JVM关闭钩子的开关
	 */
	private boolean registerShutdownHook = true;

	/**
	 * 应用上下文初始化器列表
	 */
	private List<ApplicationContextInitializer<?>> initializers;

	/**
	 * 应用监视器列表
	 */
	private List<ApplicationListener<?>> listeners;

	/**
	 * 默认属性配置的属性源
	 */
	private Map<String, Object> defaultProperties;

	/**
	 * 其他配置文件集
	 */
	private Set<String> additionalProfiles = new HashSet<String>();

	// 创建Spring应用

	/**
	 * Create a new {@link SpringApplication} instance. The application context will load
	 * beans from the specified sources (see {@link SpringApplication class-level}
	 * documentation for details). The instance can be customized before calling
	 * {@link #run(String...)}.
	 * 创建一个新的Spring应用实例。应用上下文会从指定的属性源列表加载beans。
	 * 可以在调用{@link #run(String...)}之前自定义这个实例。
	 * @param sources the bean sources bean资源对象列表
	 * @see #run(Object, String[])
	 * @see #SpringApplication(ResourceLoader, Object...)
	 */
	public SpringApplication(Object... sources) {
		// 初始化应用
		initialize(sources);
	}

	/**
	 * Create a new {@link SpringApplication} instance. The application context will load
	 * beans from the specified sources (see {@link SpringApplication class-level}
	 * documentation for details). The instance can be customized before calling
	 * {@link #run(String...)}.
	 * @param resourceLoader the resource loader to use
	 * @param sources the bean sources
	 * @see #run(Object, String[])
	 * @see #SpringApplication(ResourceLoader, Object...)
	 */
	public SpringApplication(ResourceLoader resourceLoader, Object... sources) {
		this.resourceLoader = resourceLoader;
		initialize(sources);
	}

	// 初始化Spring应用

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initialize(Object[] sources) {
		if (sources != null && sources.length > 0) {
			this.sources.addAll(Arrays.asList(sources));
		}
		// 1.推导web应用环境
		this.webEnvironment = deduceWebEnvironment();
		// 2.创建所有应用上下文初始化器的实例列表
		setInitializers((Collection) getSpringFactoriesInstances(
				ApplicationContextInitializer.class));
		// 3.创建所有应用监视器的实例列表
		setListeners((Collection) getSpringFactoriesInstances(
				ApplicationListener.class));
		// 4.推导应用主类
		this.mainApplicationClass = deduceMainApplicationClass();
	}

	/**
	 * 推导web应用环境。
	 */
	private boolean deduceWebEnvironment() {
		// 遍历web应用环境的类名列表，所有类都能被加载到才算满足
		for (String className : WEB_ENVIRONMENT_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 推导应用主类，其定义main入口方法。
	 */
	private Class<?> deduceMainApplicationClass() {
		try {
			// 应用主线程(main线程)的堆栈跟踪
			StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				// 搜索main方法
				if ("main".equals(stackTraceElement.getMethodName())) {
					// 返回定义main入口方法的类对象
					return Class.forName(stackTraceElement.getClassName());
				}
			}
		}
		catch (ClassNotFoundException ex) {
			// Swallow and continue
		}
		return null;
	}

	// 运行Spring应用

	/**
	 * Run the Spring application, creating and refreshing a new
	 * {@link ApplicationContext}.
	 * 运行Spring应用，创建和刷新一个新的应用上下文。
	 *
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return a running {@link ApplicationContext}
	 */
	public ConfigurableApplicationContext run(String... args) {
		// 秒表，记录应用启动完成耗时
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		// 可配置的应用上下文
		ConfigurableApplicationContext context = null;
		// 故障分析器列表
		FailureAnalyzers analyzers = null;
		configureHeadlessProperty();
		// 1.获取Spring应用运行各阶段监视器列表实例
		SpringApplicationRunListeners listeners = getRunListeners(args);
		// 2.发布"首次启动run方法"事件，通知对象是所有Spring应用运行各阶段监视器
		listeners.starting();
		try {
			// Spring应用参数列表
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(
					args);
			// 3.准备可配置的应用运行时环境
			ConfigurableEnvironment environment = prepareEnvironment(listeners,
					applicationArguments);
			// 4.打印横幅
			Banner printedBanner = printBanner(environment);
			// 5.创建可配置的应用上下文
			context = createApplicationContext();
			// 故障分析器列表
			analyzers = new FailureAnalyzers(context);
			// 6.准备可配置的应用上下文
			prepareContext(context, environment, listeners, applicationArguments,
					printedBanner);
			// 7.刷新可配置的应用上下文
			refreshContext(context);
			// 8.在刷新应用上下文后调用
			afterRefresh(context, applicationArguments);
			// 9.发布"run方法完成"事件
			listeners.finished(context, null);
			stopWatch.stop();
			if (this.logStartupInfo) {
				// 10.记录应用的启动信息
				new StartupInfoLogger(this.mainApplicationClass)
						.logStarted(getApplicationLog(), stopWatch);
			}
			return context;
		}
		catch (Throwable ex) {
			// 处理运行故障
			handleRunFailure(context, listeners, analyzers, ex);
			// 抛出非法状态运行时异常
			throw new IllegalStateException(ex);
		}
	}

	// 准备阶段

	/**
	 * 准备可配置的应用运行时环境。
	 */
	private ConfigurableEnvironment prepareEnvironment(
			SpringApplicationRunListeners listeners,
			ApplicationArguments applicationArguments) {
		// Create and configure the environment
		// 1.创建和配置应用运行时环境
		ConfigurableEnvironment environment = getOrCreateEnvironment();
		configureEnvironment(environment, applicationArguments.getSourceArgs());
		// 2.发布"应用运行时环境已准备好"事件
		listeners.environmentPrepared(environment);
		if (!this.webEnvironment) {
			// 对于非web应用环境，转换为标准的应用环境
			environment = new EnvironmentConverter(getClassLoader())
					.convertToStandardEnvironmentIfNecessary(environment);
		}
		return environment;
	}

	private void prepareContext(ConfigurableApplicationContext context,
			ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
			ApplicationArguments applicationArguments, Banner printedBanner) {
		// 1.设置应用运行时环境
		context.setEnvironment(environment);
		// 2.后置处理可配置的应用上下文
		postProcessApplicationContext(context);
		// 3.在应用上下文刷新前，应用所有应用上下文初始化器
		applyInitializers(context);
		// 4.发布"应用上下文已创建并准备好"事件
		listeners.contextPrepared(context);
		// 5.记录应用启动信息
		if (this.logStartupInfo) {
			// 记录应用的启动信息
			logStartupInfo(context.getParent() == null);
			// 记录活动的配置文件信息
			logStartupProfileInfo(context);
		}

		// Add boot specific singleton beans
		// 6.添加引导特定的单例beans
		// Spring应用参数列表
		context.getBeanFactory().registerSingleton("springApplicationArguments",
				applicationArguments);
		if (printedBanner != null) {
			// Spring引导横幅
			context.getBeanFactory().registerSingleton("springBootBanner", printedBanner);
		}

		// Load the sources
		// 7.加载bean资源对象集合到应用上下文中
		Set<Object> sources = getSources();
		Assert.notEmpty(sources, "Sources must not be empty");
		load(context, sources.toArray(new Object[sources.size()]));
		// 8.发布"应用上下文已加载"事件
		listeners.contextLoaded(context);
	}

	// 刷新阶段

	private void refreshContext(ConfigurableApplicationContext context) {
		// 1.刷新底层的应用上下文
		refresh(context);
		if (this.registerShutdownHook) {
			try {
				// 2.注册JVM关闭钩子(应用优雅关闭场景)
				// 调用AbstractApplicationContext.doClose()执行应用上下文关闭
				context.registerShutdownHook();
			}
			catch (AccessControlException ex) {
				// Not allowed in some environments.
			}
		}
	}

	private void configureHeadlessProperty() {
		// 使用JVM系统属性存储配置属性
		System.setProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, System.getProperty(
				SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, Boolean.toString(this.headless)));
	}

	/**
	 * 返回Spring应用运行各阶段监视器列表。
	 */
	private SpringApplicationRunListeners getRunListeners(String[] args) {
		// 构造函数的参数类型列表
		Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
		// 创建Spring应用运行各阶段监视器列表实例
		return new SpringApplicationRunListeners(logger, getSpringFactoriesInstances(
				SpringApplicationRunListener.class, types, this, args));
	}

	// Spring工厂加载机制

	private <T> Collection<? extends T> getSpringFactoriesInstances(Class<T> type) {
		return getSpringFactoriesInstances(type, new Class<?>[] {});
	}

	/**
	 * 获取由Spring工厂加载的指定类型的实例列表。
	 */
	private <T> Collection<? extends T> getSpringFactoriesInstances(Class<T> type,
			Class<?>[] parameterTypes, Object... args) {
		// 1.获取当前执行线程上下文的类加载器
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		// Use names and ensure unique to protect against duplicates
		// 使用名称并确保唯一以防止重复
		// 2.使用Spring工厂加载器从"META-INF/spring.factories"加载给定类型的全路径工厂类名列表
		Set<String> names = new LinkedHashSet<String>(
				SpringFactoriesLoader.loadFactoryNames(type, classLoader));
		// 3.创建所有工厂类名的实例列表
		List<T> instances = createSpringFactoriesInstances(type, parameterTypes,
				classLoader, args, names);
		// 4.升序排序
		AnnotationAwareOrderComparator.sort(instances);
		return instances;
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> createSpringFactoriesInstances(Class<T> type,
			Class<?>[] parameterTypes, ClassLoader classLoader, Object[] args,
			Set<String> names) {
		// 工厂类型的实例列表
		List<T> instances = new ArrayList<T>(names.size());
		for (String name : names) {
			try {
				// 1.创建工厂类名的类实例
				Class<?> instanceClass = ClassUtils.forName(name, classLoader);
				// 2.父子类校验
				Assert.isAssignable(type, instanceClass);
				// 3.获取指定参数类型列表的构造函数
				Constructor<?> constructor = instanceClass
						.getDeclaredConstructor(parameterTypes);
				// 4.使用构造函数实例化类型(Constructor#newInstance)
				T instance = (T) BeanUtils.instantiateClass(constructor, args);
				instances.add(instance);
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException(
						"Cannot instantiate " + type + " : " + name, ex);
			}
		}
		return instances;
	}

	// 可配置的应用运行时环境

	private ConfigurableEnvironment getOrCreateEnvironment() {
		if (this.environment != null) {
			return this.environment;
		}
		if (this.webEnvironment) {
			return new StandardServletEnvironment();
		}
		return new StandardEnvironment();
	}

	/**
	 * Template method delegating to
	 * {@link #configurePropertySources(ConfigurableEnvironment, String[])} and
	 * {@link #configureProfiles(ConfigurableEnvironment, String[])} in that order.
	 * Override this method for complete control over Environment customization, or one of
	 * the above for fine-grained control over property sources or profiles, respectively.
	 * 以该顺序委派给配置属性源列表和配置文件集。
	 * 重写这个方法以完全控制环境自定义，或者重写上述方法之一以分别对属性源列表或配置文件集进行细粒度控制。
	 * @param environment this application's environment
	 * @param args arguments passed to the {@code run} method
	 * @see #configureProfiles(ConfigurableEnvironment, String[])
	 * @see #configurePropertySources(ConfigurableEnvironment, String[])
	 */
	protected void configureEnvironment(ConfigurableEnvironment environment,
			String[] args) {
		// 1.配置属性源列表
		configurePropertySources(environment, args);
		// 2.配置文件集
		configureProfiles(environment, args);
	}

	/**
	 * Add, remove or re-order any {@link PropertySource}s in this application's
	 * environment.
	 * 在这个应用运行时环境中添加，删除或重新排序任何的属性源列表。
	 * @param environment this application's environment
	 * @param args arguments passed to the {@code run} method
	 * @see #configureEnvironment(ConfigurableEnvironment, String[])
	 */
	protected void configurePropertySources(ConfigurableEnvironment environment,
			String[] args) {
		// 可变的属性源列表
		MutablePropertySources sources = environment.getPropertySources();
		if (this.defaultProperties != null && !this.defaultProperties.isEmpty()) {
			// 在表尾添加默认属性配置的属性源(配置兜底方案)
			sources.addLast(
					new MapPropertySource("defaultProperties", this.defaultProperties));
		}
		if (this.addCommandLineProperties && args.length > 0) {
			// 命令行参数的属性源
			String name = CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME;
			if (sources.contains(name)) {
				// 属性源
				PropertySource<?> source = sources.get(name);
				// 组合的属性源
				CompositePropertySource composite = new CompositePropertySource(name);
				composite.addPropertySource(new SimpleCommandLinePropertySource(
						name + "-" + args.hashCode(), args));
				composite.addPropertySource(source);
				sources.replace(name, composite);
			}
			else {
				// 在表头添加命令行参数的属性源
				sources.addFirst(new SimpleCommandLinePropertySource(args));
			}
		}
	}

	/**
	 * Configure which profiles are active (or active by default) for this application
	 * environment. Additional profiles may be activated during configuration file
	 * processing via the {@code spring.profiles.active} property.
	 * 为这个应用运行时环境配置活动的配置文件集。
	 * 额外的配置文件集可以在配置文件处理过程中经由spring.profiles.active属性被激活。
	 * @param environment this application's environment
	 * @param args arguments passed to the {@code run} method
	 * @see #configureEnvironment(ConfigurableEnvironment, String[])
	 * @see org.springframework.boot.context.config.ConfigFileApplicationListener
	 */
	protected void configureProfiles(ConfigurableEnvironment environment, String[] args) {
		// 确保应用运行时环境已初始化
		environment.getActiveProfiles(); // ensure they are initialized
		// But these ones should go first (last wins in a property key clash)
		// 但是这些应该排在最前面(在属性键冲突中最后获胜)
		Set<String> profiles = new LinkedHashSet<String>(this.additionalProfiles);
		profiles.addAll(Arrays.asList(environment.getActiveProfiles()));
		environment.setActiveProfiles(profiles.toArray(new String[profiles.size()]));
	}

	// 横幅

	private Banner printBanner(ConfigurableEnvironment environment) {
		if (this.bannerMode == Banner.Mode.OFF) {
			return null;
		}
		// 资源加载器
		ResourceLoader resourceLoader = (this.resourceLoader != null)
				? this.resourceLoader : new DefaultResourceLoader(getClassLoader());
		// Spring应用横幅打印器
		SpringApplicationBannerPrinter bannerPrinter = new SpringApplicationBannerPrinter(
				resourceLoader, this.banner);
		if (this.bannerMode == Mode.LOG) {
			return bannerPrinter.print(environment, this.mainApplicationClass, logger);
		}
		return bannerPrinter.print(environment, this.mainApplicationClass, System.out);
	}

	// 可配置的应用上下文-创建阶段

	/**
	 * Strategy method used to create the {@link ApplicationContext}. By default this
	 * method will respect any explicitly set application context or application context
	 * class before falling back to a suitable default.
	 * 用于创建应用上下文的策略方法。
	 * @return the application context (not yet refreshed) 尚未刷新的可配置的应用上下文
	 * @see #setApplicationContextClass(Class)
	 */
	protected ConfigurableApplicationContext createApplicationContext() {
		Class<?> contextClass = this.applicationContextClass;
		if (contextClass == null) {
			try {
				// 1.加载并创建应用上下文类对象
				contextClass = Class.forName(this.webEnvironment
						? DEFAULT_WEB_CONTEXT_CLASS : DEFAULT_CONTEXT_CLASS);
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalStateException(
						"Unable create a default ApplicationContext, "
								+ "please specify an ApplicationContextClass",
						ex);
			}
		}
		// 2.使用反射创建可配置的应用上下文(Class#newInstance)
		return (ConfigurableApplicationContext) BeanUtils.instantiate(contextClass);
	}

	// 可配置的应用上下文-准备阶段

	/**
	 * Apply any relevant post processing the {@link ApplicationContext}. Subclasses can
	 * apply additional processing as required.
	 * 应用任何相关的后置处理到应用上下文。子类可以根据需要应用其他处理。
	 * @param context the application context
	 */
	protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
		if (this.beanNameGenerator != null) {
			// 注册内部的配置bean名称生成器
			context.getBeanFactory().registerSingleton(
					AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR,
					this.beanNameGenerator);
		}
		if (this.resourceLoader != null) {
			// 设置资源加载器
			if (context instanceof GenericApplicationContext) {
				((GenericApplicationContext) context)
						.setResourceLoader(this.resourceLoader);
			}
			if (context instanceof DefaultResourceLoader) {
				// 设置资源加载器使用的类加载器，以加载类路径资源
				((DefaultResourceLoader) context)
						.setClassLoader(this.resourceLoader.getClassLoader());
			}
		}
	}

	/**
	 * Apply any {@link ApplicationContextInitializer}s to the context before it is
	 * refreshed.
	 * 在应用上下文刷新前，应用所有应用上下文初始化器。
	 * @param context the configured ApplicationContext (not refreshed yet) 尚未刷新的可配置的应用上下文
	 * @see ConfigurableApplicationContext#refresh()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void applyInitializers(ConfigurableApplicationContext context) {
		// 应用上下文初始化器列表
		for (ApplicationContextInitializer initializer : getInitializers()) {
			// 应用上下文初始化器的类型参数
			Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(
					initializer.getClass(), ApplicationContextInitializer.class);
			Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
			// 初始化应用上下文
			initializer.initialize(context);
		}
	}

	/**
	 * Called to log startup information, subclasses may override to add additional
	 * logging.
	 * 记录应用的启动信息，子类可以重写以添加其他日志记录。
	 * @param isRoot true if this application is the root of a context hierarchy
	 */
	protected void logStartupInfo(boolean isRoot) {
		if (isRoot) {
			// 应用上下文层次结构的根，才会输出日志
			new StartupInfoLogger(this.mainApplicationClass)
					.logStarting(getApplicationLog());
		}
	}

	/**
	 * Called to log active profile information.
	 * 记录活动的配置文件信息。
	 * @param context the application context
	 */
	protected void logStartupProfileInfo(ConfigurableApplicationContext context) {
		Log log = getApplicationLog();
		if (log.isInfoEnabled()) {
			// 活动的配置文件集
			String[] activeProfiles = context.getEnvironment().getActiveProfiles();
			if (ObjectUtils.isEmpty(activeProfiles)) {
				// 默认的配置文件集
				String[] defaultProfiles = context.getEnvironment().getDefaultProfiles();
				// 未设置活动的配置文件集，退回到默认的配置文件集
				log.info("No active profile set, falling back to default profiles: "
						+ StringUtils.arrayToCommaDelimitedString(defaultProfiles));
			}
			else {
				// 以下配置文件集处于活动状态
				log.info("The following profiles are active: "
						+ StringUtils.arrayToCommaDelimitedString(activeProfiles));
			}
		}
	}

	/**
	 * Returns the {@link Log} for the application. By default will be deduced.
	 * 返回应用程序的日志记录器。默认情况下将被推导。
	 * @return the application log
	 */
	protected Log getApplicationLog() {
		if (this.mainApplicationClass == null) {
			// SpringApplication类的日志记录器
			return logger;
		}
		// 应用主类的日志记录器
		return LogFactory.getLog(this.mainApplicationClass);
	}

	// 可配置的应用上下文-加载阶段

	/**
	 * Load beans into the application context.
	 * 加载beans到应用上下文中。
	 * @param context the context to load beans into
	 * @param sources the sources to load
	 */
	protected void load(ApplicationContext context, Object[] sources) {
		if (logger.isDebugEnabled()) {
			// 加载bean资源
			logger.debug(
					"Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
		}
		// bean定义加载器
		BeanDefinitionLoader loader = createBeanDefinitionLoader(
				getBeanDefinitionRegistry(context), sources);
		if (this.beanNameGenerator != null) {
			loader.setBeanNameGenerator(this.beanNameGenerator);
		}
		if (this.resourceLoader != null) {
			loader.setResourceLoader(this.resourceLoader);
		}
		if (this.environment != null) {
			loader.setEnvironment(this.environment);
		}
		// 加载bean资源对象集合到bean定义加载器
		loader.load();
	}

	/**
	 * The ResourceLoader that will be used in the ApplicationContext.
	 * 将在应用上下文中使用的资源加载器。
	 * @return the resourceLoader the resource loader that will be used in the
	 * ApplicationContext (or null if the default)
	 */
	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	/**
	 * Either the ClassLoader that will be used in the ApplicationContext (if
	 * {@link #setResourceLoader(ResourceLoader) resourceLoader} is set, or the context
	 * class loader (if not null), or the loader of the Spring {@link ClassUtils} class.
	 * 将在应用上下文中使用的类加载器，或者当前线程上下文的类加载器，
	 * 或者ClassUtils类的类加载器，或者系统类加载器/引导类加载器。
	 * @return a ClassLoader (never null)
	 */
	public ClassLoader getClassLoader() {
		if (this.resourceLoader != null) {
			// 资源加载器使用的类加载器
			return this.resourceLoader.getClassLoader();
		}
		// 返回默认的类加载器，通常是线程上下文的类加载器
		return ClassUtils.getDefaultClassLoader();
	}

	// bean定义注册表

	/**
	 * Get the bean definition registry.
	 * 获取bean定义注册表。
	 * @param context the application context
	 * @return the BeanDefinitionRegistry if it can be determined
	 */
	private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
		if (context instanceof BeanDefinitionRegistry) {
			// 应用上下文是bean定义注册表的子类(GenericApplicationContext)
			// AnnotationConfigEmbeddedWebApplicationContext->EmbeddedWebApplicationContext->GenericWebApplicationContext->GenericApplicationContext
			return (BeanDefinitionRegistry) context;
		}
		if (context instanceof AbstractApplicationContext) {
			// 应用上下文的核心框架实现
			// DefaultListableBeanFactory是bean定义注册表的子类
			return (BeanDefinitionRegistry) ((AbstractApplicationContext) context)
					.getBeanFactory();
		}
		throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
	}

	// bean定义加载器

	/**
	 * Factory method used to create the {@link BeanDefinitionLoader}.
	 * 创建bean定义加载器的工厂方法。
	 * @param registry the bean definition registry bean定义注册表
	 * @param sources the sources to load 加载的bean对象资源容器
	 * @return the {@link BeanDefinitionLoader} that will be used to load beans
	 */
	protected BeanDefinitionLoader createBeanDefinitionLoader(
			BeanDefinitionRegistry registry, Object[] sources) {
		return new BeanDefinitionLoader(registry, sources);
	}

	// 可配置的应用上下文-刷新阶段

	/**
	 * Refresh the underlying {@link ApplicationContext}.
	 * 刷新底层的应用上下文。
	 * @param applicationContext the application context to refresh
	 */
	protected void refresh(ApplicationContext applicationContext) {
		// 校验应用上下文是否为应用上下文核心框架实现类的子类
		Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
		// 调用AbstractApplicationContext.refresh()加载或刷新配置的持久表示
		((AbstractApplicationContext) applicationContext).refresh();
	}

	// 可配置的应用上下文-刷新后阶段

	/**
	 * Called after the context has been refreshed.
	 * 在刷新应用上下文后调用。
	 * @param context the application context 刷新完成的可配置的应用上下文
	 * @param args the application arguments
	 */
	protected void afterRefresh(ConfigurableApplicationContext context,
			ApplicationArguments args) {
		// 调用运行器列表
		callRunners(context, args);
	}

	/**
	 * 调用运行器列表。
	 */
	private void callRunners(ApplicationContext context, ApplicationArguments args) {
		// 运行器列表，包含应用运行器和命令行运行器
		List<Object> runners = new ArrayList<Object>();
		runners.addAll(context.getBeansOfType(ApplicationRunner.class).values());
		runners.addAll(context.getBeansOfType(CommandLineRunner.class).values());
		// 使用顺序比较器排序
		AnnotationAwareOrderComparator.sort(runners);
		for (Object runner : new LinkedHashSet<Object>(runners)) {
			if (runner instanceof ApplicationRunner) {
				// 调用应用运行器
				callRunner((ApplicationRunner) runner, args);
			}
			if (runner instanceof CommandLineRunner) {
				// 调用命令行运行器
				callRunner((CommandLineRunner) runner, args);
			}
		}
	}

	/**
	 * 调用应用运行器。
	 */
	private void callRunner(ApplicationRunner runner, ApplicationArguments args) {
		try {
			runner.run(args);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to execute ApplicationRunner", ex);
		}
	}

	/**
	 * 调用命令行运行器。
	 */
	private void callRunner(CommandLineRunner runner, ApplicationArguments args) {
		try {
			runner.run(args.getSourceArgs());
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to execute CommandLineRunner", ex);
		}
	}

	/**
	 * 处理运行故障。
	 */
	private void handleRunFailure(ConfigurableApplicationContext context,
			SpringApplicationRunListeners listeners, FailureAnalyzers analyzers,
			Throwable exception) {
		try {
			try {
				// 处理退出代码
				handleExitCode(context, exception);
				// 发布"run方法完成"事件
				listeners.finished(context, exception);
			}
			finally {
				// 报告故障
				reportFailure(analyzers, exception);
				if (context != null) {
					// 关闭这个应用上下文，释放可能持有的所有资源和锁，这包括销毁所有缓存的单例beans
					context.close();
				}
			}
		}
		catch (Exception ex) {
			logger.warn("Unable to close ApplicationContext", ex);
		}
		// 重新抛出运行时异常，终止应用主线程
		ReflectionUtils.rethrowRuntimeException(exception);
	}

	/**
	 * 报告故障。
	 */
	private void reportFailure(FailureAnalyzers analyzers, Throwable failure) {
		try {
			// 分析并报告指定的故障
			if (analyzers != null && analyzers.analyzeAndReport(failure)) {
				// 注册已记录的给定的异常
				registerLoggedException(failure);
				return;
			}
		}
		catch (Throwable ex) {
			// Continue with normal handling of the original failure
		}
		if (logger.isErrorEnabled()) {
			// 应用启动失败
			logger.error("Application startup failed", failure);
			registerLoggedException(failure);
		}
	}

	/**
	 * Register that the given exception has been logged. By default, if the running in
	 * the main thread, this method will suppress additional printing of the stacktrace.
	 * 注册已记录的给定的异常。
	 * 默认情况下，如果在主线程中运行，则这个方法将禁止额外地打印堆栈跟踪信息。
	 * @param exception the exception that was logged
	 */
	protected void registerLoggedException(Throwable exception) {
		// Boot异常处理器
		SpringBootExceptionHandler handler = getSpringBootExceptionHandler();
		if (handler != null) {
			// 注册已记录的异常
			handler.registerLoggedException(exception);
		}
	}

	/**
	 * 处理退出代码。
	 */
	private void handleExitCode(ConfigurableApplicationContext context,
			Throwable exception) {
		// 从异常中获取退出代码
		int exitCode = getExitCodeFromException(context, exception);
		if (exitCode != 0) {
			if (context != null) {
				// 发布"退出代码"事件
				context.publishEvent(new ExitCodeEvent(context, exitCode));
			}
			SpringBootExceptionHandler handler = getSpringBootExceptionHandler();
			if (handler != null) {
				// 注册退出代码
				handler.registerExitCode(exitCode);
			}
		}
	}

	/**
	 * 从异常中获取退出代码。
	 */
	private int getExitCodeFromException(ConfigurableApplicationContext context,
			Throwable exception) {
		// 从映射的异常获取退出代码
		int exitCode = getExitCodeFromMappedException(context, exception);
		if (exitCode == 0) {
			// 从退出代码生成器的异常获取退出代码
			exitCode = getExitCodeFromExitCodeGeneratorException(exception);
		}
		return exitCode;
	}

	/**
	 * 从映射的异常获取退出代码。
	 */
	private int getExitCodeFromMappedException(ConfigurableApplicationContext context,
			Throwable exception) {
		if (context == null || !context.isActive()) {
			return 0;
		}
		// 退出代码生成器列表
		ExitCodeGenerators generators = new ExitCodeGenerators();
		// 退出代码的异常的映射表的列表
		Collection<ExitCodeExceptionMapper> beans = context
				.getBeansOfType(ExitCodeExceptionMapper.class).values();
		generators.addAll(exception, beans);
		return generators.getExitCode();
	}

	/**
	 * 从退出代码生成器的异常获取退出代码。
	 */
	private int getExitCodeFromExitCodeGeneratorException(Throwable exception) {
		if (exception == null) {
			return 0;
		}
		if (exception instanceof ExitCodeGenerator) {
			// 退出代码生成器
			return ((ExitCodeGenerator) exception).getExitCode();
		}
		// 遍历root cause的异常
		return getExitCodeFromExitCodeGeneratorException(exception.getCause());
	}

	/**
	 * 获取boot异常处理器。
	 */
	SpringBootExceptionHandler getSpringBootExceptionHandler() {
		if (isMainThread(Thread.currentThread())) {
			// 若当前执行线程为主线程，则获取当前线程的boot异常处理器
			return SpringBootExceptionHandler.forCurrentThread();
		}
		return null;
	}

	/**
	 * 判断当前执行线程是否为主线程。
	 */
	private boolean isMainThread(Thread currentThread) {
		// 线程名称和线程组名称都是main
		return ("main".equals(currentThread.getName())
				|| "restartedMain".equals(currentThread.getName()))
				&& "main".equals(currentThread.getThreadGroup().getName());
	}

	// getter/setter

	/**
	 * Returns the main application class that has been deduced or explicitly configured.
	 * 返回已经推导或显式配置的应用主类。
	 * @return the main application class or {@code null}
	 */
	public Class<?> getMainApplicationClass() {
		return this.mainApplicationClass;
	}

	/**
	 * Set a specific main application class that will be used as a log source and to
	 * obtain version information. By default the main application class will be deduced.
	 * Can be set to {@code null} if there is no explicit application class.
	 * 默认情况下，将推导出应用主类。
	 * @param mainApplicationClass the mainApplicationClass to set or {@code null}
	 */
	public void setMainApplicationClass(Class<?> mainApplicationClass) {
		this.mainApplicationClass = mainApplicationClass;
	}

	/**
	 * Returns whether this {@link SpringApplication} is running within a web environment.
	 * 返回这个spring应用是否在web环境中运行。
	 * @return {@code true} if running within a web environment, otherwise {@code false}.
	 * @see #setWebEnvironment(boolean)
	 */
	public boolean isWebEnvironment() {
		return this.webEnvironment;
	}

	/**
	 * Sets if this application is running within a web environment. If not specified will
	 * attempt to deduce the environment based on the classpath.
	 * @param webEnvironment if the application is running in a web environment
	 */
	public void setWebEnvironment(boolean webEnvironment) {
		this.webEnvironment = webEnvironment;
	}

	/**
	 * Sets if the application is headless and should not instantiate AWT. Defaults to
	 * {@code true} to prevent java icons appearing.
	 * @param headless if the application is headless
	 */
	public void setHeadless(boolean headless) {
		this.headless = headless;
	}

	/**
	 * Sets if the created {@link ApplicationContext} should have a shutdown hook
	 * registered. Defaults to {@code true} to ensure that JVM shutdowns are handled
	 * gracefully.
	 * 设置创建的应用上下文是否应该注册一个关闭钩子线程。
	 * 缺省值为true，以确保可以优雅地处理JVM关闭。
	 * @param registerShutdownHook if the shutdown hook should be registered
	 */
	public void setRegisterShutdownHook(boolean registerShutdownHook) {
		this.registerShutdownHook = registerShutdownHook;
	}

	/**
	 * Sets the {@link Banner} instance which will be used to print the banner when no
	 * static banner file is provided.
	 * 设置横幅实例，当没有提供静态横幅文件时，将用于打印横幅。
	 * @param banner the Banner instance to use
	 */
	public void setBanner(Banner banner) {
		this.banner = banner;
	}

	/**
	 * Sets the mode used to display the banner when the application runs. Defaults to
	 * {@code Banner.Mode.CONSOLE}.
	 * 设置用于在应用程序运行时显示横幅的模式。
	 * @param bannerMode the mode used to display the banner
	 */
	public void setBannerMode(Banner.Mode bannerMode) {
		this.bannerMode = bannerMode;
	}

	/**
	 * Sets if the application information should be logged when the application starts.
	 * Defaults to {@code true}.
	 * 设置在应用程序启动时是否记录应用程序信息。
	 * @param logStartupInfo if startup info should be logged.
	 */
	public void setLogStartupInfo(boolean logStartupInfo) {
		this.logStartupInfo = logStartupInfo;
	}

	/**
	 * Sets if a {@link CommandLinePropertySource} should be added to the application
	 * context in order to expose arguments. Defaults to {@code true}.
	 * @param addCommandLineProperties if command line arguments should be exposed
	 */
	public void setAddCommandLineProperties(boolean addCommandLineProperties) {
		this.addCommandLineProperties = addCommandLineProperties;
	}

	// 配置环境

	/**
	 * Set default environment properties which will be used in addition to those in the
	 * existing {@link Environment}.
	 * 将现有环境中使用的环境属性集设置为默认的环境属性集。
	 * @param defaultProperties the additional properties to set
	 */
	public void setDefaultProperties(Map<String, Object> defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	/**
	 * Convenient alternative to {@link #setDefaultProperties(Map)}.
	 * @param defaultProperties some {@link Properties}
	 */
	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = new HashMap<String, Object>();
		for (Object key : Collections.list(defaultProperties.propertyNames())) {
			this.defaultProperties.put((String) key, defaultProperties.get(key));
		}
	}

	/**
	 * Set additional profile values to use (on top of those set in system or command line
	 * properties).
	 * 设置其他的配置文件集。
	 * @param profiles the additional profiles to set
	 */
	public void setAdditionalProfiles(String... profiles) {
		this.additionalProfiles = new LinkedHashSet<String>(Arrays.asList(profiles));
	}

	/**
	 * Sets the bean name generator that should be used when generating bean names.
	 * 设置生成bean名称使用的bean名称生成器。
	 * @param beanNameGenerator the bean name generator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}

	/**
	 * Sets the underlying environment that should be used with the created application
	 * context.
	 * 设置与创建的应用上下文一起使用的底层的可配置的应用运行时环境。
	 * @param environment the environment
	 */
	public void setEnvironment(ConfigurableEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * Returns a mutable set of the sources that will be added to an ApplicationContext
	 * when {@link #run(String...)} is called.
	 * 返回可变的一组bean资源对象，将在调用run(String...)时添加到应用上下文中。
	 * @return the sources the application sources.
	 * @see #SpringApplication(Object...)
	 */
	public Set<Object> getSources() {
		return this.sources;
	}

	/**
	 * The sources that will be used to create an ApplicationContext. A valid source is
	 * one of: a class, class name, package, package name, or an XML resource location.
	 * Can also be set using constructors and static convenience methods (e.g.
	 * {@link #run(Object[], String[])}).
	 * 将用于创建应用上下文的bean资源对象列表。
	 * 有效的bean资源对象是其中之一：一个类，类名称，包，包名或XML资源位置。
	 * 可以使用构造函数和静态方法进行设置。
	 * <p>
	 * NOTE: sources defined here will be used in addition to any sources specified on
	 * construction.
	 * 注意：除了在构造函数上指定的任何来源外，还将使用此处定义的bean资源对象集合。
	 * @param sources the sources to set
	 * @see #SpringApplication(Object...)
	 */
	public void setSources(Set<Object> sources) {
		Assert.notNull(sources, "Sources must not be null");
		this.sources.addAll(sources);
	}

	/**
	 * Sets the {@link ResourceLoader} that should be used when loading resources.
	 * 设置加载资源时使用的资源加载器。
	 * @param resourceLoader the resource loader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Sets the type of Spring {@link ApplicationContext} that will be created. If not
	 * specified defaults to {@link #DEFAULT_WEB_CONTEXT_CLASS} for web based applications
	 * or {@link AnnotationConfigApplicationContext} for non web based applications.
	 * 设置将要创建的应用上下文的类型对象。
	 * 如果未指定，则对于web应用程序，默认值为AnnotationConfigEmbeddedWebApplicationContext；
	 * 对于非web应用程序，默认值为AnnotationConfigApplicationContext。
	 * @param applicationContextClass the context class to set
	 */
	public void setApplicationContextClass(
			Class<? extends ConfigurableApplicationContext> applicationContextClass) {
		this.applicationContextClass = applicationContextClass;
		if (!isWebApplicationContext(applicationContextClass)) {
			this.webEnvironment = false;
		}
	}

	/**
	 * 判断是否为web应用上下文。
	 */
	private boolean isWebApplicationContext(Class<?> applicationContextClass) {
		try {
			// web应用上下文
			return WebApplicationContext.class.isAssignableFrom(applicationContextClass);
		}
		catch (NoClassDefFoundError ex) {
			return false;
		}
	}

	// 应用上下文初始化器

	/**
	 * Sets the {@link ApplicationContextInitializer} that will be applied to the Spring
	 * {@link ApplicationContext}.
	 * 设置应用上下文初始化器列表，将应用于应用上下文。
	 * @param initializers the initializers to set
	 */
	public void setInitializers(
			Collection<? extends ApplicationContextInitializer<?>> initializers) {
		this.initializers = new ArrayList<ApplicationContextInitializer<?>>();
		this.initializers.addAll(initializers);
	}

	/**
	 * Add {@link ApplicationContextInitializer}s to be applied to the Spring
	 * {@link ApplicationContext}.
	 * 添加应用上下文初始化器列表。
	 * @param initializers the initializers to add
	 */
	public void addInitializers(ApplicationContextInitializer<?>... initializers) {
		this.initializers.addAll(Arrays.asList(initializers));
	}

	/**
	 * Returns read-only ordered Set of the {@link ApplicationContextInitializer}s that
	 * will be applied to the Spring {@link ApplicationContext}.
	 * @return the initializers
	 */
	public Set<ApplicationContextInitializer<?>> getInitializers() {
		return asUnmodifiableOrderedSet(this.initializers);
	}

	// 应用监视器

	/**
	 * Sets the {@link ApplicationListener}s that will be applied to the SpringApplication
	 * and registered with the {@link ApplicationContext}.
	 * 设置应用监视器列表。
	 * @param listeners the listeners to set
	 */
	public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
		this.listeners = new ArrayList<ApplicationListener<?>>();
		this.listeners.addAll(listeners);
	}

	/**
	 * Add {@link ApplicationListener}s to be applied to the SpringApplication and
	 * registered with the {@link ApplicationContext}.
	 * 添加应用监视器列表。
	 * @param listeners the listeners to add
	 */
	public void addListeners(ApplicationListener<?>... listeners) {
		this.listeners.addAll(Arrays.asList(listeners));
	}

	/**
	 * Returns read-only ordered Set of the {@link ApplicationListener}s that will be
	 * applied to the SpringApplication and registered with the {@link ApplicationContext}
	 * .
	 * @return the listeners
	 */
	public Set<ApplicationListener<?>> getListeners() {
		return asUnmodifiableOrderedSet(this.listeners);
	}

	// 开始运行应用

	/**
	 * Static helper that can be used to run a {@link SpringApplication} from the
	 * specified source using default settings.
	 * 可用于使用默认设置的指定的bean资源对象运行Spring应用。
	 * @param source the source to load
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return the running {@link ApplicationContext}
	 */
	public static ConfigurableApplicationContext run(Object source, String... args) {
		return run(new Object[] { source }, args);
	}

	/**
	 * Static helper that can be used to run a {@link SpringApplication} from the
	 * specified sources using default settings and user supplied arguments.
	 * @param sources the sources to load
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return the running {@link ApplicationContext}
	 */
	public static ConfigurableApplicationContext run(Object[] sources, String[] args) {
		// 初始化并运行Spring应用
		return new SpringApplication(sources).run(args);
	}

	/**
	 * A basic main that can be used to launch an application. This method is useful when
	 * application sources are defined via a {@literal --spring.main.sources} command line
	 * argument.
	 * 可用于启动应用程序的入口方法。
	 * 当通过"--spring.main.sources"命令行参数定义bean资源对象集合，这个方法很有用。
	 * <p>
	 * Most developers will want to define their own main method and call the
	 * {@link #run(Object, String...) run} method instead.
	 * 大多数开发人员都想定义自己的main入口方法，而是调用run方法。
	 * @param args command line arguments
	 * @see SpringApplication#run(Object[], String[])
	 * @see SpringApplication#run(Object, String...)
	 */
	public static void main(String[] args) {
		// 空的参数列表
		SpringApplication.run(new Object[0], args);
	}

	/**
	 * Static helper that can be used to exit a {@link SpringApplication} and obtain a
	 * code indicating success (0) or otherwise. Does not throw exceptions but should
	 * print stack traces of any encountered. Applies the specified
	 * {@link ExitCodeGenerator} in addition to any Spring beans that implement
	 * {@link ExitCodeGenerator}. In the case of multiple exit codes the highest value
	 * will be used (or if all values are negative, the lowest value will be used)
	 * 可用于退出Spring应用并获取指示成功(0)的辅助方法。
	 * 不抛出异常，但应打印遇到的任何堆栈跟踪信息。
	 * 除了实现代码退出生成器的所有beans之外，还应用指定的代码退出生成器。
	 * 在有多个退出代码的情况下，将使用最大值。
	 * @param context the context to close if possible
	 * @param exitCodeGenerators exist code generators
	 * @return the outcome (0 if successful)
	 */
	public static int exit(ApplicationContext context,
			ExitCodeGenerator... exitCodeGenerators) {
		Assert.notNull(context, "Context must not be null");
		int exitCode = 0;
		try {
			try {
				// 退出代码生成器列表
				ExitCodeGenerators generators = new ExitCodeGenerators();
				Collection<ExitCodeGenerator> beans = context
						.getBeansOfType(ExitCodeGenerator.class).values();
				generators.addAll(exitCodeGenerators);
				generators.addAll(beans);
				// 退出代码
				exitCode = generators.getExitCode();
				if (exitCode != 0) {
					// 退出代码为非零，则发布"退出代码"应用事件
					context.publishEvent(new ExitCodeEvent(context, exitCode));
				}
			}
			finally {
				// 关闭这个可配置的应用上下文
				close(context);
			}
		}
		catch (Exception ex) {
			// 这里为何不使用日志记录器打印堆栈跟踪信息？
//			ex.printStackTrace();
			exitCode = (exitCode != 0) ? exitCode : 1;
		}
		return exitCode;
	}

	/**
	 * 关闭这个可配置的应用上下文，释放可能持有的所有资源和锁，这包括销毁所有缓存的单例beans。
	 */
	private static void close(ApplicationContext context) {
		if (context instanceof ConfigurableApplicationContext) {
			// 可配置的应用上下文
			ConfigurableApplicationContext closable = (ConfigurableApplicationContext) context;
			// 关闭应用上下文
			closable.close();
		}
	}

	/**
	 * 将元素容器转换为不可修改的有序的集合。
	 */
	private static <E> Set<E> asUnmodifiableOrderedSet(Collection<E> elements) {
		List<E> list = new ArrayList<E>(elements);
		Collections.sort(list, AnnotationAwareOrderComparator.INSTANCE);
		return new LinkedHashSet<E>(list);
	}

}
