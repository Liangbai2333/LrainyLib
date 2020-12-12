package site.liangbai.lrainylib.core.annotation.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Info {
    String name();

    String version() default "1.0";

    String[] authors() default "no one";

    String description() default "";

    String website() default "";
}
