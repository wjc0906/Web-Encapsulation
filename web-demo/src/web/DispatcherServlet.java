package web;

import com.alibaba.fastjson.JSON;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import web.annotation.Controller;
import web.annotation.RequestMapping;
import web.annotation.RequestParam;
import web.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class DispatcherServlet extends HttpServlet {
    /*
            缓存请求映射信息
            key  = "/test1"
            value = mappingInfo{目标对象，目标方法}
         */
    Map<String,MappingInfo> mappingInfoMap = new HashMap<>();
    /*
       单实例存储controller对象
       key="com.controller.TestController"
       value= new Object
    */
    Map<String,Object> controllerMap = new HashMap<>();
    @Override
    public void init() throws ServletException {
        //目前配置信息来自于两个部分，而且两个配置都有可能存在
         // 进行读取xml配置文件的方法
        String xmlPath= super.getInitParameter("classpath");
        if (xmlPath != null&&!"".equals(xmlPath)){
            //在这指定了配置文件，需要读取配置文件
            //1.第一步去找xml文件的路径，第二步解析里面的内容
            readXml(xmlPath);
        }
        // 进行注解读取的方法
        String packagePath = super.getInitParameter("controller-scan");
        if (packagePath !=null && !"".equals(packagePath)){
            //指定了包路径，需要读取下类中的注解信息
        }
    }

    public void readXml(String path){
        //路径就是那个mvc.xml的路径，我们需要去out中去找路径
        path = Thread.currentThread().getContextClassLoader().getResource(path).getPath();
        System.out.println(path);
        try {
            // 创建一个输入流，来写里面的内容，将path也就是路径存储进去
            InputStream is = new FileInputStream(path);
            //使用dom4，sax读取xml内容
            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            //解析mapping标签   multipart-encoding
            parseMappingElement(document);

            //解析其他标签
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //读取xml中<mapping>请求映射信息
    private void parseMappingElement(Document document) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        //按照标签的嵌套规则，找到需要的所有mapping标记
        List<Element> mappingElements =  document.selectNodes("mvc/mapping") ;
        for(Element mappingElement : mappingElements ){
           /*
                获得标签指定的属性值
                标签中记载着请求与响应对应关系
                此时(初始化)获得这个对应关系，不是现在要用
                后面请求的时候用
                因为标签里面有很多的值，所有我们需要将他作为对象去
                存储起来，所以这个类出现了mappinginfo
            */
            String path = mappingElement.attributeValue("path") ;
            String classname = mappingElement.attributeValue("class") ; // 获取配置文件中存储的类。com.controller
            String methodname = mappingElement.attributeValue("method") ; // 获取方法 这个方法名叫t1

            //接下来通过反射创建目标对象。这是因为我们拿到的只是方法的名字，所以我们需要具体去用它，所以利用反射去创建它
            Class clazz = Class.forName(classname) ;
            Object controller = clazz.newInstance() ;

            //通过反射，根据方法名获得方法对象
            //获得mapping标签的子标签<type> 存储目标方法对应的参数列表
            List<Element> typeElements = mappingElement.selectNodes("type") ;
            //创建一个与配置文件中<type>数量相同的Class数组，准备装载目标方法对应的参数列表类型
            //将xml文件中的type标签存入数组中
            Class[] types = new Class[typeElements.size()] ;
            int i = 0 ;
            // 遍历存type的这个数组
            for(Element typeElement : typeElements){
                //<type> 获得标签的文本内容 也就是int
                String typeStr = typeElement.getText() ;
                //需要将int什么的变成。class文件我们才可以用
                types[i++] = castStringToClass(typeStr) ;
            }
            Method method = clazz.getMethod(methodname,types) ;//因为这里的types里面有很多种类型，所以用数组types去存储

            MappingInfo info = new MappingInfo(path,controller,method);
            mappingInfoMap.put(path,info) ;
        }
    }
    /*
     *  将字符串表示的类型转换成对应的Class类型
     *  "int" -> int.class
     *  "java.lang.String" -> String.class -> Class.forName("java.lang.String")
     */
    private Class castStringToClass(String typeStr) throws ClassNotFoundException {
        if("int".equals(typeStr)){
            return int.class;
        }
        if("long".equals(typeStr)){
            return long.class;
        }
        if("double".equals(typeStr)){
            return double.class;
        }
        //除去8种基本类型，以外的都是引用类型，写的都是全名，反射生成Class
        return Class.forName(typeStr) ;
    }
     public Object getSingleController(String classname) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
     Class calzz = Class.forName(classname);
     Object controller = controllerMap.get(classname);
     if (controller==null){
         controller=calzz.newInstance();
         controllerMap.put(classname,controller);
     }
         return controller;
     }

     //读取注解的方法
    public void readAnnotation(String packagePath) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        //packagePtah 这个代表我们web.xml文件配置的controller1和controller2
        //所以我们需要框架从指定的两个包中找到映射的注解
        //两个包是这样的PackagepathArray= {"controller1","controller2"}
        //所以我们需要将其拆分
        String[] packagePathArray=packagePath.split(",");
        //接下来遍历这个数组
        for (String ppath:packagePathArray){
            //此时ppath=com.controller
            // 但是我们要想反射要获取他的类名，ppath知识这个包的路径  获取到com.controller+TestController
            //如何获取这个的类名呢？反射是不可能的了，所以我们可以选择获取文件名，去掉后缀就是类名了
            //需要先将com.controller变成com/controller
            String dirpath = ppath.replace(".","/");
            //因为真实的路径在out下，所有我们获取的只是这个类的相对路径
            try{
                dirpath=Thread.currentThread().getContextClassLoader().getResource(dirpath).getPath();

            }catch (NullPointerException e ){
                //执行catch这个语句就代表没有找到路径，所以我们做一个输出告诉一下
                System.out.println("login:这个包的路径没有找到["+dirpath+"]");
                continue;
            }
            File dir = new File(dirpath);
            //以上文件夹中的内容就读取出来了
            //接下来获取文件夹中的所有子内容
            String[] fnames = dir.list();
            for (String fname : fnames) {
                //此时这个fname=TestController.class
                if (fname.endsWith(".class")){
                    int i = fname.indexOf(".");
                    //然后将后面的class截取掉
                    String className = fname.substring(0,i);
                    //这样这个类名就出来了
                    //将整成能反射的具体类名
                    //class = "com.controller.TestController"
                    className = ppath+"."+className;
                    //接下来就可以反射了，因为找到具体的类了，可以找具体的注解了
                    Class clazz = Class.forName(className);
                    Controller c = (Controller) clazz.getAnnotation(Controller.class);
                    if (c == null){
                        //当前包中的这个类没有设置@Controller,类中没有请求映射信息
                        continue ;
                    }

                    //这是为了解决直接跳转页面请求的
                    //有@Controller,类中有请求映射关系
                    //遍历类中所有的方法，找到@RequestMapping这个注解
                    Method[] methodArray = clazz.getMethods();
                    for (Method method : methodArray) {
                       RequestMapping rm = method.getAnnotation(RequestMapping.class);
                       if (rm==null){
                           continue;
                       }
                       //方法中有@RequestMapping，表示是一个映射请求的方法
                        String path = rm.value();
                       //获取controller
                       Object controller = getSingleController(className);
                       MappingInfo info = new MappingInfo(path,controller,method);
                       Object obj = mappingInfoMap.get(path);
                       if (obj!=null){
                           //当前请求映射关系已经存在
                           //去重 continue
                           //覆盖 map.put
                           //抛出异常 throw new Exception
                       }

                        mappingInfoMap.put(path,info) ;
                    }
                }
            }
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    //------------------------------------初始化处理结束-----------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------------------------

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //请求映射，根据请求调用controller的方法
        //获取请求 test1
        //req.getRequestURL(); // 获取全部的url串http://localhost:8080/web-demo/test1
        String uri = request.getRequestURI() ;// 获取这样的/web-demo/test1
        String root = request.getContextPath() ;// 获取这样的/web-demo
        String path = uri.replace(root,""); // 获取这样的/test1

        //根据请求找到对应的mapping映射信息
        MappingInfo info = mappingInfoMap.get(path);
        if (info==null){
            //这里的null代表没有找到注解，没有需要调用的controller方法
            //有可能这次请求的是一些静态文件 .java .html .js .jpg
            //之前在没有框架的时候tomacat专门提供了一个处理静态资源访问的DefaultServlet
            //该servlet处理的请求模式就是/
            //现在我们的Dispatcherservlet一半也建议配置成/
            // 这样可以覆盖tomcat自带的DefaultServlet了
            //这样我们框架就可以来处理这些静态资源
            handleStaticResource(path,request,response);
        }else {
            //扎到映射信息了，就按照映射信息分发请求，调用controller.method
            try {
                haddleDynammicResource(info,request,response);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }


//分发请求
    private void haddleDynammicResource(MappingInfo info,HttpServletRequest request,HttpServletResponse response) throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException {

        //获得参数 利用map集合去存储
        Map<String , Object> paramMap = receiveParams(request);
        //处理参数
        Object[] paramValues = handleParam(paramMap,info,request,response);

        //调用mappinginfo中的对象方法，传递参数
    }
//作响应
    private void handleResponse(Object result,Method method,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
        if (result == null){
            //框架不需要做响应
            return;
        }
        ResponseBody rb = method.getAnnotation(ResponseBody.class);
        if (rb == null){
            //证明有注解  直接响应
            if (
                    result instanceof String ||
                            result instanceof Integer ||
                            result instanceof Long ||
                            result instanceof Boolean
            ){
                //简单类型，都变成字符串，直接响应
                response.getWriter().write(result.toString());
            }else{
                //不是简单类型，就是集合对象，需要转换成json再响应
                String json = JSON.toJSONString(result);
                response.setContentType("text/json");
                response.getWriter().write(json);
            }
        }else{
            //间接响应
            if (result instanceof String ){
             //不需要携带数据
             String result1 = (String)result;
             if (result1.startsWith("redirect:")){
                 //重定向 “redirect：05.jsp”  redirect是认为规定的必须这么写
                 //所以我们才会去判断这个
                 //将前缀去掉
                 result1 = result1.replace("redirect:","");
                 response.sendRedirect(result1);
             }else{
                 //转发05.jsp
                 request.getRequestDispatcher(result1).forward(request,response);
             }
            }else{
                //就是modelandview，需要携带数据
                ModelAndView result1 = (ModelAndView) result;
                //path = "05.jsp"/"redirect:05.jsp"
                String path = result1.getViewName();
                if (path.endsWith("redirect:")){
                    //重定向携带数据 05.jsp？...
                    path.replace("redirect:","");
                    path+="?";
                    Set<String> names = result1.getObjectNames();
                    for (String name: names){
                        Object value = result1.getObject(name);
                        path+=name+"="+value+"&";
                    }
                    //05.jsp?a=1&b+2&c=3
                    response.sendRedirect(path);
                }else {
                    //转发携带数据
                    Set<String> names = result1.getObjectNames();
                    for (String name : names){
                        Object value = result1.getObject(name);
                        request.setAttribute(name,value);
                    }
                    request.getRequestDispatcher(path).forward(request,response);
                }
            }
        }
    }

    /*
      按照请求映射关系要调用的那个目标方法
      获得目标方法的参数列表
      根据参数列表获得所需要的参数
      并将参数组装Object[]中
   */
    private Object[] handleParam(Map<String , Object> paramMap,MappingInfo info,HttpServletRequest request , HttpServletResponse response) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        //获取mappingInfo中的方法
        Method method = info.getMethod();
        //将这些方法存入数组中
        Parameter[] parameters = method.getParameters();
        //创建一个与方法参数列表同长度的数组装载数据（实参）
        Object[] paramValues = new Object[parameters.length];
        int i = 0 ;
        for (Parameter parameter : parameters) {
            //获取注解
            RequestParam rp = parameter.getAnnotation(RequestParam.class);
            Class paramType = parameter.getType() ;
            if (rp != null){
                //证明有这个注解  @RequestParam("cname") String cname
                //表示需要请求传递的一个叫cname名字的参数
                //请求传递的参数之前已经处理过了
                //处理后将请求传递的参数存入了paramMap集合中，key=cname,value=[]
                String key = rp.value();//获取注解中的名字
                Object value = paramMap.get(key);
                if(value == null){
                    //当前所需要的的参数没有值，默认是null，之间找下一个参数值
                    //i++ ;
                    continue ;
                }
                //如果参数值存在，起初只有2种表现形成 String[] , MulitpartFile[]

                //将此次需要的参数value，转换成方法中定义的需要参数类型
                paramValues[i++] = casType(value,paramType);
            }else{
                //没有注解
                //可能是request，response，session domain(组装参数User，car，Goods)
                if (paramType == HttpServletRequest.class){
                    paramValues[i++] = request ;
                }else if(paramType == HttpServletResponse.class){
                    paramValues[i++] = response;
                }else if ( paramType == HttpSession.class){
                    paramValues[i++]= request.getSession();
                }else{
                   //是一个domain类型，假设是paramType = car.class
                   //将此次请求的多个参数，最终组成一个car对象
                    Object paramObject = paramType.newInstance() ;//在这相当于又新new了一个对象
                    //通过反射获得对象中的属性名，根据属性名找到与之同名的参数，为属性赋值
                    //为了更好的符合java封装的特性，不建议通过反射找属性，建议通过反射找对应的set方法
                    Method[] methods = paramType.getMethods();
                    for (Method m : methods) {
                        String mname = m.getName();
                        if (mname.startsWith("set")){
                            String key = mname.substring(3) ;//去掉set-->Cname
                            key = key.substring(0,1).toLowerCase() + key.substring(1);//就剩cname
                            Object value = paramMap.get(key);
                            if (value == null){
                                i++;
                                continue;
                            }
                            //根据domain的set方法(属性名)找到了与之对应的参数
                            //该参数起初只是String[],MultipartFile[]类型
                            //需要转换成domain中属性的类型
                            Class domainParamType = m.getParameterTypes()[0];// void setCname(String cname) -> String 获取类中的第一个参数
                            Object v = casType(value,domainParamType);
                            m.invoke(paramObject,v);
                        }
                    }
                    //循环结束，找到了所有的set方法。为对象的属性赋值，赋值结束将对象参数保存
                    paramValues[i++] = paramObject;
                }
            }



        }

        return  paramValues;
    }

    private Object casType(Object value,Class paramType){
        if(paramType == String.class){
            String str = ((String[])value)[0] ;
            return str ;
        }
        if(paramType == int.class || paramType==Integer.class){
            String str = ((String[])value)[0] ;
            int num = Integer.parseInt(str) ;
            return num ;
        }

        if(paramType == long.class || paramType==Long.class){
            String str = ((String[])value)[0] ;
            long num = Long.parseLong(str) ;
            return num ;
        }

        if(paramType == double.class || paramType==Double.class){
            String str = ((String[])value)[0] ;
            double num = Double.parseDouble(str) ;
            return  num ;
        }

        if(paramType == String[].class){
            return value ;
        }

        //url?cname=1&cname=2&cname3 =》框架会存储成数组[1,2,3]
        if(paramType == int[].class){
            String[] ss =  (String[])value ;
            int[] nums = new int[ss.length];
            for(int j=0;j< ss.length;j++){
                nums[j] = Integer.parseInt(ss[j]);
            }
            return nums ;
        }

        if(paramType == Integer[].class){
            String[] ss =  (String[])value ;
            Integer[] nums = new Integer[ss.length];
            for(int j=0;j< ss.length;j++){
                nums[j] = Integer.valueOf(ss[j]);
            }
            return nums ;
        }

        if(paramType == MulitpartFile.class){
            return ((MulitpartFile[])value)[0];
        }

        if(paramType == MulitpartFile[].class)
            return value ;

        return null ;
    }

    /*
     * 接收请求传递从参数
     * 将参数处理后装入map
     *  map.key就是请求传递参数key <input type="uname" />  map.key=uname
     *  map.value 可能是一个 String[] 也可能是一个 文件[] -> Object表示
     */
   private Map<String,Object> receiveParams(HttpServletRequest request) throws IOException {
       Map<String,Object> paramMap = new HashMap<>();
       //参数有2种可能 (普通请求，文件上传请求)
       //如果普通请求按照文件上传方式处理，会抛出异常
       //如果文件上传请求按照普通请求处理会得不到参数
           //假设是文件上传方式传递参数
           try {
               DiskFileItemFactory factory = new DiskFileItemFactory();
               ServletFileUpload upload = new ServletFileUpload(factory);
               //该行会判断是否是文件上传请求，是否配置了enctype="multipart/form-data"，如果不是会抛出异常
               List<FileItem> fis = upload.parseRequest(request);

               //确实是一个文件上传的请求，同时因为上一行代码，已经将请求的参数都组成了FileItem对象，装入了List集合
               for(FileItem fi : fis){
                   //拿到了一个参数
                   if(fi.isFormField()){
                       // 是一个普通参数，String
                       String key = fi.getFieldName() ;// <input name="uname" />
                       String value = fi.getString("utf-8") ;

                       String[] values = (String[]) paramMap.get(key);
                       if(values == null){
                           //还没有存储过当前这个名字的参数
                           values = new String[]{value} ;
                           paramMap.put(key,values) ;
                       }else{
                           //当前名字的参数之前存储过，将当前的数据装入数组，数组扩容+1
                           values = Arrays.copyOf(values,values.length+1) ;
                           values[ values.length-1 ] = value ;
                           paramMap.put(key,values) ;
                       }
                       paramMap.put(key,value) ;
                   }else{
                       // 是一个文件参数
                       String key = fi.getFieldName() ; // <input type="file" name="excel" />

                       String fileName = fi.getName() ; //文件名 1.jpg
                       long size = fi.getSize() ;//获得文件大小
                       String contentType = fi.getContentType() ; //文件的类型
                       InputStream is = fi.getInputStream() ;//文件内容

                       MulitpartFile file = new MulitpartFile(key,fileName,size,contentType,is);

                       MulitpartFile[] files = (MulitpartFile[]) paramMap.get(key);
                       if(files == null){
                           //当前名字的文件参数之前没有
                           files = new MulitpartFile[]{file} ;
                           paramMap.put(key,files);
                       }else{
                           //之前存储过这个名字的文件参数
                           files = Arrays.copyOf(files,files.length+1);
                           files[ files.length-1 ] = file ;
                           paramMap.put(key,files) ;
                       }
                   }
               }
           } catch (FileUploadException e) {
               //出现异常表示不是文件上传方式，再按照普通方式处理
               Enumeration<String> names = request.getParameterNames();
               while(names.hasMoreElements()){
                   String name = names.nextElement() ; // name="uname"
                   String[] values = request.getParameterValues(name);
                   paramMap.put(name,values) ;
               }
           }
       return paramMap;
   }






//处理静态资源，在mapping映射信息中没有找到就表示静态资源
    private void handleStaticResource(String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(path.equals("/")){
            //获得/表示的默认资源->/index.html
            path = request.getServletPath();
        }
        //path="/index.html"
        //根据请求的相对路径，找到对应文件在服务器端存储的绝对路径
        //Thread.currentThread...()获得classes目录下的文件路径
        //request.getServletContext().realPath()获得服务器目录下的文件路径
        //path ="F:\dmc\idea_workspace\web-demo\out\artifacts\web_demo_war_exploded\index.html"
        String fpath = request.getServletContext().getRealPath(path);
        File file = new File(fpath) ;
        if(!file.exists()){
            //能执行当前方法，表示此次请求的不是一个动态资源
            //文件不存在，表示此次请求的静态资源不存在
            //表示此次没有找到对应的资源
            response.sendError(404,"["+path+"]");
            return ;
        }

        InputStream is = new FileInputStream(file);
        OutputStream os = response.getOutputStream() ;
        byte[] bs = new byte[0x100] ;//16*16=256
        while(true){
            int len = is.read(bs) ;
            if(len == -1)break ;
            os.write(bs,0,len);
        }
        os.flush();
        is.close();
    }
 }


