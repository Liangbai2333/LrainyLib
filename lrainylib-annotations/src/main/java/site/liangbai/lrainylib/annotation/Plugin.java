package site.liangbai.lrainylib.annotation;

import site.liangbai.lrainylib.annotation.plugin.Info;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Plugin {
    Info info();

    String apiVersion() default "";

    String[] depend() default "";

    String[] softDepend() default "";

    String[] loadBefore() default "";

    String classLoaderOf() default "";

    String load() default "";

    String defaultPermission() default "";

    String prefix() default "";

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    @interface EventSubscriber {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    @interface Instance {
        String plugin() default "";
    }
}


