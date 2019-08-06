package com.example.classchat.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.SecureDirectoryStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity_Password extends AppCompatActivity {

    private static final String TAG = "PasswordActivity";

    // 初始化添加商品的等待控件
    private ProgressDialog loadingUpload;

    // 控件成员
    private ImageView ivReturn;
    private TextView btnUpload;
    private TextView tvId;
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etVerifyPassword;
    private String userId;

    // 标识常量
    private static final int SAVE_SUCCESS = 11;
    private static final int SAVE_FAILED = 12;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case SAVE_SUCCESS:
                    Toast.makeText(Activity_Password.this,"保存成功！",Toast.LENGTH_SHORT).show();
                    loadingUpload.dismiss();
                    finish();
                    break;
                case SAVE_FAILED:
                    Toast.makeText(Activity_Password.this,"密码错误，请重新输入",Toast.LENGTH_SHORT).show();
                    etOldPassword.setText("");
                    loadingUpload.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__password);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //After LOLLIPOP not translucent status bar
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //Then call setStatusBarColor.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.theme));
        }

//        // 去除ActionBar
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();

        // TODO 获得学生ID
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        // 初始化控件函数
        init();

        tvId.setText(userId);

        /*
        按钮的响应事件
         */
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etOldPassword.getText().toString().equals("") || etNewPassword.getText().toString().equals("") || etVerifyPassword.getText().toString().equals("")) {
                    Toast.makeText(Activity_Password.this, "您的信息不足，请继续填写", Toast.LENGTH_SHORT).show();
                } else if (!etNewPassword.getText().toString().equals(etVerifyPassword.getText().toString())) {
                    Toast.makeText(Activity_Password.this, "确认密码须与新密码相同，请重新填写", Toast.LENGTH_SHORT).show();
                    etVerifyPassword.setText("");
                } else {
                    /*
                    等待界面，因为登录操作是耗时操作
                    */
                    loadingUpload = new ProgressDialog(Activity_Password.this);  //初始化等待动画
                    loadingUpload.setCanceledOnTouchOutside(false); //
                    loadingUpload.setMessage("正在上传....");  //等待动画的标题
                    loadingUpload.show();  //显示等待动画
                    // 发起网络请求修改密码
                    RequestBody requestBody = new FormBody.Builder()
                            .add("userId", userId)
                            .add("oldpsw", etOldPassword.getText().toString())
                            .add("newpsw", etNewPassword.getText().toString())
                            .build();

                    System.out.println(etOldPassword.getText().toString());

                    Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/changepsw", requestBody, new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                            // 得到服务器返回的具体内容
                            boolean responseData = Boolean.parseBoolean(response.body().string());

                            Message message = new Message();    // 准备发送信息通知UI线程

                            if(responseData) {
                                message.what = SAVE_SUCCESS;
                                handler.sendMessage(message);   // 登录成功
                            } else {
                                message.what = SAVE_FAILED;
                                handler.sendMessage(message);
                            }
                        }
                    });
                }
            }
        });

        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void init() {
        ivReturn = findViewById(R.id.iv_modifypassword_return);
        btnUpload = findViewById(R.id.btn_modifypassword_upload);
        tvId = findViewById(R.id.tv_modifypassword_id);
        etOldPassword = findViewById(R.id.et_modifypassword_oldpassword);
        etNewPassword = findViewById(R.id.et_modifypassword_newpassword);
        etVerifyPassword = findViewById(R.id.et_modifypasswordverifypassword);
    }
}
