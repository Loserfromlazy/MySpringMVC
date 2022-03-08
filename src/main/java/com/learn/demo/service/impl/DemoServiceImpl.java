package com.learn.demo.service.impl;

import com.learn.demo.service.DemoService;
import com.learn.mvcframework.annotations.MyService;

/**
 * <p>
 * DemoServiceImpl
 * </p>
 *
 * @author Yuhaoran
 * @since 2022/3/8
 */
@MyService
public class DemoServiceImpl implements DemoService {
    @Override
    public String getName(Integer id) {
        System.out.println("参数是："+id);
        return "ZhangSan";
    }
}
