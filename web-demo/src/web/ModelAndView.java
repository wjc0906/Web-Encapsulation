package web;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModelAndView {
    private String viewName ;// 05.jsp
    //request.setAttribute(key1,value1);
    //request.setAttribute(key2,value2);
    //request.setAttribute(key3,value3);
    private Map<String,Object> values = new HashMap<>();

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void addObject(String key,Object value){
        values.put(key,value) ;
    }
    public Object getObject(String key){
        return values.get(key) ;
    }

    public Set<String> getObjectNames(){
        return values.keySet() ;
    }
}
