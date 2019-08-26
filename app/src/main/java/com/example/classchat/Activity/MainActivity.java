package com.example.classchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.classchat.Fragment.Fragment_ClassBox;
import com.example.classchat.Fragment.Fragment_Forum;
import com.example.classchat.Fragment.Fragment_SelfInformationCenter;
import com.example.classchat.Fragment.Fragment_Market;
import com.example.classchat.R;
import com.example.classchat.Util.ChangeStatusBarColor;
import com.example.classchat.Util.MyConversationClickListener;
import com.example.classchat.Util.Util_NetUtil;
import com.github.nisrulz.sensey.Sensey;
import com.sdsmdg.tastytoast.TastyToast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.LocationMessage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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
    private String token;
    private String headUrl;

    private FragmentManager manager = getSupportFragmentManager();
    private long firstTime;// 记录点击返回时第一次的时间毫秒值

    private String sendGroupId;

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

    /*
    设置handler接收网络线程的信号并处理
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        public void handleMessage(final android.os.Message msg){
            switch (msg.what){
                case 1:
                    Fragment_ClassBox fragment = (Fragment_ClassBox) mFragments.get(0);
                    String tablename = fragment.getProUni();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("time", String.valueOf(System.currentTimeMillis()))
                            .add("groupId", sendGroupId)
                            .add("tablename", tablename)
                            .build();
                    Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/course/setsigntime", requestBody, new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                android.os.Message message = new android.os.Message();
                                message.what = 2;
                                handler.sendMessage(message);
                        }
                    });
                    break;
                default:
                case 2:
                    Toast.makeText(MainActivity.this, "老师您好， 您的签到信息已经成功发出", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


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

                                    System.exit(0);
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
        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //After LOLLIPOP not translucent status bar
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //Then call setStatusBarColor.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.theme));
        }
        Intent intent = getIntent();
        Log.d(TAG, "onCreate: "+ sHA1(MainActivity.this));
        correctId = intent.getStringExtra("userId");
        isAuthentation = intent.getBooleanExtra("userAuthentationStatus", false);
        nickName = intent.getStringExtra("userName");
        imageUrl = intent.getStringExtra("userImage");
        password = intent.getStringExtra("userPassword");
        proUni = intent.getStringExtra("proUni");
        realName = intent.getStringExtra("realName");
        token = intent.getStringExtra("token");
        headUrl = intent.getStringExtra("headUrl");

        //        initView();
        initBottomNavigation();
        initData();

        RongIM.init(MainActivity.this);

        RongIM.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
            @Override
            public boolean onReceived(Message message, int i) {
                Fragment_ClassBox fragment = (Fragment_ClassBox) mFragments.get(0);
                int count = fragment.groupChatManager.getInteger(message.getTargetId());
                fragment.groupChatManager.put(message.getTargetId(), count + 1);
                fragment.updateUI(message.getTargetId());
                return false;
            }
        });

        RongIM.getInstance().setSendMessageListener(new RongIM.OnSendMessageListener() {
            @Override
            public Message onSend(Message message) {
                if (message.getContent() instanceof LocationMessage) {
                    sendGroupId = message.getTargetId();
                    android.os.Message message1 = new android.os.Message();
                    message1.what = 1;
                    handler.sendMessage(message1);
                    return message;
                }
                return message;
            }

            @Override
            public boolean onSent(Message message, RongIM.SentMessageErrorCode sentMessageErrorCode) {
                return false;
            }
        });

        RongIM.setConversationClickListener(new MyConversationClickListener(mFragments.get(0)));

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
                        // 设置沉浸式状态栏
                        if (Build.VERSION.SDK_INT >= 21) {
                            Window window = getWindow();
                            //After LOLLIPOP not translucent status bar
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            //Then call setStatusBarColor.
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            window.setStatusBarColor(getResources().getColor(R.color.theme));
                        }
                        break;
                    case R.id.menu_forum:
                        if (Build.VERSION.SDK_INT >= 21) {
                            Window window = getWindow();
                            //After LOLLIPOP not translucent status bar
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            //Then call setStatusBarColor.
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            window.setStatusBarColor(getResources().getColor(R.color.theme));
                        }
                        setFragmentPosition(1);
                        break;
                    case R.id.menu_market:
                        if (Build.VERSION.SDK_INT >= 21) {
                            Window window = getWindow();
                            //After LOLLIPOP not translucent status bar
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            //Then call setStatusBarColor.
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            window.setStatusBarColor(getResources().getColor(R.color.theme));
                        }
                        setFragmentPosition(2);
                        break;
                    case R.id.menu_home:
                        if (Build.VERSION.SDK_INT >= 21) {
                            Window window = getWindow();
                            //After LOLLIPOP not translucent status bar
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            //Then call setStatusBarColor.
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            window.setStatusBarColor(getResources().getColor(R.color.theme));
                        }
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

    public String getRealName(){return realName;}

    public String getToken(){return token;}

    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length()-1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    @Override
    public Resources getResources() {//禁止app字体大小跟随系统字体大小调节
        Resources resources = super.getResources();
        if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
            android.content.res.Configuration configuration = resources.getConfiguration();
            configuration.fontScale = 1.0f;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        return resources;
    }
}