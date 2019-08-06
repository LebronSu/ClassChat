package com.example.classchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.example.classchat.Util.Util_PictureTool;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 这是实名认证界面
 */
public class Activity_IdAuthentation extends AppCompatActivity {
    private EditText name;
    private EditText id_card;
    private EditText st_card;
    private Spinner university;
    private Spinner college;
    private ImageView face;
    private ImageView card;
    private Button confirm;
    private ImageView back;
    private Context mContext;

    private static final String TAG = "Activity_IdAuthentation";

    private Boolean face_checked = false;
    private Boolean card_checked = false;

    private AlertDialog.Builder builder1;

    private JSONArray uni = new JSONArray();
    private JSONArray col = new JSONArray();

    private ArrayAdapter<String> adapter1 = null;
    private ArrayAdapter<String> adapter2 = null;

    // 等待控件
    private ProgressDialog loadingForAddCommodity;

    //辨别是照相还是选取文件
    final static int CHOOSE_PICTURE_HEAD = 2;
    final static int CHOOSE_PICTURE_CARD = 3;
    final static int TAKE_PHOTO_HEAD = 4;
    final static int TAKE_PHOTO_CARD = 5;


    // 照相时使用到的重要变量
    private Bitmap firstBitmap = null;
    private Bitmap secondBitmap = null;

    private Uri imageUri;


    // 上传数据库时所需要到的数据、
    private String userId_;
    private String realName_;
    private String studentId_;
    private String university_;
    private String school_;

    // 广播发射器
    private LocalBroadcastManager localBroadcastManager;

