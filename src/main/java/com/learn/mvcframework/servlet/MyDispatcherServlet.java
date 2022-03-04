package com.learn.mvcframework.servlet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * MyDispatcherServlet
 * </p>
 *
 * @author Yuhaoran
 * @since 2022/3/4
 */
public class MyDispatcherServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1. 加载配置文件 springmvc.xml
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        handleLoadConfig(contextConfigLocation);
        //2. 扫描相关的类，扫描注解
        handleScan("");
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
     * 实现HandlerMapping处理器映射器
     *
     * @author Yuhaoran
     */
    private void initHandlerMapping() {

    }

    /**
     * 实现依赖注入
     *
     * @author Yuhaoran
     */
    private void handleAutowired() {

    }

    /**
     * 初始化bean对象（基于注解实现IOC容器）
     *
     * @author Yuhaoran
     */
    private void handleInstance() {
    }

    /**
     * 扫描相关的类，扫描注解
     *
     * @author Yuhaoran
     */
    private void handleScan(String scanPackage) {
    }

    /**
     * 加载配置文件
     *
     * @author Yuhaoran
     */
    private void handleLoadConfig(String contextConfigLocation) {
        SAXReader saxReader = new SAXReader();
        InputStream resourceAsStream = MyDispatcherServlet.class.getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();


        } catch (DocumentException e) {
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
