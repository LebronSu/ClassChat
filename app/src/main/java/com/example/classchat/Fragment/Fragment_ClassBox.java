package com.example.classchat.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.classchat.Activity.Activity_AddSearchCourse;
import com.example.classchat.Activity.Activity_AutoPullCourseFromWeb;
import com.example.classchat.Activity.Activity_CourseNote;
import com.example.classchat.Activity.Activity_Enter;
import com.example.classchat.Activity.Activity_SearchAddCourse;
import com.example.classchat.Activity.MainActivity;
import com.example.classchat.Object.MySubject;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.example.classchat.Util.Util_PictureTool;
import com.example.library_activity_timetable.Activity_TimetableView;
import com.example.library_activity_timetable.listener.ISchedule;
import com.example.library_activity_timetable.listener.IWeekView;
import com.example.library_activity_timetable.model.Schedule;
import com.example.library_activity_timetable.view.WeekView;
import com.example.library_cache.Cache;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.jetbrains.annotations.NotNull;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;
import io.rong.message.LocationMessage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

import static android.app.Activity.RESULT_OK;
import static io.rong.imkit.RongIM.connect;


public class Fragment_ClassBox extends Fragment implements OnClickListener {

    private AlertDialog.Builder alertBuilder;

    //初始化照片URI
    private Uri imageUri;

    //初始化 登录等待 控件
    private ProgressDialog loadingForLogin;

    private static final String TAG = "Activity_Main_Timetable";

    public JSONObject groupChatManager = new JSONObject();

    //百度地图客户端
    private LocationClient client;
    private double now_longitude;
    private double now_latitude;

    public JSONObject signstatus = new JSONObject();
    public JSONObject getSignstatus() {
        return signstatus;
    }


    //控件
    Activity_TimetableView mTimetableView;
    WeekView mWeekView;
    ImageButton moreButton;
    ImageButton scanButton;
    LinearLayout layout;
    TextView titleTextView;
    List<MySubject> mySubjects = new ArrayList<MySubject>();

    //学生聊天时所需的token
    String token = "";

    //学生ID
    private String userId;

    // 学生专业
    private String proUni;

    public String getProUni() {
        return proUni;
    }

    // 头像Url
    private String imageUrl;

    // 真人头像
    private String headUrl;

    private Bitmap bitmap;

    // 学生真实姓名
    private String realName;

    // 学生是否实名认证
    private Boolean isAuthentation;

    // 对话框
    Dialog coursedetail_dialog;

    //记录切换的周次，不一定是当前周
    int target = -1;

    // 搞一个自己的变量
    Fragment_ClassBox myContext = this;

    // 缓存
    private String mClassBoxData = "";

    private Boolean diegod = true;

    private UPDATEcastReceiver updatereceiver;
    private UpdateStateReceiver updateStateReceiver;
    private UpdateGroupIdReceiver updateGroupIdReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private Context mcontext;


