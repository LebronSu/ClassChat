package com.example.classchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.example.classchat.Util.Util_PictureTool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity_AccountInfo extends AppCompatActivity {
    private ProgressDialog loadingUpload;

    // 声明一个数组permissions，将需要的权限都放在里面
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
    // 创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<>();

    final int mFirstRequestCode = 100;//权限请求码
    final int mPhoteRequestCode = 150;//照相权限申请码


    //辨别是照相还是选取文件
    final static int CHOOSE_PICTURE = 0;
    final static int TAKE_PHOTO = 1;

    final int NAME_ACTIVITY = 200;//开启修改昵称请求码

    private static final int SAVE_SUCCESS = 11;
    private static final int SAVE_FAILED = 12;

    // 控件作为成员存在
    private RelativeLayout headImageLayout;
    private RelativeLayout nameLayout;
    private RelativeLayout passwordLayout;
    private TextView tvName;
    private TextView tvId;
    private ImageView ivReturn;

    // 照相时使用到的重要变量
    private Bitmap headbitmap = null;
    private ImageView headimage;

    private Uri imageUri;

    private String userId;
    private String userName;
    private String imageUrl;

    // 广播发射器
    private LocalBroadcastManager localBroadcastManager;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case SAVE_SUCCESS:
                    Toast.makeText(Activity_AccountInfo.this,"保存成功！",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("com.example.theclasschat_UPDATE_ACCOUNTINFO");
                    localBroadcastManager.sendBroadcast(intent);
                    loadingUpload.dismiss();
                    break;
                case SAVE_FAILED:
                    Toast.makeText(Activity_AccountInfo.this,"网络错误，再试试？",Toast.LENGTH_SHORT).show();
                    loadingUpload.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__account_info);

//        // 去除ActionBar
//        ActionBar actionBar = getSupportActionBar();
//        assert actionBar != null;
//        actionBar.hide();

        // TODO 获得学生ID
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");
        imageUrl = intent.getStringExtra("headUrl");
//        imageUrl = "http://106.12.105.160/default_ico.png";
//        userId = "18801356149";
//        userName = "唠课";

        initPermission();

        init(); // 初始化控件

        tvName.setText(userName);
        tvId.setText(userId);
        Glide.with(this).load(imageUrl).into(headimage);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        /*
        为头像布局设置监听器
         */
        headImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePicDialog();
            }
        });

        /*
        为昵称布局设置监听器
         */
        nameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_AccountInfo.this, Activity_Modify.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                startActivityForResult(intent, NAME_ACTIVITY);
            }
        });

        /*
        为密码布局设置监听器
         */
        passwordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_AccountInfo.this, Activity_Password.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    // 初始权限申请函数
    private void initPermission() {
        mPermissionList.clear();//清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mFirstRequestCode);
        }else{
            // 感谢您的配合现在开始使用我们畅快淋漓的课程表体验吧！！！
        }
    }

    // 初始化控件
    private void init() {
        headImageLayout = findViewById(R.id.rl_personalinfo_headimage);
        nameLayout = findViewById(R.id.rl_personalinfo_name);
        passwordLayout = findViewById(R.id.rl_personalinfo_password);
        headimage = findViewById(R.id.iv_personalinfo_headimage);
        tvName = findViewById(R.id.tv_personalinfo_name);
        tvId = findViewById(R.id.tv_personalinfo_id);
        ivReturn = findViewById(R.id.iv_personalinfo_return);
    }

    // 选择使用照片还是使用拍照
    protected void showChoosePicDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置头像");
        String[] items = { "选择本地照片", "拍照" };
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case CHOOSE_PICTURE: // 选择本地照片
                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_PICTURE);
                        break;
                    case TAKE_PHOTO:
                        if(ContextCompat.checkSelfPermission(Activity_AccountInfo.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        {
                            ActivityCompat.requestPermissions(Activity_AccountInfo.this, new String[]{Manifest.permission.CAMERA}, mPhoteRequestCode);
                        }
                        else {
                            takePhoto();
                        }
                }
            }
        });
        builder.create().show();
    }

    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mFirstRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
//                showPermissionDialog();//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
            }else{
                // 初始权限全部通过啦，感谢您的配合
            }
        } else if(mPhoteRequestCode == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();  // 权限授予成功
            } else {
                // 权限授予失败
                Toast.makeText(this, "获得照相权限失败！", Toast.LENGTH_SHORT).show();
            }
        }

    }

    // 拍照函数函数
    private void takePhoto() {
        //创建File对象，用于存储拍照后的照片
        File outputImage = new File(getExternalCacheDir(),
                "output_image.jpg");
        try {
            if (outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24){
            imageUri = FileProvider.getUriForFile(Activity_AccountInfo.this,
                    "com.example.theclasschat.camera.fileprovider",outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    // 得到结果后的回调函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case CHOOSE_PICTURE:
                if (resultCode == RESULT_OK){
                    try {
                        headbitmap = getBitmapFormUri(data.getData());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    headimage.setImageBitmap(headbitmap);
                    sendImageToServer();
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK){
                    try {
                        headbitmap = getBitmapFormUri(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    headimage.setImageBitmap(headbitmap);
                    sendImageToServer();
                }
                break;
            case NAME_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String newName = data.getStringExtra("name_return");
                    tvName.setText(newName);
                    Intent intent = new Intent("com.example.theclasschat_UPDATE_ACCOUNTINFO");
                    localBroadcastManager.sendBroadcast(intent);
                }
        }
    }

    public Bitmap getBitmapFormUri(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = getContentResolver().openInputStream(uri);

        //这一段代码是不加载文件到内存中也得到bitmap的真是宽高，主要是设置inJustDecodeBounds为true
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;//不加载到内存
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.RGB_565;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;

        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 800f;//这里设置宽度为480f
        //缩放比，由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        input = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return Util_PictureTool.compressImage(bitmap);//再进行质量压缩
    }



    private void sendImageToServer() {
        /*
        等待界面，因为登录操作是耗时操作
         */
        loadingUpload = new ProgressDialog(Activity_AccountInfo.this);  //初始化等待动画
        loadingUpload.setCanceledOnTouchOutside(false); //
        loadingUpload.setMessage("正在上传....");  //等待动画的标题
        loadingUpload.show();  //显示等待动画

        /*
        开启网络线程
         */
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", userId)
                .addFormDataPart("icon", "head_image", RequestBody.create(MediaType.parse("image/jpeg"), Util_PictureTool.compressImage(headbitmap, "new_head")))
                .build();   //构建请求体

        Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/changeico", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 得到服务器返回的具体内容
                boolean responseData = Boolean.parseBoolean(response.body().string());

                Message message = new Message();    // 准备发送信息通知UI线程

                if(responseData) {
                    message.what = SAVE_SUCCESS;
                    handler.sendMessage(message);   // 登录成功
                } else {
                    message.what = SAVE_FAILED;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 在这里对异常情况进行处理
            }
        });
    }

}
