package com.example.classchat.Activity;

import android.os.Build;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.classchat.R;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * 这是个人信息碎片里的关于我们界面
 */
public class Activity_AboutUs extends AppCompatActivity {

    // 注册控件
    private RelativeLayout relativeLayout;
    private ImageView about_return;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__about_us);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //After LOLLIPOP not translucent status bar
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //Then call setStatusBarColor.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.theme));
        }
        init();

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription("唠课团队历时四个月打磨出的App。学生可以查课表，泡论坛，逛同校商城，有专属的课程群聊，还能够有专属的笔记空间；老师可以轻松准确的传达作业和信息，有课程资料上传专区；学校可以管理学生考勤，了解学生民意。" +
                        "唠课有以下几个特点：" +
                        "1.功能简洁，做到“麻雀虽小 五脏俱全”。" +
                        "2.刷脸   GPS定位 双重考勤" +
                        "3.严格的实名认证带来稳定的社区生态" +
                        "4.唠课论坛帮助学生建立自己的交流平台                                \n\n\n" +
                        "  唠课源于学生，服务学生！")//介绍
                .addItem(new Element().setTitle("Version 1.0"))
                .addGroup("与我们联系")
                .addWebsite("http://www.classchat.club")//网站
                .addEmail("1836828080@qq.com")
                .create();

        relativeLayout.addView(aboutPage);

        about_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }); // 返回按钮逻辑
    }

    private void init() {
        relativeLayout = (RelativeLayout) findViewById(R.id.about_relativeLayout);
        about_return = findViewById(R.id.about_iv_return);
    }


}
