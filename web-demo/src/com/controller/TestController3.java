package com.controller;

import com.domain.Car;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/test3")
@SuppressWarnings("all")
public class TestController3 extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("测试3");

        req.setCharacterEncoding("utf-8");
        String cno = req.getParameter("cno");
        String cname = req.getParameter("cname") ;
        String color = req.getParameter("color");
        String price = req.getParameter("price") ;

        Integer _cno  ;
        if(cno != null && !"".equals(cno)){
            _cno = Integer.parseInt(cno) ;
        }else{
            throw new NumberFormatException("格式不正确");
        }

        Integer _price ;
        if(price != null && !"".equals(price)){
            _price = Integer.parseInt(price);
        }else{
            throw new NumberFormatException("格式不正确");
        }

        Car car = new Car(_cno,cname,color,_price);
        System.out.println(car);
    }
}
