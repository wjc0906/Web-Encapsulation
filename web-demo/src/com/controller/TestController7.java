package com.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


//@Auth("com.duyi.qxgl.user")
public class TestController7 extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("测试7");

//        resp.setContentType("text/html;charset=utf-8"); //即设置响应时编码，有设置浏览器处理的编码
        resp.setCharacterEncoding("utf-8");//设置响应时的编码，而不影响浏览器处理的编码
        resp.getWriter().write("直接响应文本，注意中文编码");

    }
}
