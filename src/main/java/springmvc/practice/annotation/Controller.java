package springmvc.practice.annotation;
/**
 * 1����дSpringMVC���õ�ע�⣬ͨ����дע�⣬�����springmvc�Ļ�������
 */
import java.lang.annotation.*;
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
	String value() default "";
}
