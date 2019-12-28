
package org.springframework.boot.context.properties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field in a {@link ConfigurationProperties} object should be treated as
 * if it were a nested type. This annotation has no bearing on the actual binding
 * processes, but it is used by the {@code spring-boot-configuration-processor} as a hint
 * that a field is not bound as a single value. When this is specified, a nested group is
 * created for the field and its type is harvested.
 * 嵌套的配置属性，指示应将外部化配置的注解对象中的字段视为嵌套类型。
 * 这个注解与实际的绑定过程没有关系，但是它被spring boot配置处理器用来暗示一个字段没有被绑定为单个值。
 * 指定这个选项后，将为这个字段创建一个嵌套组，并获取其类型。
 * <p>
 * This has no effect on collections and maps as these types are automatically identified.
 * 这对容器和映射表没有影响，因为这些类型是自动识别的。
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 1.2.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NestedConfigurationProperty {

}
