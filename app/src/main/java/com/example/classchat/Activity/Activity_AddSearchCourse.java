package com.example.classchat.Activity;

//搜索添加课程界面

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.classchat.Adapter.Adapter_SearchCourseListView;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.example.classchat.model.AddCourseDataBase;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Activity_AddSearchCourse extends AppCompatActivity {

    //声明控件
    private EditText edit;
    private ListView list;
    private AlertDialog.Builder builder;
    private Button back;
    private Button add;
    private Context mContext;
    private List<AddCourseDataBase> items = new ArrayList<>();

    private String userId;
    private String proUni;

    private static final int GET = 1;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case GET:
                    list.setAdapter(new Adapter_SearchCourseListView(mContext,items,userId));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__add_search_course);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        proUni = intent.getStringExtra("proUni");


        //绑定控件
        edit=(EditText)findViewById(R.id.edit_search);
        back=(Button)findViewById(R.id.back_from_searchAddCourse_button);
        add=(Button)findViewById(R.id.add_personally);
        list = (ListView) findViewById(R.id.search_list);
        mContext=this;





        //手动添加提示框；
        builder = new AlertDialog.Builder(this);
        builder.setTitle("提示：");
        builder.setMessage("手动添加的课程可能无法使用唠课的聊天功能哦，是否确定？");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.setClass(Activity_AddSearchCourse.this,Activity_AddCourse.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });



      //搜索编辑框的文字监听
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                //todo s.toString()为输入的关键字，这里需要写把关键字发给数据库和返回一个包含(id，课程名，老师，专业)
                // （固定顺序）的二维string数组的函数，数组给hoolder赋值；
                final RequestBody requestBody = new FormBody.Builder()
                        .add("tablename", proUni)
                        .add("condition", s.toString())
                        .build();

                Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/searchcourse", requestBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                        String responsedata = response.body().string();
                        System.out.println(responsedata);
                        try {
                            JSONArray jsonArray = new JSONArray(responsedata);
                            items.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                AddCourseDataBase item=new AddCourseDataBase(jsonObject.getString("groupChatId"), jsonObject.getString("courseName"), jsonObject.getString("teacher"));
                                items.add(item);
                            }
                            Message message = new Message();
                            message.what = GET;
                            handler.sendMessage(message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });



        //返回
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //跳转手动添加
        add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               builder.show();
            }
        });
    }

}


