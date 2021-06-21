package com.domain;

public class Car {
    private Integer cno ;
    private String cname ;
    private String color ;
    private Integer price ;

    public Integer getCno() {
        return cno;
    }

    public void setCno(Integer cno) {
        this.cno = cno;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Car(Integer cno, String cname, String color, Integer price) {
        this.cno = cno;
        this.cname = cname;
        this.color = color;
        this.price = price;
    }

    public Car() {
    }

    @Override
    public String toString() {
        return "Car{" +
                "cno=" + cno +
                ", cname='" + cname + '\'' +
                ", color='" + color + '\'' +
                ", price=" + price +
                '}';
    }
}