    /*
    设置handler接收网络线程的信号并处理
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    adapter1 = new ArrayAdapter(mContext, R.layout.item_auth,uni.toJavaList(String.class));
                    university.setAdapter(adapter1);
                    adapter2 = new ArrayAdapter(mContext, R.layout.item_auth,col.toJavaList(String.class));
                    college.setAdapter(adapter2);
                    loadingForAddCommodity.dismiss();
                    break;
                case 2:
                    Intent intent = new Intent("com.example.theclasschat_UPDATE_ACCOUNTINFO");
                    localBroadcastManager.sendBroadcast(intent);
                    Intent intent1 = new Intent("com.example.broadcasttest.UPDATE_STATE");
                    localBroadcastManager.sendBroadcast(intent1);
                    loadingForAddCommodity.dismiss();
                    Toast.makeText(Activity_IdAuthentation.this,"认证成功", Toast.LENGTH_SHORT).show();
                    finish();
                case 3:

                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__id_authentation);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //After LOLLIPOP not translucent status bar
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //Then call setStatusBarColor.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.theme));
        }

        // 从MainActivity获得学生ID
        Intent intent = getIntent();
        userId_ = intent.getStringExtra("userId");

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        mContext = this;
        name = (EditText) findViewById(R.id.auth_name);
//        id_card = (EditText) findViewById(R.id.auth_number);
        st_card = (EditText) findViewById(R.id.auth_st_number);
        face = (ImageView) findViewById(R.id.auth_photo_face);
        card = (ImageView) findViewById(R.id.auth_photo_card);
        confirm = (Button) findViewById(R.id.auth_confirm);
        university = (Spinner) findViewById(R.id.auth_university);
        college = (Spinner) findViewById(R.id.auth_colloge);
        back = findViewById(R.id.back_from_authentation_button);

        loadingForAddCommodity = new ProgressDialog(Activity_IdAuthentation.this);  //初始化等待动画
        loadingForAddCommodity.setCanceledOnTouchOutside(false); //
        loadingForAddCommodity.setMessage("正在上传....");  //等待动画的标题
        loadingForAddCommodity.show();  //显示等待动画

        // 从网络上取得所需的数据
        Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/index/getalluniversity", new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responsedata = response.body().string();
                System.out.println(responsedata);
                JSONObject jsonObject = JSON.parseObject(responsedata);
                col = jsonObject.getJSONArray("school");
                uni = jsonObject.getJSONArray("university");
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        });


        university.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                school_ = (String) adapter1.getItem(pos);
                System.out.println(school_);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        college.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                university_ = (String) adapter2.getItem(pos);
                System.out.println(university_);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePicDialog(100);
            }
        });
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePicDialog(150);
            }
        });


        //信息补全提示框1
        builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("提示");
        builder1.setMessage("请补全实名认证信息哦");
        builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });


        //提交审核按钮
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(name.getText()) || TextUtils.isEmpty(st_card.getText()) || !face_checked || !card_checked || university_ == null || school_ == null) {
                    builder1.show();
                } else {

                    realName_ = name.getText().toString();
//                    cardId_ = id_card.getText().toString();
                    studentId_ = st_card.getText().toString();
                    loadingForAddCommodity = new ProgressDialog(Activity_IdAuthentation.this);  //初始化等待动画
                    loadingForAddCommodity.setCanceledOnTouchOutside(false); //
                    loadingForAddCommodity.setMessage("正在上传....");  //等待动画的标题
                    loadingForAddCommodity.show();  //显示等待动画
                    Log.d(TAG, "university+ "+ university_);
                    Log.d(TAG, "school "+ school_);
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("userId", userId_)
                            .addFormDataPart("studentId", studentId_)
                            .addFormDataPart("university", school_)
                            .addFormDataPart("school", university_)
                            .addFormDataPart("realname", realName_)
                            .addFormDataPart("headico", "image1", RequestBody.create(MediaType.parse("image/jpeg"), BitmapToFile(firstBitmap, "real_head")))
                            .addFormDataPart("card", "image2", RequestBody.create(MediaType.parse("image/jpeg"), BitmapToFile(secondBitmap, "real_card")))
                            .build();

                    Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/authentation", requestBody, new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            Boolean responseData = Boolean.valueOf(response.body().string());
                            Log.d(TAG, "onResponse: "+ responseData);
                            if (responseData) {
                                Message message = new Message();
                                message.what = 2;
                                handler.sendMessage(message);
                            }
                        }
                    });
                }

            }
        });
    }

    // 选择使用照片还是使用拍照
    protected void showChoosePicDialog(final int partSend){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置头像");
        String[] items = { "选择本地照片", "拍照" };
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0: // 选择本地照片
                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                        intent.setType("image/*");
                        if (partSend == 100)
                            startActivityForResult(intent, CHOOSE_PICTURE_HEAD);
                        else
                            startActivityForResult(intent, CHOOSE_PICTURE_CARD);
                        break;
                    case 1:
                        if(ContextCompat.checkSelfPermission(Activity_IdAuthentation.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        {
                            if (partSend == 100)
                                ActivityCompat.requestPermissions(Activity_IdAuthentation.this, new String[]{Manifest.permission.CAMERA}, TAKE_PHOTO_HEAD);
                            else
                                ActivityCompat.requestPermissions(Activity_IdAuthentation.this, new String[]{Manifest.permission.CAMERA}, TAKE_PHOTO_CARD);
                        }
                        else {
                            if (partSend == 100) {
                                takePhoto(partSend);
                            }else
                                takePhoto(150);
                        }
                }
            }
        });
        builder.create().show();
    }

    // 得到结果后的回调函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case CHOOSE_PICTURE_HEAD:
                if (resultCode == RESULT_OK){
                    try {
                        firstBitmap = compressBitmapInQuality(data.getData());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    face.setImageBitmap(firstBitmap);
                    face_checked = true;
                }
                break;
            case CHOOSE_PICTURE_CARD:
                if (resultCode == RESULT_OK){
                    try {
                        secondBitmap = compressBitmapInQuality(data.getData());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    card.setImageBitmap(secondBitmap);
                    card_checked = true;
                }
                break;
            case TAKE_PHOTO_HEAD:
                if (resultCode == RESULT_OK){
                    try {
                        firstBitmap = compressBitmapInQuality(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    face.setImageBitmap(firstBitmap);
                    face_checked = true;
                }
                break;
            case TAKE_PHOTO_CARD:
                if (resultCode == RESULT_OK){
                    try {
                        secondBitmap = compressBitmapInQuality(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    card.setImageBitmap(secondBitmap);
                    card_checked = true;
                }
                break;
        }
    }

    // 拍照函数函数
    private void takePhoto(int partSend) {
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
            imageUri = FileProvider.getUriForFile(Activity_IdAuthentation.this,
                    "com.example.classchat.FileProvider",outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        if (partSend == 100)
            startActivityForResult(intent, TAKE_PHOTO_HEAD);
        else
            startActivityForResult(intent, TAKE_PHOTO_CARD);
    }


    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == TAKE_PHOTO_HEAD) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto(100);  // 权限授予成功
            } else {
                Toast.makeText(this, "获得照相权限失败！", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto(150);  // 权限授予成功
            } else {
                Toast.makeText(this, "获得照相权限失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 单纯的进行质量压缩，不进行尺寸压缩
     * @param uri 拍照返回的Uri
     * @return Bitmap 返回bitmap
     */
    public Bitmap compressBitmapInQuality(Uri uri) throws IOException {
        InputStream input = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input); // 这里直接把图片从流里取出，不过内存会一下子变大
        input.close();
        return bitmap;
    }

    public static File BitmapToFile(Bitmap bitmap, String name) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        File file = new File(Environment.getExternalStorageDirectory(), name + ".jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        return file;
    }

}





