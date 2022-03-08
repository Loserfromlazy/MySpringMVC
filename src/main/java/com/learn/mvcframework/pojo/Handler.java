package com.learn.mvcframework.pojo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>
 * Handler
 * </p>
 *
 * @author Yuhaoran
 * @since 2022/3/8
 */
public class Handler {

    private Object controller;

    private Method method;
    /**
     * 存储url支持正则
     */
    private Pattern pattern;
    /**
     * 存储参数顺序，为了进行参数绑定
     * key是参数名，value是代表第几个参数
     */
    private Map<String,Integer> paramIndexMapping;

    public Handler(Object controller, Method method, Pattern pattern) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
        this.paramIndexMapping = new HashMap<>();
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
        this.paramIndexMapping = paramIndexMapping;
    }
}
