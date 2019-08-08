package com.example.classchat.Object;

public class Course {
    private String className;
    private String signTime;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSignTime() {
        return signTime;
    }

    public void setSignTime(String signTime) {
        this.signTime = signTime;
    }

    public Course(String className, String signTime) {
        this.className = className;
        this.signTime = signTime;
    }
}
