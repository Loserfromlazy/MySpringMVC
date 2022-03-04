package com.learn.mvcframework.annotations;

import java.lang.annotation.*;

/**
 * 自定义Controller
 *
 * @author Yuhaoran
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyController {
    String value() default "";
}
