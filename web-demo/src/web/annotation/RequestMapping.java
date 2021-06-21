package web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 *配置请求映射关系
 *指定哪个请求对应哪个controller的方法
 *作用在哪个方法上，就表示映射哪个方法
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    public String value();
}

