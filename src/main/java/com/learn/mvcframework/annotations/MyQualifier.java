package com.learn.mvcframework.annotations;

import java.lang.annotation.*;

/**
 * 自定义Qualifier
 *
 * @author Yuhaoran
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyQualifier {
    String value() default "";
}
