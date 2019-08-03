package com.example.classchat.Object;

import com.example.library_activity_timetable.model.Schedule;
import com.example.library_activity_timetable.model.ScheduleEnable;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义实体类需要实现ScheduleEnable接口并实现getSchedule()
 *
 * @see ScheduleEnable#getSchedule()
 */
public class MySubject implements ScheduleEnable {

    public static final String EXTRAS_ID = "extras_id";
    public static final String EXTRAS_AD_URL = "extras_ad_url";
    private String id;
    private String name;
    private String teacher;
    private int messageCount;
    private String room;
    private List<Integer> weekList = new ArrayList<>();
    private int start;
    private int step;
    private int day;
    private String url;


    public MySubject() {
        // TODO Auto-generated constructor stub
    }

    public MySubject(String name, String room, String teacher, List<Integer> weekList, int start, int step, int day, String id, int messageCount) {
        super();
        this.id = id;
        this.name = name;
        this.room = room;
        this.teacher = teacher;
        this.weekList = weekList;
        this.start = start;
        this.step = step;
        this.day = day;
        this.messageCount = messageCount;

    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTeacher() {
        return teacher;
    }
    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }
    public String getRoom() {
        return room;
    }
    public void setRoom(String room) {
        this.room = room;
    }
    public List<Integer> getWeekList() {
        return weekList;
    }
    public void setWeekList(List<Integer> list){this.weekList =list;}
    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }
    public int getStep() {
        return step;
    }
    public void setStep(int step) {
        this.step = step;
    }
    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public int getMessageCount() {
        return messageCount;
    }
    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }


    @Override
    public Schedule getSchedule() {
        Schedule schedule = new Schedule();
        schedule.setId(getId());
        schedule.setDay(getDay());
        schedule.setName(getName());
        schedule.setRoom(getRoom());
        schedule.setStart(getStart());
        schedule.setStep(getStep());
        schedule.setTeacher(getTeacher());
        schedule.setWeekList(getWeekList());
        schedule.putExtras(EXTRAS_ID, getId());
        schedule.putExtras(EXTRAS_AD_URL, getUrl());
        schedule.setMessagecount(getMessageCount());
        return schedule;
    }
}

