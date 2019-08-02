package com.example.classchat.Activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.classchat.R;

import io.rong.imkit.fragment.ConversationFragment;

public class Activity_Conversation extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(option);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.rgb(255, 255, 255));// SDK21
        }

        ImageView imageView = findViewById(R.id.iv_return);

        FragmentManager fragmentManage = getSupportFragmentManager();
        ConversationFragment fragement = (ConversationFragment) fragmentManage.findFragmentById(R.id.conversation);
        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversation").appendPath(io.rong.imlib.model.Conversation.ConversationType.GROUP.getName().toLowerCase())
                .appendQueryParameter("targetId", getIntent().getData().getQueryParameter("targetId"))
                .appendQueryParameter("title", getIntent().getData().getQueryParameter("title"))
                .build();

        fragement.setUri(uri);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
