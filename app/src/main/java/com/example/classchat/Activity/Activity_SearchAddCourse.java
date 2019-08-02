package com.example.classchat.Activity;

//自动添加课程页面

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.example.classchat.Object.MySubject;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.example.library_cache.Cache;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity_SearchAddCourse extends AppCompatActivity {
    //各格子控件
    private TextView course;
    private TextView teacher;
    private EditText dayOfWeek;
    private EditText room;
    private EditText start;
    private EditText end;
    private TextView choose_week;
   //返回
    private Button back;
    private Button add;

    //操作周数数组
    private final String[] weeks= new String[]{"第1周","第2周","第3周","第4周","第5周","第6周","第7周","第8周","第9周","第10周","第11周","第12周","第13周","第14周","第15周","第16周","第17周","第18周","第19周","第20周","第21周","第22周","第23周","第24周","第25周"};
    private boolean[] weeksChecked = new boolean[]{false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
    //周数选择对话框
    private AlertDialog.Builder mutilChoicebuilder;
    //提示对话框
    private AlertDialog.Builder builder1;
    private AlertDialog.Builder builder2;
    private AlertDialog.Builder builder3;
    private AlertDialog.Builder builder4;
    //周数数组
    List<Integer> weeksnum=new ArrayList<>();

    //缓存
    private String mClassBoxData;

    //广播
    private LocalBroadcastManager localBroadcastManager;

    private final static int CHANGE_VIEW = 100;

    //handler处理反应回来的信息
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case CHANGE_VIEW:
                    Intent intent1 = new Intent("com.example.broadcasttest.LOCAL_BROADCAST1");
                    localBroadcastManager.sendBroadcast(intent1);
                    Intent intent2 = new Intent(Activity_SearchAddCourse.this, MainActivity.class);
                    startActivity(intent2);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__search_add_course);

        //从所选的搜索条目获取信息
        Intent intent=getIntent();
        final String id= (String) intent.getSerializableExtra("id");
        final String course_name=(String)intent.getSerializableExtra("course");
        final String teacher_name=(String)intent.getSerializableExtra("teacher");
        final String userId = (String)intent.getSerializableExtra("userId");

        //绑定控件
        add=(Button)findViewById(R.id.add_search_button);
        back=(Button)findViewById(R.id.back_from_addCourse2_button);
        choose_week=(TextView)findViewById(R.id.choose_search_week);
        course=(TextView)findViewById(R.id.get_search_course_name) ;
        teacher=(TextView)findViewById(R.id.get_search_course_teacher);
        room=(EditText)findViewById(R.id.get_search_course_room);
        start=(EditText)findViewById(R.id.get_search_course_start);
        end=(EditText)findViewById(R.id.get_search_course_end);
        dayOfWeek=(EditText)findViewById(R.id.get_search_course_day);

        //接收上一级页面参数
        course.setText(course_name);
        teacher.setText(teacher_name);


        //周数选择多选框
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
                    Toast.makeText(Activity_SearchAddCourse.this, "未选择周数!", Toast.LENGTH_SHORT).show();
                }


            }
        });
        mutilChoicebuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //点击周数选择出现对话框
        choose_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mutilChoicebuilder.show();
            }
        });

        //广播
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

        //信息补全提示框4
        builder4 = new AlertDialog.Builder(this);
        builder4.setTitle("提示");
        builder4.setMessage("您当前添加的课程与现有课程表存在冲突(这个时间段已经有此课程了),请先删除原有课程！");
        builder4.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });


        //点击返回
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //点击添加课程
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String room_=room.getText().toString();
                final int dayOfWeek_;
                final int start_;
                int end_;
                final int step;
                if(TextUtils.isEmpty(room.getText())||TextUtils.isEmpty(dayOfWeek.getText())|| TextUtils.isEmpty(start.getText())||TextUtils.isEmpty(end.getText())||weeksnum.size()==0)
                {builder1.show();}
                else{
                    dayOfWeek_=Integer.parseInt(dayOfWeek.getText().toString());
                    start_=Integer.parseInt(start.getText().toString());
                    end_=Integer.parseInt(end.getText().toString());
                    step=end_-start_+1;
                    if(end_<start_||end_>12||start_<1) {builder2.show();}
                    else if(dayOfWeek_<1||dayOfWeek_>7){builder3.show();}
                    else {
                        final MySubject item = new MySubject( course_name, room_, teacher_name, weeksnum, start_, step, dayOfWeek_,id,0);
                        if(isLegal(item)){
                            System.out.println(userId);
                            RequestBody requestBody = new FormBody.Builder()
                                    .add("userId",userId)
                                    .add("subject", JSON.toJSONString(item))
                                    .build();

                            Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/addcourse", requestBody, new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    boolean responseData = Boolean.parseBoolean(response.body().string());
                                    System.out.println(responseData);
                                    if (responseData) {

                                        mClassBoxData= Cache.with(v.getContext())
                                                .path(getCacheDir(v.getContext()))
                                                .getCache("classBox", String.class);

                                        List<MySubject> mySubjects = JSON.parseArray(mClassBoxData, MySubject.class);

                                        mySubjects.add(item);

                                        mClassBoxData=mySubjects.toString();

                                        Cache.with(v.getContext())
                                                .path(getCacheDir(v.getContext()))
                                                .saveCache("classBox", mClassBoxData);
                                        Message message = new Message();
                                        message.what = CHANGE_VIEW;
                                        handler.sendMessage(message);
                                    }
                                }
                            });
                        }else{
                            builder4.show();
                        }
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

    public Boolean isLegal(MySubject subject) {

        List<MySubject> doubtfulList = new ArrayList<>();

        mClassBoxData = Cache.with(Activity_SearchAddCourse.this)
                .path(getCacheDir(Activity_SearchAddCourse.this))
                .getCache("classBox", String.class);

        List<MySubject> mySubjects = JSON.parseArray(mClassBoxData, MySubject.class);

        for (MySubject item : mySubjects) {
            //先筛选出可以列表
            if ((item.getDay() == subject.getDay()) && !( (item.getStart() > (subject.getStart() + subject.getStep() - 1))|| ((item.getStart() + item.getStep() - 1) < subject.getStart())) ) {
                doubtfulList.add(item);
            }
        }
        //接下来判断这些可以的item会不会有和要添加的subject周数重合的
        for(MySubject sub : doubtfulList){
            List<Integer> week1 = sub.getWeekList();
            List<Integer> week2 = subject.getWeekList();
            week1.retainAll(week2);
            if(!week1.isEmpty())
                return false;
        }
        return true;
    }

}
