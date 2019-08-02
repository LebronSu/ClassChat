package com.example.classchat.Activity;

//手动添加课程页面

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.Button;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.example.classchat.Object.MySubject;
import com.example.classchat.R;
import com.example.classchat.Util.SharedUtil;
import com.example.classchat.Util.Util_BlockchainTool;
import com.example.library_cache.Cache;

import org.jetbrains.annotations.NotNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity_AddCourse extends AppCompatActivity {

    //各格子控件
    private EditText course;
    private EditText teacher;
    private EditText dayOfWeek;
    private EditText room;
    private EditText start;
    private EditText end;
    private TextView choose_week;
    private Button add;
    private Button back;

    private View view;

    //缓存
    private String mClassBoxData = "";

    //广播
    private LocalBroadcastManager localBroadcastManager;

    //提示框builder
    private AlertDialog.Builder builder1;
    private AlertDialog.Builder builder2;
    private AlertDialog.Builder builder3;
    //周数多选框
    private AlertDialog.Builder mutilChoicebuilder;
    //配合周数多选框的数组
    private final String[] weeks= new String[]{"第1周","第2周","第3周","第4周","第5周","第6周","第7周","第8周","第9周","第10周","第11周","第12周","第13周","第14周","第15周","第16周","第17周","第18周","第19周","第20周","第21周","第22周","第23周","第24周","第25周"};
    private boolean[] weeksChecked = new boolean[]{false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
    //周数数组
    List<Integer> weeksnum=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__add_course);

        //绑定控件
        add=(Button)findViewById(R.id.add_button);
        choose_week=(TextView)findViewById(R.id.choose_search_week);
        course=(EditText) findViewById(R.id.get_course_name) ;
        teacher=(EditText) findViewById(R.id.get_course_teacher);
        room=(EditText)findViewById(R.id.get_course_room);
        dayOfWeek=(EditText)findViewById(R.id.get_course_day);
        start=(EditText)findViewById(R.id.get_course_start);
        end=(EditText)findViewById(R.id.get_course_end);
        back=(Button)findViewById(R.id.back_from_addCourse_button);
        choose_week=(TextView)findViewById(R.id.choose_week);

        //周数多选框
        mutilChoicebuilder = new AlertDialog.Builder(this);
        mutilChoicebuilder.setTitle("选择周数");
        mutilChoicebuilder.setMultiChoiceItems(weeks, weeksChecked, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

            }
        });
        mutilChoicebuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String s = new String();
                int end = 0;
                for(int i=0;i<weeksChecked.length;i++){
                    if(weeksChecked[i])
                    weeksnum.add(i+1);
                }

                for(int i = 0; i < weeksChecked.length; i++)
                {
                    if (weeksChecked[i])
                    {

                        int start = i+1;
                        for(int j=i+1;j<=weeksChecked.length;++j) {
                            if (j == weeksChecked.length && weeksChecked[j - 1]) {
                                end = weeksChecked.length;
                                i = weeksChecked.length - 1;
                                break;
                            } else if (!weeksChecked[j]) {
                                end = j;
                                i = j;
                                break;
                            }
                        }
                        if(start==end)
                            s+="第"+start+"周 ";
                        else
                            s+="第"+start+"~"+end+"周 ";

                    }

                }
                if (weeksnum.size() > 0){
                    choose_week.setText(s);
                }else{
                    //没有选择
                    Toast.makeText(Activity_AddCourse.this, "未选择周数!", Toast.LENGTH_SHORT).show();
                }


            }
        });
        mutilChoicebuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        choose_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mutilChoicebuilder.show();
            }
        });

        localBroadcastManager = LocalBroadcastManager.getInstance(this);



        //信息补全提示框1
        builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("提示");
        builder1.setMessage("请填全课程信息哦");
        builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //信息补全提示框2
        builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("提示");
        builder2.setMessage("请填入正确的节次哦");
        builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //信息补全提示框3
        builder3 = new AlertDialog.Builder(this);
        builder3.setTitle("提示");
        builder3.setMessage("请填入正确的星期几哦");
        builder3.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });


        //返回
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //点击添加课程
        add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String room_=room.getText().toString();
                String course_=course.getText().toString();
                String teacher_=teacher.getText().toString();
                int dayOfWeek_;
                int start_;
                int end_;
                int step;

                //若有文本框未编辑
                if(TextUtils.isEmpty(teacher.getText())||TextUtils.isEmpty(course.getText())||TextUtils.isEmpty(room.getText())||TextUtils.isEmpty(dayOfWeek.getText())|| TextUtils.isEmpty(start.getText())||TextUtils.isEmpty(end.getText())||weeksnum.size()==0)
                {
                    builder1.show(); }
                else {
                    dayOfWeek_=Integer.parseInt(dayOfWeek.getText().toString());
                    start_=Integer.parseInt(start.getText().toString());
                    end_=Integer.parseInt(end.getText().toString());
                    step=end_-start_+1;
                    //若结束节次小于开始节次
                    if (end_ < start_||end_>12||start_<1) { builder2.show(); }
                    else if(dayOfWeek_<1||dayOfWeek_>7){builder3.show();}
                    else {
                        MySubject item = new MySubject( course_, room_, teacher_, weeksnum, start_, step, dayOfWeek_, null,0);

                        mClassBoxData= Cache.with(v.getContext())
                                .path(getCacheDir(v.getContext()))
                                .getCache("classBox", String.class);

                        List<MySubject> mySubjects = JSON.parseArray(mClassBoxData, MySubject.class);

                        mySubjects.add(item);

                        mClassBoxData=mySubjects.toString();

                        Log.v("mySubjects",mClassBoxData);

                        Cache.with(v.getContext())
                                .path(getCacheDir(v.getContext()))
                                .saveCache("classBox", mClassBoxData);

                        Intent intent1 = new Intent("com.example.broadcasttest.LOCAL_BROADCAST1");
                        localBroadcastManager.sendBroadcast(intent1);

                        Intent intent2 = new Intent();
                        intent2.setClass(Activity_AddCourse.this,MainActivity.class);
                        startActivity(intent2);
                    }
                }
            }
        });

    }
    /*
     * 获得缓存地址
     * */
    public String getCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }
}
