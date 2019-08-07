package com.example.classchat.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.classchat.Object.MySubject;
import com.example.classchat.R;

import java.util.List;

public class Adapter_Course extends RecyclerView.Adapter<Adapter_Course.ViewHolder> {
    private List<MySubject> mySubjectList;

    public Adapter_Course(List<MySubject> mySubjects) {
        mySubjectList = mySubjects;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseName;
        TextView courseWeeks;
        TextView coursePlace;
        TextView courseTeacher;
        TextView courseSignTimes;

        public ViewHolder(View view) {
            super(view);
            courseName = view.findViewById(R.id.courseitem_coursename);
            courseWeeks = view.findViewById(R.id.courseitem_courseweeks);
            coursePlace = view.findViewById(R.id.courseitem_courseplace);
            courseTeacher = view.findViewById(R.id.courseitem_courseteacher);
            courseSignTimes = view.findViewById(R.id.courseitem_coursesigntimes);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.course_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        MySubject mySubject = mySubjectList.get(position);
        viewHolder.courseName.setText(mySubject.getName());
        viewHolder.coursePlace.setText(mySubject.getRoom());
        viewHolder.courseTeacher.setText(mySubject.getTeacher());
        viewHolder.courseSignTimes.setText(mySubject.getId());
        viewHolder.courseWeeks.setText(mySubject.getWeekList().size());
    }

    @Override
    public int getItemCount() {
        return mySubjectList.size();
    }
}
