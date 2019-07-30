//package com.example.classchat.Activity;
//
//import android.Manifest;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.os.Handler;
//import android.os.Message;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.View;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.classchat.R;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import okhttp3.Call;
//import okhttp3.FormBody;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//public class Activity_Enter extends AppCompatActivity {
//
//    //初始化图形控件
//    private EditText editPerson, editCode;
//    private TextView register;
//    private Button login;
//    private CheckBox isLogin;
//
//    //初始化 登录等待 控件
//    private ProgressDialog loadingForLogin;
//
//    //设置登录成功或失败的常量
//    private static final int LOGIN_FAILED = 0;
//    private static final int LOGIN_SUCCESS = 1;
//
//
//    /*
//    设置handler接收网络线程的信号并处理
//     */
//    private Handler handler = new Handler(){
//        public void handleMessage(Message msg){
//            switch (msg.what){
//                case LOGIN_FAILED:
//                    //密码错误报警
//                    Toast.makeText(EnterActivity.this,"用户名或密码错误",Toast.LENGTH_SHORT).show();
//                    editPerson.setText(null);editCode.setText(null);
//                    if (loadingForLogin != null && loadingForLogin.isShowing()) {
//                        loadingForLogin.dismiss();
//                    }
//                    break;
//                case LOGIN_SUCCESS:
//                    //登录成功
//                    Toast.makeText(EnterActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
//                    if (isLogin.isChecked()) {
//                        saveUserInfo();
//                    }
//                    Intent intent = new Intent(EnterActivity.this,MainActivity.class);
//                    intent.putExtra("correctId", editPerson.getText().toString());
//                    intent.putExtra("VERIFICATION", editCode.getText().toString());
//                    loadingForLogin.dismiss();
//                    startActivity(intent);
//                    finish();
//                    break;
//                default:
//            }
//        }
//    };
//
//    /*
//    重写活动启动方法
//     */
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.login_enter_activity);
//
//        init();
//
//        /*
//        利用权限列表来获取权限
//         */
//        List<String> permissionlist = new ArrayList<>();
//        if(ContextCompat.checkSelfPermission(EnterActivity.this , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            permissionlist.add(Manifest.permission.ACCESS_FINE_LOCATION);
//        }  //是否有位置权限
//        if(ContextCompat.checkSelfPermission(EnterActivity.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            permissionlist.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }  //是否有存储权限
//        if(!permissionlist.isEmpty()){
//            String []permissions = permissionlist.toArray(new String[permissionlist.size()]);
//            ActivityCompat.requestPermissions(EnterActivity.this , permissions , 1);
//        }  //若有没有的权限，进行询问获取
//    }
//
//    /*
//    初始化各控件
//     */
//    private void init() {
//        login = findViewById(R.id.bn_common_login);
//        login.setOnClickListener(this);
//        isLogin = findViewById(R.id.btn_loginactivity_autologin);
//        editCode = findViewById(R.id.et_password);
//        editPerson = findViewById(R.id.et_username);
//        register = findViewById(R.id.tv_register);
//        register.setOnClickListener(this);
//        getUserInfo();
//    }
//
//    /*
//    点击响应事件
//     */
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.bn_common_login:  //登录按钮
//                login(v);
//                break;
//            case R.id.tv_register:  //注册按钮
//                Intent intent = new Intent(this, RegisterActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.btn_loginactivity_autologin:
//                isLogin.setChecked(isLogin.isChecked());
//        }
//    }
//
//    /*
//    登录方法
//     */
//    public void login(View view) {
//
//        String currentUsername = editPerson.getText().toString(); //去除空格，获取手机号
//        String currentPassword = editCode.getText().toString();  //去除空格，获取密码
//
//        if (TextUtils.isEmpty(currentUsername)) { //判断手机号是不是为空
//            Toast.makeText(this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (TextUtils.isEmpty(currentPassword)) {  //判断密码是不是空
//            Toast.makeText(this, R.string.Confirm_password_cannot_be_empty, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        /*
//        等待界面，因为登录操作是耗时操作
//         */
//        loadingForLogin = new ProgressDialog(EnterActivity.this);  //初始化等待动画
//        loadingForLogin.setCanceledOnTouchOutside(false); //
//        loadingForLogin.setMessage("正在登录....");  //等待动画的标题
//        loadingForLogin.show();  //显示等待动画
//
//        /*
//        开启网络线程，发送登录请求
//         */
//        RequestBody requestBody = new FormBody.Builder()
//                .add("username", currentUsername)
//                .add("password", currentPassword)
//                .build();   //构建请求体
//
//        NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/login", requestBody, new okhttp3.Callback() {
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                // 得到服务器返回的具体内容
//
//                boolean responseData = Boolean.parseBoolean(response.body().string());
//
//                System.out.println(responseData);
//
//                Message message = new Message();    // 准备发送信息通知UI线程
//
//                if(responseData) {
//                    message.what = LOGIN_SUCCESS;
//                    handler.sendMessage(message);   // 登录成功
//                } else {
//                    message.what = LOGIN_FAILED;
//                    handler.sendMessage(message);
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                // 在这里对异常情况进行处理
//            }
//        });
//
//    }
//
//    /*
//    若登录成功，将用户的账号和密码保存下来
//     */
//    private void saveUserInfo(){
//        SharedPreferences sp = getSharedPreferences("userinfo" , Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putString("name",editPerson.getText().toString()).commit();
//        editor.putString("psw" , editCode.getText().toString()).commit();
//    }
//
//    /*
//    登录时获取存储的用户的账号和密码
//     */
//    private void getUserInfo(){
//        SharedPreferences sp = getSharedPreferences("userinfo" ,Context.MODE_PRIVATE );
//        editPerson.setText(sp.getString("name",""));
//        editCode.setText(sp.getString("psw",""));
//    }
//}
