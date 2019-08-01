package com.example.classchat.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.classchat.Activity.Activity_AddSearchCourse;
import com.example.classchat.Activity.Activity_AutoPullCourseFromWeb;
import com.example.classchat.Activity.MainActivity;
import com.example.classchat.Object.MySubject;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.example.library_activity_timetable.Activity_TimetableView;
import com.example.library_activity_timetable.listener.ISchedule;
import com.example.library_activity_timetable.listener.IWeekView;
import com.example.library_activity_timetable.model.Schedule;
import com.example.library_activity_timetable.view.WeekView;
import com.example.library_cache.Cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

import org.jetbrains.annotations.NotNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Fragment_ClassBox extends Fragment implements OnClickListener {

    private static final String TAG = "Activity_Main_Timetable";

    //控件
    Activity_TimetableView mTimetableView;
    WeekView mWeekView;
    ImageButton moreButton;
    LinearLayout layout;
    TextView titleTextView;
    List<MySubject> mySubjects = new ArrayList<MySubject>();

    //学生ID
    private String userId;

    //学生专业
    private String proUni;

    //学生真实姓名
    private String realName;

    //学生是否实名认证
    private Boolean isAuthentation;

    //对话框
    Dialog coursedetail_dialog;

    //记录切换的周次，不一定是当前周
    int target = -1;

    // 搞一个自己的变量
    Fragment_ClassBox myContext = this;

    //缓存
    private String mClassBoxData = "";

    private UPDATEcastReceiver updatereceiver;
    private UpdateStateReceiver updateStateReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private Context mcontext;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.activity__main__timetable, container, false);

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mcontext = this.getActivity();
        MainActivity mainActivity = (MainActivity)getActivity();
        userId = mainActivity.getId();
        isAuthentation = mainActivity.getAuthentation();
        realName = mainActivity.getRealName();
        proUni = mainActivity.getProUni();
        moreButton =(ImageButton)getActivity().findViewById(R.id.id_more);
        moreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopmenu();
            }
        });

        titleTextView = (TextView)getActivity().findViewById(R.id.id_title);
        layout = (LinearLayout)getActivity().findViewById(R.id.id_layout);
        layout.setOnClickListener(this);

        //广播接收
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        //广播接收
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcasttest.LOCAL_BROADCAST");
        updatereceiver = new UPDATEcastReceiver();
        localBroadcastManager.registerReceiver(updatereceiver, intentFilter);
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction("com.example.broadcasttest.UPDATE_STATE");
        updateStateReceiver = new UpdateStateReceiver();
        localBroadcastManager.registerReceiver(updateStateReceiver, intentFilter1);
        initClassBoxData();

        initTimetableView();
    }

    /**
     * 模拟数据读取与存储
     */
    private void initClassBoxData(){


        Cache.with(myContext.getActivity())
                .path(getCacheDir(myContext.getActivity()))
                .remove("classBox");

//        mClassBoxData="[{'id':'123', 'name':'计算机', 'teacher':'ABC', 'room':'A1-101', 'stringweeklist':'5,6,7', 'start':'1', 'step':'4', 'day':'2', 'messagecount':'3'},  {'id':'456', 'name':'网络工程', 'teacher':'ABC', 'room':'A1-101', 'stringweeklist':'5,6,7', 'start':'1', 'step':'4', 'day':'4', 'messagecount':'0'}]";


        mClassBoxData= Cache.with(this.getActivity())
                .path(getCacheDir(this.getActivity()))
                .getCache("classBox", String.class);

//        Log.v("mySubjects1",mClassBoxData);

//        Cache.with(myContext.getActivity())
//                .path(getCacheDir(myContext.getActivity()))
//                .saveCache("classBox", mClassBoxData);

        if (mClassBoxData==null||mClassBoxData.length()<=0){
            //TODO  mClassBoxData=接收的json字符串
            // 请求网络方法，获取数据
            System.out.println(userId);
            RequestBody requestBody = new FormBody.Builder()
                    .add("userId", userId)
                    .build();
            Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/getallcourse", requestBody,new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    //TODO  mClassBoxData=接收的json字符串

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    // 得到服务器返回的具体内容
                    String responseData = response.body().string();
                    System.out.println(responseData);
                    mClassBoxData = responseData;
                    // 转化为具体的对象列表
                    List<String> jsonlist = JSON.parseArray(responseData, String.class);
                    mySubjects.clear();
                    for(String s : jsonlist) {
                        MySubject mySubject = JSON.parseObject(s, MySubject.class);
                        mySubjects.add(mySubject);
                    }
                    //获得数据后存入缓存
                    Cache.with(myContext.getActivity())
                            .path(getCacheDir(myContext.getActivity()))
                            .remove("classBox");

                    Cache.with(myContext.getActivity())
                            .path(getCacheDir(myContext.getActivity()))
                            .saveCache("classBox", mClassBoxData);
                }
            });
        } else {

            Log.v("here", "here");

            mySubjects.clear();
            mySubjects = JSON.parseArray(mClassBoxData, MySubject.class);

            Log.v("mySubjects", mySubjects.toString());
        }

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

    /**
     * 初始化课程控件
     */
    private void initTimetableView() {
        //获取控件
        mWeekView = (WeekView)getActivity().findViewById(R.id.id_weekview);
        mTimetableView = (Activity_TimetableView)getActivity().findViewById(R.id.id_timetableView);

        //设置周次选择属性
        mTimetableView.curWeek("2019-07-01 00:00:00");
        mWeekView.source(mySubjects)
                .curWeek(mTimetableView.curWeek())
                .callback(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int week) {
                        int cur = mTimetableView.curWeek();
                        //更新切换后的日期，从当前周cur->切换的周week
                        mTimetableView.onDateBuildListener()
                                .onUpdateDate(cur, week);
                        mTimetableView.changeWeekOnly(week);
                    }
                })
                .callback(new IWeekView.OnWeekLeftClickedListener() {
                    @Override
                    public void onWeekLeftClicked() {
                        onWeekLeftLayoutClicked();
                    }
                })
                .isShow(false)//设置隐藏，默认显示
                .showView();

        mTimetableView.source(mySubjects)
