
package org.springframework.boot;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 * Class used by {@link SpringApplication} to print the application banner.
 * Spring应用对象打印器，用于打印应用程序的横幅。
 *
 * @author Phillip Webb
 */
class SpringApplicationBannerPrinter {

	/**
	 * 横幅文件的位置
	 */
	static final String BANNER_LOCATION_PROPERTY = "banner.location";

	/**
	 * 横幅图像的位置
	 */
	static final String BANNER_IMAGE_LOCATION_PROPERTY = "banner.image.location";

	/**
	 * 默认的横幅文件的位置
	 */
	static final String DEFAULT_BANNER_LOCATION = "banner.txt";

	static final String[] IMAGE_EXTENSION = { "gif", "jpg", "png" };

	/**
	 * 默认的横幅对象
	 */
	private static final Banner DEFAULT_BANNER = new SpringBootBanner();

	/**
	 * 文件资源加载器
	 */
	private final ResourceLoader resourceLoader;

	/**
	 * 后备的横幅对象
	 */
	private final Banner fallbackBanner;

	SpringApplicationBannerPrinter(ResourceLoader resourceLoader, Banner fallbackBanner) {
		this.resourceLoader = resourceLoader;
		this.fallbackBanner = fallbackBanner;
	}

	/**
	 * 打印应用程序的横幅。
	 */
	public Banner print(Environment environment, Class<?> sourceClass, Log logger) {
		Banner banner = getBanner(environment);
		try {
			// 记录日志信息
			logger.info(createStringFromBanner(banner, environment, sourceClass));
		}
		catch (UnsupportedEncodingException ex) {
			logger.warn("Failed to create String for banner", ex);
		}
		return new PrintedBanner(banner, sourceClass);
	}

	public Banner print(Environment environment, Class<?> sourceClass, PrintStream out) {
		Banner banner = getBanner(environment);
		// 打印横幅
		banner.printBanner(environment, sourceClass, out);
		return new PrintedBanner(banner, sourceClass);
	}

	private Banner getBanner(Environment environment) {
		Banners banners = new Banners();
		// 图像文件形式的横幅
		banners.addIfNotNull(getImageBanner(environment));
		// 文本文件形式的横幅
		banners.addIfNotNull(getTextBanner(environment));
		if (banners.hasAtLeastOneBanner()) {
			return banners;
		}
		if (this.fallbackBanner != null) {
			return this.fallbackBanner;
		}
		return DEFAULT_BANNER;
	}

	private Banner getTextBanner(Environment environment) {
		String location = environment.getProperty(BANNER_LOCATION_PROPERTY,
				DEFAULT_BANNER_LOCATION);
		Resource resource = this.resourceLoader.getResource(location);
		if (resource.exists()) {
			return new ResourceBanner(resource);
		}
		return null;
	}

	private Banner getImageBanner(Environment environment) {
		String location = environment.getProperty(BANNER_IMAGE_LOCATION_PROPERTY);
		if (StringUtils.hasLength(location)) {
			Resource resource = this.resourceLoader.getResource(location);
			return (resource.exists() ? new ImageBanner(resource) : null);
		}
		for (String ext : IMAGE_EXTENSION) {
			Resource resource = this.resourceLoader.getResource("banner." + ext);
			if (resource.exists()) {
				return new ImageBanner(resource);
			}
		}
		return null;
	}

	private String createStringFromBanner(Banner banner, Environment environment,
			Class<?> mainApplicationClass) throws UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		banner.printBanner(environment, mainApplicationClass, new PrintStream(baos));
		// 横幅字符编码
		String charset = environment.getProperty("banner.charset", "UTF-8");
		return baos.toString(charset);
	}

	/**
	 * {@link Banner} comprised of other {@link Banner Banners}.
	 */
	private static class Banners implements Banner {

		/**
		 * 横幅对象列表
		 */
		private final List<Banner> banners = new ArrayList<Banner>();

		public void addIfNotNull(Banner banner) {
			if (banner != null) {
				this.banners.add(banner);
			}
		}

		public boolean hasAtLeastOneBanner() {
			return !this.banners.isEmpty();
		}

		@Override
		public void printBanner(Environment environment, Class<?> sourceClass,
				PrintStream out) {
			for (Banner banner : this.banners) {
				// 打印横幅
				banner.printBanner(environment, sourceClass, out);
			}
		}

	}

	/**
	 * Decorator that allows a {@link Banner} to be printed again without needing to
	 * specify the source class.
	 */
	private static class PrintedBanner implements Banner {

		/**
		 * 横幅对象
		 */
		private final Banner banner;

		/**
		 * 资源类
		 */
		private final Class<?> sourceClass;

		PrintedBanner(Banner banner, Class<?> sourceClass) {
			this.banner = banner;
			this.sourceClass = sourceClass;
		}

		@Override
		public void printBanner(Environment environment, Class<?> sourceClass,
				PrintStream out) {
			sourceClass = (sourceClass != null) ? sourceClass : this.sourceClass;
			this.banner.printBanner(environment, sourceClass, out);
		}

	}

}
