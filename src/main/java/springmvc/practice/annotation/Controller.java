package springmvc.practice.annotation;
/**
 * 1、编写SpringMVC常用的注解，通过编写注解，来完成springmvc的基本功能
 */
import java.lang.annotation.*;
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
	String value() default "";
}
