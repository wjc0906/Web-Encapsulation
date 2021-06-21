package web;

import java.lang.reflect.Method;

/**
 * 存储请求映射关系
 */
public class MappingInfo {
    private String  path ;//   /test1 请求命令
    private Object controller ; //  TestController 目标对象
    private Method method  ;// t1()  目标方法

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public MappingInfo(String path, Object controller, Method method) {
        this.path = path;
        this.controller = controller;
        this.method = method;
    }

    public MappingInfo() {
    }
}
