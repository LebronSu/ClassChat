package com.example.classchat.Activity;

import android.os.Build;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.classchat.R;

/**
 * 这是帮助与反馈界面
 */
public class Activity_HelpAndFeedback extends AppCompatActivity {

    // 注册相关控件
    private ImageView iv_Return;
    private Button upLoad;
    private EditText feedbackText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__help_and_feedback);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //After LOLLIPOP not translucent status bar
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //Then call setStatusBarColor.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.theme));
        }
        init(); // 初始化
        iv_Return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }); // 返回按钮的定义逻辑

        upLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 这里应该说提交一个什么样的东西，后台能接收到前台返回的话
                Toast.makeText(Activity_HelpAndFeedback.this, "您的反馈我们已经收到，将尽快给您回复", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 控件绑定初始化
    private void init() {
        iv_Return = findViewById(R.id.iv_HelpandFeedBack_back);
        upLoad = findViewById(R.id.btn_HelpandFeedBack_done);
    }
}
