package com.learn.mvcframework.servlet;

import com.learn.mvcframework.annotations.MyAutowired;
import com.learn.mvcframework.annotations.MyController;
import com.learn.mvcframework.annotations.MyQualifier;
import com.learn.mvcframework.annotations.MyService;
import com.sun.deploy.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

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
    private final Properties properties = new Properties();
    /**
     * 存储扫描到的类的全限定类名
     */
    private List<String> classNames = new ArrayList<>();
    /**
     * IOC容器
     */
    private Map<String,Object> iocMap = new HashMap<>();

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
        if (iocMap.isEmpty()){
            return;
        }

    }

    /**
     * 实现依赖注入
     *
     * @author Yuhaoran
     */
    private void handleAutowired() {
        if (iocMap.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (!field.isAnnotationPresent(MyAutowired.class)){
                    continue;
                }
                String beanName ="";
                if (field.isAnnotationPresent(MyQualifier.class)){
                    MyQualifier annotation = field.getAnnotation(MyQualifier.class);
                    beanName = annotation.value();
                }else {
                    //如果没有MyQualifier 那就根据字段类型注入 即接口注入
                    beanName= field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),iocMap.get(beanName));
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
    private void handleInstance(){
        if (classNames.size() == 0) {
            return;
        }
        try{
            for (String className : classNames) {
                Class<?> aClass = Class.forName(className);
                if (aClass.isAnnotationPresent(MyController.class)){
                    String simpleName = aClass.getSimpleName();
                    MyController annotation = aClass.getAnnotation(MyController.class);
                    String beanName = annotation.value();
                    if ("".equals(beanName)||beanName==null){
                        beanName =lowerFirst(aClass.getSimpleName());
                        iocMap.put(beanName,aClass.newInstance());
                    }else {
                        iocMap.put(beanName,aClass.newInstance());
                    }
                }else if (aClass.isAnnotationPresent(MyService.class)){
                    MyService annotation = aClass.getAnnotation(MyService.class);
                    String beanName = annotation.value();
                    if ("".equals(beanName)||beanName==null){
                        beanName =lowerFirst(aClass.getSimpleName());
                        iocMap.put(beanName,aClass.newInstance());
                    }else {
                        iocMap.put(beanName,aClass.newInstance());
                    }
                    //service层一般都有接口，我们将接口再保存一份，便于自动注入时使用
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (Class<?> anInterface : interfaces) {
                        //存入接口的全限定类名
                        iocMap.put(anInterface.getName(),aClass.newInstance());
                    }
                }
            }
        }catch (Exception e){
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
        String scanPath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + scanPackage.replaceAll("\\.", "\\");
        File scanFile = new File(scanPath);
        File[] files = scanFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                //递归扫描
                handleScan(scanPackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                //类的全限定类名 com.learn.demo.DemoController
                String className = scanPackage + file.getName().replaceAll(".class", "");
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
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
