package com.example.classchat.Object;

import java.util.List;

public  class Object_Commodity implements Comparable{



    //TODO 图片 点赞方法修改
    private String itemID;
    private String itemName;
    private List<String> imageList;
    private String ownerID;
    private double price;
    private String briefIntroduction;
    private String detailIntroduction;
    private List<String> thumbedList;
    public Object_Commodity(){}
    public Object_Commodity(String itemID, String itemName, List<String> imageList, String ownerID, double price, String briefIntroduction, String detailIntroduction, List<String> thumbedList){
        this.itemID = itemID;
        this.itemName = itemName;
        this.imageList = imageList;
        this.ownerID = ownerID;
        this.price = price;
        this.briefIntroduction = briefIntroduction;
        this.detailIntroduction = detailIntroduction;
        this.thumbedList = thumbedList;//储存点赞过的用户ID
    }

    public String getItemID() {return itemID;}

    public String getItemName() {return itemName;}

    public List<String> getImageList() {return imageList;}

    public String getOwnerID() {return ownerID;}

    public  double getPrice(){return price;}

    public String getBriefIntroduction() {return briefIntroduction;}

    public String getDetailIntroduction() {return detailIntroduction;}

    public List<String> getThumbedList() {return thumbedList;}

    //这个用户如果点过赞 肯定在这个列表里面，那么就根据返回的bool值来修改UI
    public boolean getThumbsUpState(String id){return thumbedList.contains(id)?true:false;}

    //外部判断用户没点赞的情况下 将用户加入点赞列表
    public void addToThumbedList(String id){thumbedList.add(id);}

    //外部确认用户点过赞 取消点赞
    public void removeFromThumbedList(String id){thumbedList.remove(id);}

    //得到用户点赞的个数
    public int getThumbsUpCount(){
        if(thumbedList != null){
            return thumbedList.size();
        }
        else
            return 0;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setBriefIntroduction(String briefIntroduction) {
        this.briefIntroduction = briefIntroduction;
    }

    public void setDetailIntroduction(String detailIntroduction) {
        this.detailIntroduction = detailIntroduction;
    }

    public void setThumbedList(List<String> thumbedList) {
        this.thumbedList = thumbedList;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public String toString(){
        return itemName;
    }
}
