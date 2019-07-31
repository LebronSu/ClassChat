package com.example.classchat.Activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.classchat.Fragment.Fragment_ClassBox;
import com.example.classchat.Fragment.Fragment_Forum;
import com.example.classchat.Fragment.Fragment_SelfInformationCenter;
import com.example.classchat.Fragment.Fragment_Market;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.github.nisrulz.sensey.Sensey;
import com.sdsmdg.tastytoast.TastyToast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //    private Toolbar mToolbar;
    private BottomNavigationView mBottomNavigationView;
    private int lastIndex;

    private String correctId;
    private boolean isAuthentation;
    private String password;
    private String imageUrl;
    private String nickName;
    private String proUni;
    private String realName;

    private FragmentManager manager = getSupportFragmentManager();
    private long firstTime;// 记录点击返回时第一次的时间毫秒值

    List<Fragment> mFragments;
    AlertDialog builder=null;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if (manager.getBackStackEntryCount() != 0) {
                manager.popBackStack();
            } else {
                exitApp(2000);// 退出应用
            }
        }
        return true;
    }


    private void exitApp(long timeInterval) {
        // 第一次肯定会进入到if判断里面，然后把firstTime重新赋值当前的系统时间
        // 然后点击第二次的时候，当点击间隔时间小于2s，那么退出应用；反之不退出应用
        if (System.currentTimeMillis() - firstTime >= timeInterval) {
            TastyToast.makeText(this, "再按一次退出程序", TastyToast.LENGTH_SHORT,TastyToast.WARNING).show();
            firstTime = System.currentTimeMillis();
        } else {
            builder = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("温馨提示：")
                    .setMessage("您是否要退出程序？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    MainActivity.this.finish();
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    builder.dismiss();
                                }
                            }).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        correctId = intent.getStringExtra("userId");
        isAuthentation = intent.getBooleanExtra("userAuthentationStatus", false);
        nickName = intent.getStringExtra("userName");
        imageUrl = intent.getStringExtra("userImage");
        password = intent.getStringExtra("userPassword");
        proUni = intent.getStringExtra("proUni");
        realName = intent.getStringExtra("realName");

//        initView();
        initBottomNavigation();
        initData();

        //防止在商城搜索时导航栏上移到键盘上方
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

//    public void initView() {
//        mToolbar = findViewById(R.id.toolbar);
//
//    }

    public void initData() {
//        setSupportActionBar(mToolbar);
        mFragments = new ArrayList<>();
        mFragments.add(new Fragment_ClassBox());
        mFragments.add(new Fragment_Forum());
        mFragments.add(new Fragment_Market());
        mFragments.add(new Fragment_SelfInformationCenter());
        // 初始化展示MessageFragment
        setFragmentPosition(0);
    }

    public void initBottomNavigation() {
        mBottomNavigationView = findViewById(R.id.bv_bottomNavigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_classtable:
                        setFragmentPosition(0);
                        break;
                    case R.id.menu_forum:
                        setFragmentPosition(1);
                        break;
                    case R.id.menu_market:
                        setFragmentPosition(2);
                        break;
                    case R.id.menu_home:
                        setFragmentPosition(3);
                        break;
                    default:
                        break;
                }
                // 这里注意返回true,否则点击失效
                return true;
            }
        });
    }


    private void setFragmentPosition(int position) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment currentFragment = mFragments.get(position);
        Fragment lastFragment = mFragments.get(lastIndex);
        lastIndex = position;
        ft.hide(lastFragment);
        if (!currentFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
            ft.add(R.id.ll_frameLayout, currentFragment);
        }
        ft.show(currentFragment);
        ft.commitAllowingStateLoss();
    }

    //用于手势监听
    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        // Setup onTouchEvent for detecting type of touch gesture
        Sensey.getInstance().setupDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onResume() {
        /**
         * 从购物车跳转到商城首页
         */
        int id = getIntent().getIntExtra("id", 0);
        if (id == 1) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.ll_frameLayout,new Fragment_Market())
                    .addToBackStack(null)
                    .commit();
        }

        super.onResume();
    }

    public String getId() {
        return correctId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getAuthentation() {return isAuthentation;}

    public String getProUni() {return proUni;}

    public String getRealName(){return realName;};

}