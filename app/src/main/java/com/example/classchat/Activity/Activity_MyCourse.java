package com.example.classchat.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.classchat.Adapter.Adapter_Course;
import com.example.classchat.Fragment.Fragment_ClassBox;
import com.example.classchat.Object.Course;
import com.example.classchat.Object.MySubject;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.example.library_cache.Cache;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 这是我的课程界面
 */
public class Activity_MyCourse extends AppCompatActivity {

    private List<Course> courseList;
    private String userId;
    private String proUni;

    private RecyclerView recyclerView;
    private ImageView back;

    private static final String TAG = "Activity_MyCourse";

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Adapter_Course adapter_course = new Adapter_Course(courseList);
                    recyclerView.setAdapter(adapter_course);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__my_wallet);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        proUni = intent.getStringExtra("proUni");

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //After LOLLIPOP not translucent status bar
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //Then call setStatusBarColor.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.theme));
        }

        courseList = new ArrayList<>();

        initData();

        recyclerView = findViewById(R.id.rl_course);
        back = findViewById(R.id.iv_mywallet_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void initData() {
        courseList.clear();

        RequestBody requestBody = new FormBody.Builder()
                .add("userId", userId)
                .add("tablename", proUni)
                .build();

        Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/getmycourse", requestBody,new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 得到服务器返回的具体内容
                String responseData = response.body().string();
                // 转化为具体的对象列表
                JSONObject jsonObject = JSON.parseObject(responseData);

                for (String key : jsonObject.keySet()) {
                    courseList.add(new Course(key, jsonObject.getIntValue(key)));
                }

                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);

            }
        });
    }
}