    private ImageView qrcode;
    /*
    设置handler接收网络线程的信号并处理
     */
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    initTimetableView();
                    break;
                case 2:
                    Toast.makeText(getContext() , "位置校验无误，请进行人脸识别" , Toast.LENGTH_SHORT).show();
                    //调用相机进行人脸识别
                    takephoto();
                    break;
                case 3:
                    Toast.makeText(getContext() , "位置有误，签到失败！" , Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    loadingForLogin.dismiss();
                    Toast.makeText(getContext(),"签到成功！" ,Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    RequestBody requestBody = new FormBody.Builder()
                            .add("groupId", signstatus.getString("groupId"))
                            .add("userId" , userId)
                            .add("tablename", proUni)
                            .build();
                    Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/course/updatesignstatus", requestBody, new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {


                            Message message = new Message();
                            message.what = 5;
                            handler.sendMessage(message);

                        }
                    });
                    break;
                case 6:
                    Toast.makeText(getContext(),"签到失败，请调整照相姿势，重新签到！" ,Toast.LENGTH_SHORT).show();
                    loadingForLogin.dismiss();
                    break;
                case 7:
                    client = new LocationClient(getContext());
                    client.registerLocationListener(new MyLocationListener());
                    initclient();
                    client.start();
                    loadingForLogin = new ProgressDialog(getContext());  //初始化等待动画
                    loadingForLogin.setCanceledOnTouchOutside(false); //
                    loadingForLogin.setMessage("正在获取位置....");  //等待动画的标题
                    loadingForLogin.show();  //显示等待动画
                    break;
                case 8:
                    Toast.makeText(getContext(),"签到失败" ,Toast.LENGTH_SHORT).show();
                    break;
                case 9:
                    //这里需要向服务器发出这些课程，服务器再返回合格的mysubjects
                    RequestBody requestBody1 = new FormBody.Builder()
                            .add("userId", userId)
                            .add("subjects", JSON.toJSONString(mySubjects))
                            .add("tablename" , proUni)
                            .build();
                    Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/autoupdatecourse", requestBody1, new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        }
                    });
                default:
                    break;
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.activity__main__timetable, container, false);

    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //将判断签到状态设置好
        mcontext = this.getActivity();
        MainActivity mainActivity = (MainActivity)getActivity();
        headUrl = mainActivity.getHeadUrl();
        userId = mainActivity.getId();
        isAuthentation = mainActivity.getAuthentation();
        realName = mainActivity.getRealName();
        proUni = mainActivity.getProUni();
        token = mainActivity.getToken();
        imageUrl = mainActivity.getImageUrl();
        scanButton = getActivity().findViewById(R.id.id_scan);
        moreButton =(ImageButton)getActivity().findViewById(R.id.id_more);
        moreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopmenu();
            }
        });
        scanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 这里面写扫一扫的逻辑
                if (!isAuthentation) {
                    Toast.makeText(getContext(), "请先实名认证！", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getContext(), CaptureActivity.class);
                    /*ZxingConfig是配置类
                     *可以设置是否显示底部布局，闪光灯，相册，
                     * 是否播放提示音  震动
                     * 设置扫描框颜色等
                     * 也可以不传这个参数
                     * */
                    ZxingConfig config = new ZxingConfig();
                    config.setPlayBeep(true);//是否播放扫描声音 默认为true
                    config.setShake(true);//是否震动  默认为true
                    config.setDecodeBarCode(true);//是否扫描条形码 默认为true
                    config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
                    config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
                    config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
                    config.setFullScreenScan(true);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
                    intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                    startActivityForResult(intent, 3);
                }
            }
        });



        titleTextView = (TextView)getActivity().findViewById(R.id.id_title);
        layout = (LinearLayout)getActivity().findViewById(R.id.id_layout);
        layout.setOnClickListener(this);

        //广播接收
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

        // 加载页面的广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcasttest.LOCAL_BROADCAST1");
        updatereceiver = new UPDATEcastReceiver();
        localBroadcastManager.registerReceiver(updatereceiver, intentFilter);

        // 实名认证的广播
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction("com.example.broadcasttest.UPDATE_STATE");
        updateStateReceiver = new UpdateStateReceiver();
        localBroadcastManager.registerReceiver(updateStateReceiver, intentFilter1);

        // 获得GroupId的广播
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.example.broadcasttest.LOCAL_BROADCAST2");
        updateGroupIdReceiver = new UpdateGroupIdReceiver();
        localBroadcastManager.registerReceiver(updateGroupIdReceiver, intentFilter2);

        initClassBoxData();

        initTimetableView();
    }

    /**
     * 模拟数据读取与存储
     */
    private void initClassBoxData(){


//        Cache.with(myContext.getActivity())
//                .path(getCacheDir(myContext.getActivity()))
//                .remove("classBox");

        mClassBoxData = Cache.with(this.getActivity())
                .path(getCacheDir(this.getActivity()))
                .getCache("classBox", String.class);


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

                    // 获取课程id 和未读消息数的 Key Value 关系
                    groupChatManager = getGroupChatManager(mySubjects);

                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);

                    // 发送登录聊天的广播
                    Intent intent2 = new Intent("com.example.broadcasttest.LOCAL_BROADCAST2");
                    localBroadcastManager.sendBroadcast(intent2);
                }
            });
        } else {

            mySubjects.clear();
            mySubjects = JSON.parseArray(mClassBoxData, MySubject.class);

            //获取 课程id 和未读消息数的 Key Value 关系
            groupChatManager = getGroupChatManager(mySubjects);

            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);

            // 发送登录聊天的广播
            Intent intent2 = new Intent("com.example.broadcasttest.LOCAL_BROADCAST2");
            localBroadcastManager.sendBroadcast(intent2);
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
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        titleTextView.setText("第" + curWeek + "周");
                    }
                })
                .callback(new ISchedule.OnItemLongClickListener() {
                    @Override
                    public void onLongClick(View v, int day, int start) {
                        Log.d(TAG, "onLongClick: + " + mClassBoxData);
                        Bitmap bitmap = CodeCreator.createQRCode("http://106.12.105.160:8081/getallcourse?userId=" + userId, 700, 700, null);
                        LayoutInflater inflater=LayoutInflater.from(getContext());
                        View xxview=inflater.inflate(R.layout.fragment_qrcodeshowdialog,null);
                        final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                        qrcode = xxview.findViewById(R.id.iv_qrcode);
                        qrcode.setImageBitmap(bitmap);
                        builder.setView(xxview);
                        builder.create().show();
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
//    @Override
//    public void onStart() {
//        super.onStart();
//        initTimetableView();
//        mTimetableView.onDateBuildListener()
//                .onHighLight();
//    }

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
    private ImageView imageViewRemoveClass;
    private ImageView imageViewCloseDialog;
    private LinearLayout linearLayoutChat;
    private LinearLayout linearLayoutCollect;
    private LinearLayout linearLayoutSign;
    private LinearLayout linearLayoutNote;

    private Badge badge = null;

    protected void showDialog(final Schedule bean){
        LayoutInflater inflater=LayoutInflater.from(this.getActivity());
        View myview=inflater.inflate(R.layout.dialog_coursedetail,null);
        final AlertDialog.Builder builder=new AlertDialog.Builder(this.getActivity());

        textViewforcoursename=myview.findViewById(R.id.coursedetail_name);
        textViewforncoursezhou=myview.findViewById(R.id.coursedetail_zhoutime);
        textViewforcoursetime=myview.findViewById(R.id.coursedetail_daytime);
        textViewforcourseteacher=myview.findViewById(R.id.coursedetail_teacher);
        textViewforcourseroom=myview.findViewById(R.id.coursedetail_room);
        imageViewRemoveClass=myview.findViewById(R.id.delete_class);
        imageViewCloseDialog=myview.findViewById(R.id.close_dialog);
        linearLayoutChat = myview.findViewById(R.id.course_chat);
        linearLayoutCollect = myview.findViewById(R.id.course_file);
        linearLayoutSign = myview.findViewById(R.id.course_sign);
        linearLayoutNote = myview.findViewById(R.id.course_note);

        textViewforcoursename.setText(bean.getName());
        textViewforncoursezhou.setText("第"+bean.getWeekList().get(0) + " ~ "+bean.getWeekList().get(bean.getWeekList().size() - 1) +"周");
        textViewforcoursetime.setText("周"+bean.getDay()+"   "+"第"+bean.getStart()+"-"+(bean.getStart()+bean.getStep() -1 )+"节");
        textViewforcourseroom.setText(bean.getRoom());
        textViewforcourseteacher.setText(bean.getTeacher());

        builder.setView(myview);
        coursedetail_dialog=builder.create();
        coursedetail_dialog.show();

        if (bean.getMessagecount() != 0) {
            badge = new QBadgeView(getActivity()).bindTarget(linearLayoutChat).setBadgeNumber(bean.getMessagecount());
        }

        imageViewCloseDialog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                coursedetail_dialog.dismiss();
            }
        });

        imageViewRemoveClass.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alertBuilder = new AlertDialog.Builder(getContext());
                alertBuilder.setTitle("提示");
                alertBuilder.setMessage("确认删除？");
                alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeSubject(new MySubject(bean.getName(), bean.getRoom(), bean.getTeacher(), bean.getWeekList(), bean.getStart(), bean.getStep(), bean.getDay(), bean.getId(), bean.getMessagecount()));
                        coursedetail_dialog.dismiss();
                    }
                });
                alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertBuilder.show();
            }
        });

        linearLayoutChat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                groupChatManager.put(bean.getId(), 0);
                RongIM.getInstance().startGroupChat(getContext(), bean.getId(), bean.getName());
                updateUI(bean.getId());
                if(!(null == badge ))
                    badge.hide(false);
            }
        });

        linearLayoutCollect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 点击后展示这门课的内容
                String path = Environment.getExternalStorageDirectory() + "/RongCloud/Media";
                File file= new File(path);
                Uri fileURI = FileProvider.getUriForFile(getActivity(), "com.example.classchat.FileProvider", file);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setDataAndType(fileURI, "file/*");
                Uri originalUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ARongCloud%2FMedia");
                DocumentFile docFile = DocumentFile.fromTreeUri(getActivity(), originalUri);
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, docFile.getUri());
                startActivity(intent);
            }
        });

        linearLayoutSign.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestBody requestBody = new FormBody.Builder()
                        .add("time", String.valueOf(System.currentTimeMillis()))
                        .add("groupId", bean.getId())
                        .add("userId", userId)
                        .add("tablename", proUni)
                        .build();

                Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/course/issignable", requestBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (Boolean.valueOf(response.body().string())) {
                            android.os.Message message1 = new android.os.Message();
                            message1.what = 7;
                            handler.sendMessage(message1);
                        }else {
                            android.os.Message message1 = new android.os.Message();
                            message1.what = 8;
                            handler.sendMessage(message1);
                        }
                    }
                });
            }
        });

        linearLayoutNote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Activity_CourseNote.class);
                startActivity(intent);
            }
        });

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
                        if (isAuthentation){
                            if (diegod) {
                                importClass();
                            }
                            else {
                                Toast.makeText(getContext(), "实名认证状态还未更新，请稍等片刻再点击", Toast.LENGTH_SHORT).show();
                            }
                        }
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

    class UpdateGroupIdReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            if (isAuthentation)
                connect(token, new RongIMClient.ConnectCallback() {
                    @Override
                    public void onTokenIncorrect() {

                    }

                    @Override
                    public void onSuccess(String s) {

                        Log.d("LoginActivity", "--onSuccess");

                        // 登陆成功
//                        Toast.makeText(context, "可以使用聊天", Toast.LENGTH_SHORT).show();

                        RongIM.getInstance().setCurrentUserInfo(new UserInfo(userId, realName, Uri.parse(imageUrl)));
                        RongIM.getInstance().setMessageAttachedUserInfo(true);
                        RongIM.getInstance().enableNewComingMessageIcon(true);//显示新消息提醒
                        RongIM.getInstance().enableUnreadMessageIcon(true);//显示未读消息数目

                        for (final String groupId :groupChatManager.keySet()) {
                            RongIM.getInstance().getUnreadCount(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer integer) {
                                    groupChatManager.put(groupId, integer);
                                    for(int i = 0 ; i < mySubjects.size() ; i++){
                                        if(mySubjects.get(i).getId().equals(groupId))
                                            mySubjects.get(i).setMessageCount(integer);
                                    }

                                    Message message = new Message();
                                    message.what = 1;
                                    handler.sendMessage(message);
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    Log.d(TAG, String.valueOf(errorCode.getValue()));
                                }
                            });
                        }

                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {

                    }
                });
        }
    }

    class UpdateStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){

            diegod = false;

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
                    proUni = jsonObject.getString("university") + "_" + jsonObject.getString("school");
                    isAuthentation = Boolean.parseBoolean(jsonObject.getString("authentationstatus"));
                    token = jsonObject.getString("token");
                    headUrl = jsonObject.getString("head");
                    diegod = true;
                    connect(token, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {

                        }

                        @Override
                        public void onSuccess(String s) {
                            // 去执行下面的函数\
                            Log.d("LoginActivity", "--onSuccess");

                            // 登陆成功
//                            Toast.makeText(getActivity(), "可以使用聊天", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                }
            });
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(updatereceiver);
        localBroadcastManager.unregisterReceiver(updateStateReceiver);
        localBroadcastManager.unregisterReceiver(updateGroupIdReceiver);
        RongIM.getInstance().logout();
    }

    public void removeSubject (MySubject subject) {
        mClassBoxData = Cache.with(myContext.getActivity())
                .path(getCacheDir(myContext.getActivity()))
                .getCache("classBox", String.class);

        List<MySubject> mySubjects = JSON.parseArray(mClassBoxData, MySubject.class);

        int count = 0;

        for (MySubject item : mySubjects) {
            if (item.getId().equals(subject.getId())) {
                count++;
            }
        }

        if (count == 1) {

            //调用服务器
            RequestBody requestBody = new FormBody.Builder()
                    .add("userId", userId)
                    .add("coursename", subject.getName())
                    .build();

            Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/removecourse", requestBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                }
            });
        }

        for (int i = 0 ; i < mySubjects.size() ; i++) {
            if ((mySubjects.get(i).getId().equals(subject.getId())) && (mySubjects.get(i).getStart() == subject.getStart()) && (mySubjects.get(i).getDay() == subject.getDay()) ) {
                mySubjects.remove(i);
            }
        }

        //获得数据后存入缓存
        Cache.with(myContext.getActivity())
                .path(getCacheDir(myContext.getActivity()))
                .remove("classBox");

        Cache.with(myContext.getActivity())
                .path(getCacheDir(myContext.getActivity()))
                .saveCache("classBox", JSON.toJSONString(mySubjects));

        Intent intent1 = new Intent("com.example.broadcasttest.LOCAL_BROADCAST1");
        localBroadcastManager.sendBroadcast(intent1);
    }

    public JSONObject getGroupChatManager(List<MySubject> subjectList){
        JSONObject object = new JSONObject();
        for(MySubject subject : subjectList) {
            object.put(subject.getId(), 0);
        }
        return object;
    }

    public void updateUI(String groupId) {
        for(int i = 0 ; i < mySubjects.size() ; i++){
            if(mySubjects.get(i).getId().equals(groupId))
                mySubjects.get(i).setMessageCount(groupChatManager.getInteger(groupId));
        }

        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }

    //百度地图客户端初始化
    private void initclient(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        client.setLocOption(option);
    }

    //百度地图得到结果回调
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            now_latitude = Double.valueOf(location.getLatitude());
            now_longitude = Double.valueOf(location.getLongitude());
            Log.d(TAG, "onReceiveLocation: "+ now_latitude);
            Log.d(TAG, "onReceiveLocation: "+ now_longitude);
            // 不要转圈
            loadingForLogin.dismiss();
            // 判断符不符合
            if((Math.abs((now_latitude - signstatus.getDoubleValue("la")))) < 0.1&&(Math.abs((now_longitude - signstatus.getDoubleValue("lo")))) < 0.1){
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            }else{
                Message message = new Message();
                message.what = 3;
                handler.sendMessage(message);
            }

        }
    }

    public void takephoto(){
        //创建File对象，用于存储拍照后的照片
        File outputImage = new File(getCacheDir(getContext()),
                "output_image.jpg");
        try {
            if (outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24){
            imageUri = FileProvider.getUriForFile(getContext(),
                    "com.example.classchat.FileProvider",outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,2);
    }

    //照相得到结果回调
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case 2:
                if (resultCode == RESULT_OK){
                    try {
                        //将拍摄的照片显示出来
                        bitmap = compressBitmapInQuality(imageUri);
                        sign();
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            case 3:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        String content = data.getStringExtra(Constant.CODED_CONTENT);

                        Util_NetUtil.sendOKHTTPRequest(content, new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {

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

                                // 获取课程id 和未读消息数的 Key Value 关系
                                groupChatManager = getGroupChatManager(mySubjects);

                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);

                                Message message1 = new Message();
                                message1.what = 9;
                                handler.sendMessage(message1);

                                // 发送登录聊天的广播
                                Intent intent2 = new Intent("com.example.broadcasttest.LOCAL_BROADCAST2");
                                localBroadcastManager.sendBroadcast(intent2);
                            }
                        });
                    }
                }
                break;
            default:
                break;
        }
    }

    public String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }



    private void sign() {
        loadingForLogin.setMessage("正在进行人脸识别");
        loadingForLogin.show();
        Log.d(TAG, "sign: "+ headUrl);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api_key","MyoixvqhJTuO4SsirijhOQH6qeHX2Z8N")
                .addFormDataPart("api_secret", "T6qzPC3JpUWeZhG1PdEaPpSToIMp59qD")
//                .addFormDataPart("image_base64_1", bitmapToBase64(bitmap))
//                .addFormDataPart("image_base64_2", bitmapToBase64(bitmap))
                .addFormDataPart("image_url1", headUrl)
//                .addFormDataPart("image_url2", "http://farm.rxsy.net/wp-content/uploads/2019/08/Vladimir-Serov-the-faces-2.jpg")
//                .addFormDataPart("image_file1", "image_file1", RequestBody.create(MediaType.parse("image/jpeg"), Util_PictureTool.compressImage(bitmap, "image_file1")))
                .addFormDataPart("image_file2", "image_file2", RequestBody.create(MediaType.parse("image/jpeg"), BitmapToFile(bitmap, "image_file2")))
                .build();   //构建请求体

        Util_NetUtil.sendOKHTTPRequest("https://api-cn.faceplusplus.com/facepp/v3/compare", requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                JSONObject object = JSON.parseObject(res);
                double confidence = object.getDoubleValue("confidence");
                Log.d(TAG, "confidence:"+ confidence);
                Log.d(TAG, "onResponse: " + object.getString("image_id1"));
                Log.d(TAG, "onResponse: " + object.getString("image_id2"));
                Log.d(TAG, "onResponse: " + object.getJSONArray("faces1"));
                Log.d(TAG, "onResponse: " + object.getJSONArray("faces2"));
                Log.d(TAG, "onResponse: " + object.getString("error_message"));
                if(confidence >= 90){
                    Message message = new Message();
                    message.what = 4;
                    handler.sendMessage(message);
                }
                else {
                    Message message = new Message();
                    message.what = 6;
                    handler.sendMessage(message);
                }
            }
        });
    }

    /**
     * 单纯的进行质量压缩，不进行尺寸压缩
     * @param uri 拍照返回的Uri
     * @return Bitmap 返回bitmap
     */
    public Bitmap compressBitmapInQuality(Uri uri) throws IOException {
        InputStream input = getActivity().getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input); // 这里直接把图片从流里取出，不过内存会一下子变大
        input.close();
        return bitmap;
    }

    public static File BitmapToFile(Bitmap bitmap, String name) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        File file = new File(Environment.getExternalStorageDirectory(), name + ".jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        return file;
    }



}
