package site.liangbai.lrainylib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CommandHandler {
    String value();

    String usage() default "";

    String description() default "";

    String permission() default "";

    String permissionMessage() default "";

    String[] aliases() default "";
}
