package com.learn.mvcframework.annotations;

import java.lang.annotation.*;

/**
 * 自定义RequestMapping
 *
 * @author Yuhaoran
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRequestMapping {
}
