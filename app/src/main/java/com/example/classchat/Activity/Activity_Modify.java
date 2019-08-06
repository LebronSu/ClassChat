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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity_Modify extends AppCompatActivity {
    // 控件成员
    private ImageView ivReturn;
    private TextView btnUpload;
    private EditText etName;

    // 标识常量
    private static final int SAVE_SUCCESS = 11;
    private static final int SAVE_FAILED = 12;

    private String userId;
    private String userName;

    // 初始化等待控件
    private ProgressDialog loadingUpload;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case SAVE_SUCCESS:
                    Toast.makeText(Activity_Modify.this,"保存成功！",Toast.LENGTH_SHORT).show();
                    loadingUpload.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra("name_return", etName.getText().toString());
                    System.out.println(etName.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                case SAVE_FAILED:
                    Toast.makeText(Activity_Modify.this,"保存失败",Toast.LENGTH_SHORT).show();
                    loadingUpload.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__modify);

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

        // 为控件绑定成员
        ivReturn = findViewById(R.id.iv_modifyname_return);
        btnUpload = findViewById(R.id.btn_modifyname_upload);
        etName = findViewById(R.id.et_modifyname_name);

        // TODO 获得学生ID
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");

        etName.setText(userName);


        /*
        按钮敲击响应事件
         */
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etName.getText().toString().equals("")) {
                    Toast.makeText(Activity_Modify.this, "请先输入您的昵称", Toast.LENGTH_SHORT).show();
                } else {
                    /*
                    等待界面，因为登录操作是耗时操作
                    */
                    loadingUpload = new ProgressDialog(Activity_Modify.this);  //初始化等待动画
                    loadingUpload.setCanceledOnTouchOutside(false); //
                    loadingUpload.setMessage("正在上传....");  //等待动画的标题
                    loadingUpload.show();  //显示等待动画
                    // 发起网络请求修改昵称
                    RequestBody requestBody = new FormBody.Builder()
                            .add("userId", userId)
                            .add("newname", etName.getText().toString())
                            .build();

                    Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/changenickname", requestBody, new Callback() {
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
                                handler.sendMessage(message);
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
}
