package com.example.classchat.Activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coremedia.iso.Hex;
import com.example.classchat.Fragment.Fragment_ClassBox;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import io.rong.imkit.fragment.ConversationFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity_Conversation extends FragmentActivity {

    private static final String TAG = "Activity_Conversation";

    private ImageView remindView;
    private Boolean state;
    private String Id;
    private int userId;


    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    remindView.setImageResource(R.drawable.notification_on);
                    state = false;
                    break;
                case 2:
                    remindView.setImageResource(R.drawable.notification_off);
                    state = true;
                    break;
                case 3:
                    if (!state) {
                        remindView.setImageResource(R.drawable.notification_off);
                        state = true;
                    }else {
                        remindView.setImageResource(R.drawable.notification_on);
                        state = false;
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //After LOLLIPOP not translucent status bar
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //Then call setStatusBarColor.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.theme));
        }

        Id = getIntent().getData().getQueryParameter("targetId");
        Fragment_ClassBox fragment_classBox = new Fragment_ClassBox();
        userId = fragment_classBox.getId();

        ImageView imageView = findViewById(R.id.iv_return);

        remindView = findViewById(R.id.iv_reminder);

        remindView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i;
                if (state) {
                    i = 0;
                }else {
                    i = 1;
                }
                RequestBody requestBody = new FormBody.Builder()
                        .add("requestId", String.valueOf(userId))
                        .add("targetId", Id)
                        .add("isMuted", String.valueOf(i))
                        .build();

                Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/setmutestatus", requestBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Log.d(TAG, "onResponse: 孙铭泽" + response.body().string());
                        Message message = new Message();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                });
            }
        });

        TextView textView = findViewById(R.id.course_chat_title);

        textView.setText(getIntent().getData().getQueryParameter("title"));

        initUI();

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

    private void initUI() {
        RequestBody requestBody = new FormBody.Builder()
                .add("requestId", String.valueOf(userId))
                .add("targetId", Id)
                .build();

        Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/getmutestatus", requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d(TAG, "onResponse: 孙铭泽" + "i");
                if (Boolean.valueOf(response.body().string())) {
                    Message message = new Message();
                    message.what = 2;
                    handler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }
        });
    }
}
