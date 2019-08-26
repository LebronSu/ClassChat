package com.example.classchat.Activity;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.classchat.Object.MySubject;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.example.library_cache.Cache;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity_AutoPullCourseFromWeb extends AppCompatActivity {
    private String userId;
    private String proUni;
    private WebView webView;
    private String cookie;
    private List<MySubject> mysubjects;
    // 初始化添加商品的等待控件
    private ProgressDialog loadingForAddCommodity;
    //广播
    private LocalBroadcastManager localBroadcastManager;
    //handler处理反应回来的信息
    private static final String TAG = "Activity_AutoPullCourse";
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(final Message msg){
            switch (msg.what){
                case 1:
                    OkHttpClient client =new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("xnm" ,"2019" )
                            .add("xqm" , "3")
                            .build();
                    Request request =new Request.Builder()
                            .url("http://xsjw2018.scuteo.com/jwglxt/kbcx/xskbcx_cxXsKb.html?gnmkdm=N2151")
                            .addHeader("Cookie" , cookie)
                            .post(body)
                            .build();
                    client.newCall(request).enqueue(new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject object = JSON.parseObject(response.body().string());
                            JSONArray array = object.getJSONArray("kbList");
                            System.out.println(array.size());
                            //新建一个装subject的列表

                            mysubjects = new ArrayList<>();
                            //下面的过程就是把这些课都转化成Mysubject对象
                            for(int i = 0 ; i< array.size() ; i++ ){
                                //从kbList中拿到一节课的JSON
                                JSONObject course = array.getJSONObject(i);
                                MySubject subject = new MySubject();
                                //获取课程名称
                                subject.setName(course.getString("kcmc"));
                                Log.d(TAG, "onResponse: " + subject.getName());
                                //获取课程代码
                                subject.setId(course.getString("kch_id"));

                                //获取课程开始和结束的节数
                                String[] lasttime = course.getString("jcor").split("-");
                                subject.setStart(Integer.parseInt(lasttime[0]));
                                subject.setStep(Integer.parseInt(lasttime[1])  - Integer.parseInt(lasttime[0])  + 1);

                                //获取课程授课教室
                                subject.setRoom(course.getString("cdmc"));
                                //获取老师的名字
                                subject.setTeacher(course.getString("xm"));
                                //获取上课周列表
                                String target = course.getString("zcd");
                                subject.setWeekList(parseCourse(target));

                                //获取上课是哪一天
                                subject.setDay(Integer.parseInt(course.getString("xqj")));
                                mysubjects.add(subject);

                            }
                            // 向数据库发送待调整的mysubjects
                            Log.d(TAG, "onResponse: " + mysubjects);
                            Message message = new Message();
                            message.what = 2;
                            handler.sendMessage(message);
                        }
                    });
                    break;

                case 2:
                    //这里需要向服务器发出这些课程，服务器再返回合格的mysubjects
                    RequestBody requestBody = new FormBody.Builder()
                            .add("userId", userId)
                            .add("subjects", JSON.toJSONString(mysubjects))
                            .add("tablename" , proUni)
                            .build();
                    Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/autoupdatecourse", requestBody, new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String responseData = response.body().string();
                            List<String> jsonlist = JSON.parseArray(responseData, String.class);
                            mysubjects.clear();
                            for(String s : jsonlist) {
                                MySubject mySubject = JSON.parseObject(s, MySubject.class);
                                mysubjects.add(mySubject);
                            }
                            //获得数据后存入缓存
                            Cache.with(Activity_AutoPullCourseFromWeb.this)
                                    .path(getCacheDir(Activity_AutoPullCourseFromWeb.this))
                                    .remove("classBox");

                            Cache.with(Activity_AutoPullCourseFromWeb.this)
                                    .path(getCacheDir(Activity_AutoPullCourseFromWeb.this))
                                    .saveCache("classBox", responseData);
                            // 发送信息给 handler
                            Message message = new Message();
                            message.what = 3;
                            handler.sendMessage(message);
                        }
                    });
                    break;
                case 3:
                    // 结束等待转圈画面 跳转回课程表fragment 刷新其页面（广播）
                    Intent intent1 = new Intent("com.example.broadcasttest.LOCAL_BROADCAST1");
                    localBroadcastManager.sendBroadcast(intent1);
                    loadingForAddCommodity.dismiss();
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__auto_pull_course_from_web);
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        proUni = intent.getStringExtra("proUni");
        System.out.println(userId);
        System.out.println(proUni);
        //广播
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        webView = findViewById(R.id.auto_wv);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view , String url , Bitmap fa){
                super.onPageStarted(view ,url ,fa);
                if(!url.equals("https://sso.scut.edu.cn/cas/login?service=http%3A%2F%2Fxsjw2018.scuteo.com%2Fsso%2Fdriotlogin")){
                    // 等待界面
                    loadingForAddCommodity = new ProgressDialog(Activity_AutoPullCourseFromWeb.this);  //初始化等待动画
                    loadingForAddCommodity.setCanceledOnTouchOutside(false); //
                    loadingForAddCommodity.setMessage("正在上传....");  //等待动画的标题
                    loadingForAddCommodity.show();  //显示等待动画
                }
            }

            @Override
            public void onPageFinished(WebView webView ,String url){
                if(!url.equals("https://sso.scut.edu.cn/cas/login?service=http%3A%2F%2Fxsjw2018.scuteo.com%2Fsso%2Fdriotlogin")){
                    CookieManager manager = CookieManager.getInstance();
                    System.out.println(webView.getUrl());
                    cookie = manager.getCookie(webView.getUrl());
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }else {
                    System.out.println("this is origin");
                }
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setDisplayZoomControls(true);
        webSettings.setDefaultFontSize(12);
        CookieManager manager = CookieManager.getInstance();
        webView.loadUrl("https://sso.scut.edu.cn/cas/login?service=http%3A%2F%2Fxsjw2018.scuteo.com%2Fsso%2Fdriotlogin");
    }
    public List<Integer> parseCourse(String data){
        List<Integer> weekList = new ArrayList<>();

        String[] originList = data.split(",");
        for(int i =0 ; i <originList.length ; i++){
            int start;
            int end;
            String[]  s = originList[i].split("周");
            if(s[0].split("-").length > 1){
                start = Integer.parseInt(s[0].split("-")[0]);
                end = Integer.parseInt(s[0].split("-")[1]);
            }else {
                start = Integer.parseInt(s[0]);
                end = Integer.parseInt(s[0]);
            }

            if(s.length > 1){
                for(int j = start ; j <= end ; j+=2 ){
                    weekList.add(j);
                }
            }else {
                for (int j = start ; j <= end ; j++){
                    weekList.add(j);
                }
            }
        }
        return weekList;
    }

    /*
     * 获得缓存地址
     * */
    public String getCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }
}
