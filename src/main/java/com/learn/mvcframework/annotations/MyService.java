package com.learn.mvcframework.annotations;

import java.lang.annotation.*;

/**
 * 自定义Service
 *
 * @author Yuhaoran
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyService {
}
