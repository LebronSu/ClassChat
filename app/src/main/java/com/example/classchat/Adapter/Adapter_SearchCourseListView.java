package com.example.classchat.Adapter;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.view.LayoutInflater;

import com.example.classchat.Activity.Activity_SearchAddCourse ;
import com.example.classchat.model.AddCourseDataBase;
import com.example.classchat.R;

import java.util.List;

public class Adapter_SearchCourseListView extends BaseAdapter {
    LayoutInflater inflater;
    List<AddCourseDataBase> ls;
    Context mContext;
    String userId;

    AddCourseDataBase medium;

    public Adapter_SearchCourseListView(Context context, List<AddCourseDataBase> objects, String userId){
        mContext=context;
        this.inflater=LayoutInflater.from(context);
        this.ls=objects;
        this.userId = userId;
    }

    public boolean isEnabled(int position) {
        return false;
    }
    @Override
    public int getCount() {
        return ls.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AddCourseDataBase item=ls.get(position);
        View v=inflater.inflate(R.layout.add_search_item,null);
        TextView courseName= (TextView) v.findViewById(R.id.searchCourseName);
        TextView teacher= (TextView) v.findViewById(R.id.searchTeacher);
        Button choose=(Button)v.findViewById(R.id.searchChoose);
        courseName.setText(item.getCourseName());
        teacher.setText(item.getTeacher());

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, Activity_SearchAddCourse.class);
                intent.putExtra("userId", userId);
                intent.putExtra("course",item.getCourseName());
                intent.putExtra("id",item.getId());
                intent.putExtra("teacher",item.getTeacher());
                mContext.startActivity(intent);
            }
        });
        return v;
    }

}

