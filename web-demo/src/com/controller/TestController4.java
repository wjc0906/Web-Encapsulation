package com.controller;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@WebServlet("/test4")
public class TestController4 extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("测试4");

        req.setCharacterEncoding("utf-8");
        try{
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory) ;
            List<FileItem> fis = upload.parseRequest(req);

            for(FileItem fi : fis){
                if(fi.isFormField()){
                    String key = fi.getFieldName() ;
                    String value = fi.getString("utf-8");
                    System.out.println("key="+key+",value="+value);
                }else{
                    String key = fi.getFieldName();
                    String filename = fi.getName() ;
                    long size = fi.getSize() ;
                    InputStream is = fi.getInputStream() ;
                    System.out.println("key="+key+",filename="+filename+",size="+size);

                    //io流操作
                    OutputStream os = new FileOutputStream("d:/z/"+filename);
                    while(true){
                        int b = is.read() ;
                        if(b == -1)break ;
                        os.write(b);
                    }
                    is.close();
                    os.close();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
