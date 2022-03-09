package com.learn.demo.controller;

import com.learn.demo.service.DemoService;
import com.learn.mvcframework.annotations.MyAutowired;
import com.learn.mvcframework.annotations.MyController;
import com.learn.mvcframework.annotations.MyRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * DemoController
 * </p>
 *
 * @author Yuhaoran
 * @since 2022/3/8
 */
@MyController
@MyRequestMapping("/demo")
public class DemoController {

    @MyAutowired
    private DemoService demoService;

    @MyRequestMapping("/getName")
    public void getName(HttpServletRequest request, HttpServletResponse response,Integer id){
        String name = demoService.getName(id);
        System.out.println(name);
    }


}
