package com.example.classchat.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.classchat.Adapter.Adapter_MyGoodsRecyclerView;
import com.example.classchat.Object.Object_Commodity;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.TouchTypeDetector;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.sdsmdg.tastytoast.TastyToast;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity_Market_MyGoods extends AppCompatActivity {

    private static final String TAG = "Activity_Market_MyGoods";

    private PullLoadMoreRecyclerView mPullLoadMoreRecyclerView;
    private Adapter_MyGoodsRecyclerView mAdapterMyGoodsRecyclerView;
    private ImageView back;
    private List<Object_Commodity>list = new ArrayList<>();
    private MaterialSearchBar searchBar;

    protected final static int RECEIVE_SUCCESS = 1;
    protected final static int RECEIVE_NULL = 2;

    private String userID = "17690710589";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__market__my_goods);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //After LOLLIPOP not translucent status bar
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //Then call setStatusBarColor.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.theme));
        }

        getData();

        TouchTypeDetector.TouchTypListener touchTypListener = new TouchTypeDetector.TouchTypListener() {
            @Override
            public void onDoubleTap() {}
            @Override
            public void onLongPress() {}
            @Override
            public void onScroll(int scrollDirection) {}
            @Override
            public void onSingleTap() {}
            @Override
            public void onSwipe(int swipeDirection) {
                switch (swipeDirection) {
                    case TouchTypeDetector.SWIPE_DIR_RIGHT:
                        finish();
                        Sensey.getInstance().stopTouchTypeDetection();
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onThreeFingerSingleTap() {}
            @Override
            public void onTwoFingerSingleTap() {}
        };
        Sensey.getInstance().startTouchTypeDetection(this,touchTypListener);

        back = findViewById(R.id.iv_market_my_goods_back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Sensey.getInstance().stopTouchTypeDetection();
            }
        });

        mPullLoadMoreRecyclerView = findViewById(R.id.my_goods_pullLoadMoreRecyclerView);
        mPullLoadMoreRecyclerView.setRefreshing(true);
        mPullLoadMoreRecyclerView.setPushRefreshEnable(false);
        mPullLoadMoreRecyclerView.setLinearLayout();
        mPullLoadMoreRecyclerView.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                //重置list
                if(list.size() > 0)
                    list.clear();
                getData();
            }

            @Override
            public void onLoadMore() {}
        });
        mPullLoadMoreRecyclerView.setAdapter(mAdapterMyGoodsRecyclerView);

        searchBar = findViewById(R.id.my_goods_searchBar);
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!searchBar.isSearchEnabled()){
                    //重置list,防止重复添加
                    if(list.size() > 0)
                        list.clear();
                    getData();
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence key) {
                InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                searchMyItem(key.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                switch (buttonCode) {
                    case MaterialSearchBar.BUTTON_BACK:
                        searchBar.disableSearch();
                        break;
                    default:
                        break;
                }
            }
        });

    }

    private void getData() {
        // 构建requestbody
        RequestBody requestBody = new FormBody.Builder()
                .add("userID", userID)
                .build();

        Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/getuseritems", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 得到服务器返回的具体内容
                String responseData = response.body().string();
                // 转化为具体的对象列表
                List<Object_Commodity> jsonlist = JSON.parseObject(responseData, new TypeReference<List<Object_Commodity>>(){});

                if(jsonlist.size() > 0){
                    for(Object_Commodity obj : jsonlist) {
                        list.add(obj);
                    }
                    Message message = new Message();
                    message.what = RECEIVE_SUCCESS;
                    handler.sendMessage(message);
                }
                else{
                    Message message = new Message();
                    message.what = RECEIVE_NULL;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 在这里对异常情况进行处理
                Log.d(TAG, "onFailure: 加载商品失败，没有收到商品");
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case RECEIVE_SUCCESS:
                    mPullLoadMoreRecyclerView.setLinearLayout();
                    mPullLoadMoreRecyclerView.setAdapter(new Adapter_MyGoodsRecyclerView(getApplicationContext(), list));
                    mPullLoadMoreRecyclerView.setPullLoadMoreCompleted();
                    break;
                case RECEIVE_NULL:
                    TastyToast.makeText(getApplicationContext(),"还没有商品，快去发布试试吧！", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                    break;
                default:
                    getData();
                    break;
            }
        }
    };

    //用于手势监听
    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        // Setup onTouchEvent for detecting type of touch gesture
        Sensey.getInstance().setupDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }


    private void searchMyItem(String key) {
        List<Object_Commodity> searchItems = new ArrayList<Object_Commodity>();
        //重置list
        if(searchItems.size() > 0)
            searchItems.clear();
        for(int i = 0; i < list.size() ; ++ i){
            if(list.get(i).getItemName().contains(key) || list.get(i).getBriefIntroduction().contains(key)){
                searchItems.add(list.get(i));
            }
        }

        if(searchItems.size() > 0){
            mPullLoadMoreRecyclerView.setLinearLayout();
            mPullLoadMoreRecyclerView.setAdapter(new Adapter_MyGoodsRecyclerView(this, searchItems));
            mPullLoadMoreRecyclerView.setPullLoadMoreCompleted();
        }
        else {
            TastyToast.makeText(this,"还没有发布过这类商品", TastyToast.LENGTH_SHORT, TastyToast.CONFUSING);
        }
    }

}
