package com.controller;

import com.alibaba.fastjson.JSON;
import com.domain.Car;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TestController8 extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("测试8");

        List<Car> cars = new ArrayList<Car>();
        cars.add( new Car(1,"宝马","红色",300000) );
        cars.add( new Car(21,"奔驰","白色",400000) );
        cars.add( new Car(3,"奥迪","黑色",500000) );
        String json = JSON.toJSONString(cars);

//        resp.setContentType("text/json;charset=utf-8");
        resp.setCharacterEncoding("gbk");
        resp.getWriter().write(json);

    }
}
