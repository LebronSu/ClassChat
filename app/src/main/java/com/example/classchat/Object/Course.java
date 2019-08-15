package com.example.classchat.Object;

public class Course {
    private String className;
    private Integer signTime;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getSignTime() {
        return signTime;
    }

    public void setSignTime(Integer signTime) {
        this.signTime = signTime;
    }

    public Course(String className, Integer signTime) {
        this.className = className;
        this.signTime = signTime;
    }
}
