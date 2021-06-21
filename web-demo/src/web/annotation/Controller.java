package web.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用在controller类上，表示这个类中有方法可以实现请求，可以扫描
 * **/
@Target(
        ElementType.METHOD
)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

}
