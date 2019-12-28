
package org.springframework.boot.context.properties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.validation.annotation.Validated;

/**
 * Annotation for externalized configuration. Add this to a class definition or a
 * {@code @Bean} method in a {@code @Configuration} class if you want to bind and validate
 * some external Properties (e.g. from a .properties file).
 * 外部化配置属性集的注解。
 * 如果要绑定和验证某些外部属性，请将其添加到类定义或@Configuration类中的@Bean方法。
 * <p>
 * Note that contrary to {@code @Value}, SpEL expressions are not evaluated since property
 * values are externalized.
 * 请注意，与@Value相反，由于属性值是外部化的，因此不评估SpEL表达式。
 *
 * @author Dave Syer
 * @see ConfigurationPropertiesBindingPostProcessor
 * @see EnableConfigurationProperties
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {

	/**
	 * The name prefix of the properties that are valid to bind to this object. Synonym
	 * for {@link #prefix()}.
	 * @return the name prefix of the properties to bind
	 */
	@AliasFor("prefix")
	String value() default "";

	/**
	 * The name prefix of the properties that are valid to bind to this object. Synonym
	 * for {@link #value()}.
	 * 属性的名称前缀
	 * @return the name prefix of the properties to bind
	 */
	@AliasFor("value")
	String prefix() default "";

	/**
	 * Flag to indicate that when binding to this object invalid fields should be ignored.
	 * Invalid means invalid according to the binder that is used, and usually this means
	 * fields of the wrong type (or that cannot be coerced into the correct type).
	 * 是否忽略对象无效字段，默认值为不忽略。
	 * @return the flag value (default false)
	 */
	boolean ignoreInvalidFields() default false;

	/**
	 * Flag to indicate that when binding to this object fields with periods in their
	 * names should be ignored.
	 * 是否忽略嵌套的属性，默认值为不忽略。
	 * @return the flag value (default false)
	 */
	boolean ignoreNestedProperties() default false;

	/**
	 * Flag to indicate that when binding to this object unknown fields should be ignored.
	 * An unknown field could be a sign of a mistake in the Properties.
	 * 是否忽略未知的字段，默认值为忽略。
	 * @return the flag value (default true)
	 */
	boolean ignoreUnknownFields() default true;

	/**
	 * Flag to indicate that an exception should be raised if a Validator is available,
	 * the class is annotated with {@link Validated @Validated} and validation fails. If
	 * it is set to false, validation errors will be swallowed. They will be logged, but
	 * not propagated to the caller.
	 * @return the flag value (default true)
	 * @deprecated as of 1.5 since validation only kicks in when {@code @Validated} is
	 * present
	 */
	@Deprecated
	boolean exceptionIfInvalid() default true;

}
