package site.liangbai.lrainylib.annotation.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Permission {
    String name();
    
    String description() default "";
    
    String defaultValue() default "op";
}
