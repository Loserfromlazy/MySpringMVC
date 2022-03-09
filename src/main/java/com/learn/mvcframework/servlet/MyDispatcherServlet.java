package com.learn.mvcframework.servlet;

import com.learn.mvcframework.annotations.*;
import com.learn.mvcframework.pojo.Handler;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * MyDispatcherServlet
 * </p>
 *
 * @author Yuhaoran
 * @since 2022/3/4
 */
public class MyDispatcherServlet extends HttpServlet {

    /**
     * 存储配置文件
     */
    private Properties properties = new Properties();
    /**
     * 存储扫描到的类的全限定类名
     */
    private List<String> classNames = new ArrayList<>();
    /**
     * IOC容器
     */
    private Map<String, Object> iocMap = new HashMap<>();

    /**
     * 处理器映射器，存储url和method的映射关系
     */
    private List<Handler> handlerMapping = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1. 加载配置文件 springmvc.xml
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        handleLoadConfig(contextConfigLocation);
        //2. 扫描相关的类，扫描注解
        handleScan(properties.getProperty("component-scan"));
        //3. 初始化bean对象（基于注解实现IOC容器）
        handleInstance();
        //4. 实现依赖注入
        handleAutowired();
        //5. 构造各个组件，这里仅实现HandlerMapping处理器映射器，将配置好的url和method建立映射关系
        initHandlerMapping();
        System.out.println("初始化完成");
        //6. 等待请求进入，处理请求
    }

    /**
     * 实现HandlerMapping处理器映射器,将url和method建立关系
     *
     * @author Yuhaoran
     */
    private void initHandlerMapping() {
        if (iocMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            Class<?> aClass = entry.getValue().getClass();
            if (!aClass.isAnnotationPresent(MyController.class)) {
                continue;
            }
            String baseUrl = "";
            if (aClass.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping annotation = aClass.getAnnotation(MyRequestMapping.class);
                baseUrl = annotation.value();
            }
            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                String methorUrl = annotation.value();
                String url = baseUrl + methorUrl;
                //封装handler
                Handler handler = new Handler(entry.getValue(),method, Pattern.compile(url));
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].getType() == HttpServletRequest.class || parameters[i].getType() ==HttpServletResponse.class){
                        handler.getParamIndexMapping().put(parameters[i].getType().getSimpleName(),i);
                    }else {
                        handler.getParamIndexMapping().put(parameters[i].getName(),i);
                    }
                }
                handlerMapping.add(handler);
            }

        }

    }

    /**
     * 实现依赖注入
     *
     * @author Yuhaoran
     */
    private void handleAutowired() {
        if (iocMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    continue;
                }
                String beanName = "";
                if (field.isAnnotationPresent(MyQualifier.class)) {
                    MyQualifier annotation = field.getAnnotation(MyQualifier.class);
                    beanName = annotation.value();
                } else {
                    //如果没有MyQualifier 那就根据字段类型注入 即接口注入
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), iocMap.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 初始化bean对象（基于注解实现IOC容器）
     *
     * @author Yuhaoran
     */
    private void handleInstance() {
        if (classNames.size() == 0) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> aClass = Class.forName(className);
                if (aClass.isAnnotationPresent(MyController.class)) {
                    String simpleName = aClass.getSimpleName();
                    MyController annotation = aClass.getAnnotation(MyController.class);
                    String beanName = annotation.value();
                    if ("".equals(beanName) || beanName == null) {
                        beanName = lowerFirst(aClass.getSimpleName());
                        iocMap.put(beanName, aClass.newInstance());
                    } else {
                        iocMap.put(beanName, aClass.newInstance());
                    }
                } else if (aClass.isAnnotationPresent(MyService.class)) {
                    MyService annotation = aClass.getAnnotation(MyService.class);
                    String beanName = annotation.value();
                    if ("".equals(beanName) || beanName == null) {
                        beanName = lowerFirst(aClass.getSimpleName());
                        iocMap.put(beanName, aClass.newInstance());
                    } else {
                        iocMap.put(beanName, aClass.newInstance());
                    }
                    //service层一般都有接口，我们将接口再保存一份，便于自动注入时使用
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (Class<?> anInterface : interfaces) {
                        //存入接口的全限定类名
                        iocMap.put(anInterface.getName(), aClass.newInstance());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 首字母小写
     *
     * @author Yuhaoran
     * @date 2022/3/8 16:40
     */
    private String lowerFirst(String name) {
        char[] chars = name.toCharArray();
        if (chars[0] <= 'Z' && chars[0] >= 'A') {
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }

    /**
     * 扫描相关的类，扫描注解
     *
     * @author Yuhaoran
     */
    private void handleScan(String scanPackage) {
        String scanPath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + scanPackage.replaceAll("\\.", "\\\\");
        File scanFile = new File(scanPath);
        File[] files = scanFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                //递归扫描
                handleScan(scanPackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                //类的全限定类名 com.learn.demo.DemoController
                String className = scanPackage + "."+file.getName().replaceAll(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 加载配置文件
     *
     * @author Yuhaoran
     */
    private void handleLoadConfig(String contextConfigLocation) {
        InputStream resourceAsStream = MyDispatcherServlet.class.getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Handler handler = getHandler(req);
        if (handler==null){
            resp.getWriter().write("404 NOT FOUND");
            return;
        }
        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();
        Object[] paramsValue = new Object[parameterTypes.length];
        Map<String, String[]> parameterMap = req.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            // 此处是因为前端传递过来的参数是数组，如?name=1&name=2 所以需要进行处理
            String join = StringUtils.join(entry.getValue(), ",");
            if (!handler.getParamIndexMapping().containsKey(entry.getKey())){
                continue;
            }
            Integer index = handler.getParamIndexMapping().get(entry.getKey());
            Class<?> parameterType = parameterTypes[index];
            Object o = null;
            try {
                o = transformValue(parameterType,join);
            } catch (Exception e) {
                e.printStackTrace();
            }
            paramsValue[index] = o;
        }

        Integer requetsIndex = handler.getParamIndexMapping().get(HttpServletRequest.class.getSimpleName());
        Integer responseIndex = handler.getParamIndexMapping().get(HttpServletResponse.class.getSimpleName());
        paramsValue[requetsIndex] = req;
        paramsValue[responseIndex] = resp;
        try {
            handler.getMethod().invoke(handler.getController(),paramsValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    /**
     * 从handlerMapping获取Handler
     */
    private Handler getHandler(HttpServletRequest request){
        if (handlerMapping.isEmpty()){
            return null;
        }
        String requestURI = request.getRequestURI();
        for (Handler handler : handlerMapping) {
            Matcher matcher = handler.getPattern().matcher(requestURI);
            if (!matcher.matches()){
                continue;
            }
            return handler;
        }
        return null;
    }

    /**
     * 将value转换为对应的class类型 fieldType
     *
     * @param fieldType class类型
     * @param value     需要转换的值
     * @return Object
     * @throws Exception 反射获取类 Class.forName 可能会导致异常
     */
    private Object transformValue(Class fieldType, String value) throws Exception {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        if (fieldType == String.class) {
            return String.valueOf(value);
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return Boolean.valueOf(value);
        } else if (fieldType == Byte.class || fieldType == byte.class) {
            return Byte.valueOf(value);
        } else if (fieldType == Double.class || fieldType == double.class) {
            return Double.valueOf(value);
        } else if (fieldType == Float.class || fieldType == float.class) {
            return Float.valueOf(value);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.valueOf(value);
        } else if (fieldType == Long.class || fieldType == long.class) {
            return Long.valueOf(value);
        } else if (fieldType == Short.class || fieldType == short.class) {
            return Short.valueOf(value);
        } else if (fieldType == Character.class || fieldType == char.class) {
            return value.charAt(0);
        } else if (fieldType == BigDecimal.class) {
            return new BigDecimal(value);
        } else if (fieldType == BigInteger.class) {
            return new BigInteger(value);
        } else if (fieldType == Date.class) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(value);
        } else if (fieldType == List.class) {
            return Arrays.asList(value.split(","));
        } else if (fieldType == Set.class) {
            return new HashSet<>(Arrays.asList(value.split(",")));
        } else if (fieldType.isEnum()) { // 枚举类型
            Class<?> cl = Class.forName(fieldType.getName());
            Field field = cl.getDeclaredField(value);
            return field.get(cl);
        } else if (fieldType == Pattern.class) {
            return Pattern.compile(value);
        } else {
            return value;
        }
    }
}