//                .curWeek("2019-07-10")
                .curTerm("大三下学期")
                .maxSlideItem(12)
                .monthWidthDp(30)

                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, Schedule schedule) {

                        showDialog(schedule);

                    }
                })
                .callback(new ISchedule.OnItemLongClickListener() {
                    @Override
                    public void onLongClick(View v, int day, int start) {
                        Toast.makeText(getActivity(),
                                "长按:周" + day  + ",第" + start + "节",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        titleTextView.setText("第" + curWeek + "周");
                    }
                })
//                //旗标布局点击监听
//                .callback(new ISchedule.OnFlaglayoutClickListener() {
//                    @Override
//                    public void onFlaglayoutClick(int day, int start) {
//                        mTimetableView.hideFlaglayout();
//                        Toast.makeText(getActivity(),
//                                "点击了旗标:周" + (day + 1) + ",第" + start + "节",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                })
                .showView();
        hideNonThisWeek();
    }

    /**
     * 更新一下，防止因程序在后台时间过长（超过一天）而导致的日期或高亮不准确问题。
     */
    @Override
    public void onStart() {
        super.onStart();
        mTimetableView.onDateBuildListener()
                .onHighLight();
    }

    /**
     * 周次选择布局的左侧被点击时回调<br/>
     * 对话框修改当前周次
     */
    protected void onWeekLeftLayoutClicked() {
        final String items[] = new String[25];
        int itemCount = mWeekView.itemCount();
        for (int i = 0; i < itemCount; i++) {
            items[i] = "第" + (i + 1) + "周";
        }
        target = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("设置当前周");
        builder.setSingleChoiceItems(items, mTimetableView.curWeek() - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        target = i;
                    }
                });
        builder.setPositiveButton("设置为当前周", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (target != -1) {
                    mWeekView.curWeek(target + 1).updateView();
                    mTimetableView.changeWeekForce(target + 1);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /*
    * 显示课程详情
    * */
    private TextView textViewforcoursename;
    private TextView textViewforncoursezhou;
    private TextView textViewforcoursetime;
    private TextView textViewforcourseteacher;
    private TextView textViewforcourseroom;

    protected void showDialog(Schedule bean){
        LayoutInflater inflater=LayoutInflater.from(this.getActivity());
        View myview=inflater.inflate(R.layout.dialog_coursedetail,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(this.getActivity());

        textViewforcoursename=myview.findViewById(R.id.coursedetail_name);
        textViewforncoursezhou=myview.findViewById(R.id.coursedetail_zhoutime);
        textViewforcoursetime=myview.findViewById(R.id.coursedetail_daytime);
        textViewforcourseteacher=myview.findViewById(R.id.coursedetail_teacher);
        textViewforcourseroom=myview.findViewById(R.id.coursedetail_room);

        textViewforcoursename.setText(bean.getName());
        textViewforncoursezhou.setText("第"+bean.getWeekList()+"周");
        textViewforcoursetime.setText("周"+bean.getDay()+"   "+"第"+bean.getStart()+"-"+(bean.getStart()+bean.getStep())+"节");
        textViewforcourseroom.setText(bean.getRoom());
        textViewforcourseteacher.setText(bean.getTeacher());

        builder.setView(myview);
        coursedetail_dialog=builder.create();
        coursedetail_dialog.show();

    }

    /**
     * 显示弹出菜单
     */
    @SuppressLint("RestrictedApi")
    public void showPopmenu() {
        PopupMenu popup = new PopupMenu(this.getActivity(), moreButton);
        popup.getMenuInflater().inflate(R.menu.tabletime_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_add_course:
                        if (isAuthentation)
                            addSubject();
                        else
                            Toast.makeText(getContext(), "请先实名认证",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menu_import_classes:
                        if (isAuthentation)
                            importClass();
                        else
                            Toast.makeText(getContext(), "请先实名认证", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menu_shownotthisweek:
                        showNonThisWeek();
                        break;
                    case R.id.menu_hidenotthisweek:
                        hideNonThisWeek();
                        break;
                    case R.id.menu_showweekend:
                        showWeekends();
                        break;
                    case R.id.menu_hideweekend:
                        hideWeekends();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        try { //popupmenu显示icon的关键
            Field mpopup=popup.getClass().getDeclaredField("mPopup");
            mpopup.setAccessible(true);
            MenuPopupHelper mPopup = (MenuPopupHelper) mpopup.get(popup);
            mPopup.setForceShowIcon(true);
        } catch (Exception e) {
        }
        popup.show();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.id_layout:
                //如果周次选择已经显示了，那么将它隐藏，更新课程、日期
                //否则，显示
                if (mWeekView.isShowing()) {
                    mWeekView.isShow(false);
                    int cur = mTimetableView.curWeek();
                    mTimetableView.onDateBuildListener()
                            .onUpdateDate(cur, cur);
                    mTimetableView.changeWeekOnly(cur);
                } else {
                    mWeekView.isShow(true);
                }
                break;
        }
    }

    /**
     * 删除课程
     * 内部使用集合维护课程数据，操作集合的方法来操作它即可
     * 最后更新一下视图（全局更新）
     */
    protected void deleteSubject() {
        int size = mTimetableView.dataSource().size();
        int pos = (int) (Math.random() * size);
        if (size > 0) {
            mTimetableView.dataSource().remove(pos);
            mTimetableView.updateView();
        }
    }

    /**
     * 添加课程
     * 内部使用集合维护课程数据，操作集合的方法来操作它即可
     * 最后更新一下视图（全局更新）
     */
    protected void addSubject() {
        Intent add = new Intent(getActivity(), Activity_AddSearchCourse.class);
        add.putExtra("userId",userId);
        add.putExtra("proUni", proUni);
        startActivity(add);
    }

    /**
     * 隐藏非本周课程
     * 修改了内容的显示，所以必须更新全部（性能不高）
     * 建议：在初始化时设置该属性
     * <p>
     * updateView()被调用后，会重新构建课程，课程会回到当前周
     */
    protected void hideNonThisWeek() {
        mTimetableView.isShowNotCurWeek(false).updateView();
        mWeekView.curWeek(mTimetableView.curWeek()).updateView();
        mTimetableView.changeWeekForce(mTimetableView.curWeek());
    }

    /**
     * 显示非本周课程
     * 修改了内容的显示，所以必须更新全部（性能不高）
     * 建议：在初始化时设置该属性
     */
    protected void showNonThisWeek() {
        mTimetableView.isShowNotCurWeek(true).updateView();
        mWeekView.curWeek(mTimetableView.curWeek()).updateView();
        mTimetableView.changeWeekForce(mTimetableView.curWeek());
    }

    /**
     * 显示WeekView
     */

    protected void showWeekView() {
        mWeekView.isShow(true);
    }

    /**
     * 隐藏WeekView
     */

    protected void hideWeekView() {
        mWeekView.isShow(false);
    }

    //TODO 导入课表的函数
    /**
     * 教务导入课表
     */
    protected  void importClass() {
        Intent add = new Intent(getActivity(), Activity_AutoPullCourseFromWeb.class);
        add.putExtra("userId",userId);
        add.putExtra("proUni", proUni);
        startActivity(add);
    }


    /**
     * 隐藏周末
     */
    private void hideWeekends() {
        mTimetableView.isShowWeekends(false).updateView();
    }

    /**
     * 显示周末
     */
    private void showWeekends() {
        mTimetableView.isShowWeekends(true).updateView();
    }

    /*
    * 广播
    * */

    class UPDATEcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){

            System.out.println(mcontext);

            mClassBoxData = Cache.with(mcontext)
                    .path(getCacheDir(mcontext))
                    .getCache("classBox", String.class);

            List<String> jsonlist = JSON.parseArray(mClassBoxData, String.class);

            mySubjects.clear();

            for(String s : jsonlist) {
                MySubject mySubject = JSON.parseObject(s, MySubject.class);
                mySubjects.add(mySubject);
            }

            initTimetableView();
        }
    }

    class UpdateStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){

            System.out.println("已收到来自个人认证的广播");

            RequestBody requestBody = new FormBody.Builder()
                    .add("userId", userId)
                    .build();

            Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/getuserinfo", requestBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    JSONObject jsonObject = JSON.parseObject(response.body().string());
                    realName = jsonObject.getString("realname");
                    proUni = jsonObject.getString("tablename");
                    isAuthentation = Boolean.parseBoolean(jsonObject.getString("authentationstatus"));
                }
            });
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(updatereceiver);
    }

}
