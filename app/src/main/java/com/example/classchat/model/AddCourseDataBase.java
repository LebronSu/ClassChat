package com.example.classchat.model;

//储存搜索条目数据的类

public class AddCourseDataBase {
    private String id;
    private String courseName;
    private  String teacher;


    public AddCourseDataBase(String id,String courseName,String teacher){
        this.id=id;
        this.courseName=courseName;
        this.teacher=teacher;

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }




}
