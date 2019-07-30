package com.example.classchat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.classchat.R;

import java.util.Timer;
import java.util.TimerTask;

public class Activity_Flash extends AppCompatActivity {

    private int SPLASH_DISPLAY_LENGHT = 5;//界面停留的时间
    private TextView tip;
    Timer timer = new Timer();
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        getWindow().setFlags(flag, flag);
        setContentView(R.layout.activity__flash);
        initView();
        timer.schedule(task, 1000, 1000);//等待时间一秒，停顿时间一秒
        /**
         * 正常情况下不点击跳过
         */
        handler = new Handler();
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                //从闪屏界面跳转到首界面
                Intent intent = new Intent(Activity_Flash.this, Activity_Enter.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGHT*1000);//延迟5S后发送handler信息


        //避免从桌面启动程序后，会重新实例化入口的Activity
        //第三方平台安装app启动后，home键回到桌面后点击app启动时会再次启动入口类
        if (!this.isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }

    }
    private void initView() {
        tip = (TextView) findViewById(R.id.tip);//跳过
        tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //从闪屏界面跳转到首界面
                Intent intent = new Intent(Activity_Flash.this, Activity_Enter.class);
                startActivity(intent);
                finish();
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
            }
        });//跳过监听
    }
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() { // UI thread
                @Override
                public void run() {
                    SPLASH_DISPLAY_LENGHT--;
                    tip.setText("跳过" + SPLASH_DISPLAY_LENGHT);
                    if (SPLASH_DISPLAY_LENGHT < 0) {
                        timer.cancel();
                        tip.setVisibility(View.GONE);//倒计时到0隐藏字体
                    }
                }
            });
        }
    };
}
