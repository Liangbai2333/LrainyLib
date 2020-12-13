package site.liangbai.lrainylib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Service {
    String priority() default "Normal";

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    @interface ServiceProviderInstance {
        String classFullName();
    }
}
