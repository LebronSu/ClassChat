package com.example.classchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.nightonke.boommenu.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity_Enter extends AppCompatActivity implements View.OnClickListener{

    //初始化图形控件
    private EditText editPerson, editCode;
    private TextView register;
    private Button login;
    private CheckBox isLogin;

    //初始化 登录等待 控件
    private ProgressDialog loadingForLogin;

    //设置登录成功或失败的常量
    private static final int LOGIN_FAILED = 0;
    private static final int LOGIN_SUCCESS = 1;

    // 登录时就返回必须的数据，这里先定义好
    private boolean isAuthentation;
    private String imageUrl;
    private String headUrl;
    private String nickName;
    private String proUni;
    private String realName;
    private String token;

    // 声明一个数组permissions，将需要的权限都放在里面
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
    // 创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<>();

    final int mFirstRequestCode = 100;//权限请求码

    /*
    设置handler接收网络线程的信号并处理
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case LOGIN_FAILED:
                    //密码错误报警
                    Toast.makeText(Activity_Enter.this,"用户名或密码错误",Toast.LENGTH_SHORT).show();
                    editPerson.setText(null);editCode.setText(null);
                    if (loadingForLogin != null && loadingForLogin.isShowing()) {
                        loadingForLogin.dismiss();
                    }
                    break;
                case LOGIN_SUCCESS:
                    //登录成功
                    Toast.makeText(Activity_Enter.this,"登录成功",Toast.LENGTH_SHORT).show();
                    if (isLogin.isChecked()) {
                        saveUserInfo();
                    }
                    Intent intent = new Intent(Activity_Enter.this,MainActivity.class);
                    intent.putExtra("userName", nickName);
                    intent.putExtra("userPassword", editCode.getText().toString());
                    intent.putExtra("userId", editPerson.getText().toString());
                    intent.putExtra("userImage", imageUrl);
                    intent.putExtra("userAuthentationStatus", isAuthentation);
                    intent.putExtra("proUni", proUni);
                    intent.putExtra("token", token);
                    intent.putExtra("realName", realName);
                    intent.putExtra("headUrl", headUrl);
                    loadingForLogin.dismiss();
                    startActivity(intent);
                    finish();
                    break;
                default:
            }
        }
    };

    /*
    重写活动启动方法
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__enter);
        initPermission(); // 初始化权限请求
        init(); // 初始化各控件
        getUserInfo(); // 取出储存好的用户信息

    }

    /*
    初始化权限函数
     */
    private void initPermission() {
        mPermissionList.clear();//清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mFirstRequestCode);
        }else{
            // 感谢您的配合现在开始使用我们畅快淋漓的课程表体验吧！！！
        }
    }

    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mFirstRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
//                showPermissionDialog();//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
            }else{
                // 初始权限全部通过啦，感谢您的配合
            }
        }

    }

    /*
    初始化各控件
     */
    private void init() {
        login = findViewById(R.id.bn_common_login);
        login.setOnClickListener(this);
        isLogin = findViewById(R.id.btn_loginactivity_autologin);
        editCode = findViewById(R.id.et_password);
        editPerson = findViewById(R.id.et_username);
        register = findViewById(R.id.tv_register);
        register.setOnClickListener(this);
    }

    /*
    点击响应事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_common_login:  //登录按钮
                login(v);
                break;
            case R.id.tv_register:  //注册按钮
                Intent intent = new Intent(this, Activity_Register.class);
                startActivity(intent);
                break;
            case R.id.btn_loginactivity_autologin:
                isLogin.setChecked(isLogin.isChecked());
        }
    }

    /*
    登录方法
     */
    public void login(View view) {

        String currentUsername = editPerson.getText().toString(); //去除空格，获取手机号
        String currentPassword = editCode.getText().toString();  //去除空格，获取密码

        if (TextUtils.isEmpty(currentUsername)) { //判断手机号是不是为空
            Toast.makeText(this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(currentPassword)) {  //判断密码是不是空
            Toast.makeText(this, R.string.Confirm_password_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        /*
        等待界面，因为登录操作是耗时操作
         */
        loadingForLogin = new ProgressDialog(Activity_Enter.this);  //初始化等待动画
        loadingForLogin.setCanceledOnTouchOutside(false); //
        loadingForLogin.setMessage("正在登录....");  //等待动画的标题
        loadingForLogin.show();  //显示等待动画

        /*
        开启网络线程，发送登录请求
         */
        final RequestBody requestBody = new FormBody.Builder()
                .add("username", currentUsername)
                .add("password", currentPassword)
                .build();   //构建请求体

        Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/login", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 得到服务器返回的具体内容
                Message message = new Message();
                String responsedata = response.body().string();
                if (responsedata.equals("ERROR"))
                {
                    message.what = LOGIN_FAILED;
                    handler.sendMessage(message);
                } else {
                    JSONObject jsonObject = JSON.parseObject(responsedata);
                    nickName = jsonObject.getString("nickname");
                    imageUrl = jsonObject.getString("ico");
                    isAuthentation = Boolean.parseBoolean(jsonObject.getString("authentationstatus"));
                    realName = jsonObject.getString("realname");
                    proUni = jsonObject.getString("university") + "_" + jsonObject.getString("school");
                    token = jsonObject.getString("token");
                    headUrl = jsonObject.getString("headUrl");
                    message.what = LOGIN_SUCCESS;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 在这里对异常情况进行处理
            }
        });

    }

    /*
    若登录成功，将用户的账号和密码保存下来
     */
    private void saveUserInfo(){
        SharedPreferences sp = getSharedPreferences("userinfo" , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("name",editPerson.getText().toString()).commit();
        editor.putString("psw" , editCode.getText().toString()).commit();
    }

    /*
    登录时获取存储的用户的账号和密码
     */
    private void getUserInfo(){
        SharedPreferences sp = getSharedPreferences("userinfo" ,Context.MODE_PRIVATE );
        editPerson.setText(sp.getString("name",""));
        editCode.setText(sp.getString("psw",""));
    }

    @Override
    public Resources getResources() {//禁止app字体大小跟随系统字体大小调节
        Resources resources = super.getResources();
        if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
            android.content.res.Configuration configuration = resources.getConfiguration();
            configuration.fontScale = 1.0f;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        return resources;
    }
}
