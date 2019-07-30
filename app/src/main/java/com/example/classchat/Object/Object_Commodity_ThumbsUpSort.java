package com.example.classchat.Object;

import java.util.List;

public class Object_Commodity_ThumbsUpSort extends Object_Commodity implements Comparable{

    public Object_Commodity_ThumbsUpSort(String itemid, String itemName, List<String> imageList, String ownerID, double price, String briefIntroduction, String detailIntroduction, List<String> thumbedList)
    {
        super(itemid, itemName, imageList, ownerID, price, briefIntroduction, detailIntroduction, thumbedList);
    }

    @Override
    public int compareTo(Object o) {
        Object_Commodity_ThumbsUpSort c =(Object_Commodity_ThumbsUpSort) o;
        if(this.getThumbsUpCount() < c.getThumbsUpCount())
            return -1;
        else if(this.getThumbsUpCount() > c.getThumbsUpCount())
            return 1;
        else if(this.getThumbsUpCount() == c.getThumbsUpCount())
            return 0;
        else
            return 2;
    }
}
