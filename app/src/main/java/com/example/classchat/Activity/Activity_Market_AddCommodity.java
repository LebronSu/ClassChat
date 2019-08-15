package com.example.classchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;
import com.sdsmdg.tastytoast.TastyToast;

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


public class Activity_Market_AddCommodity extends AppCompatActivity {

    private static final String TAG = "Activity_Market_AddComm";

    private ImageView addimageview1, addimageview2, addimageview3, back;
    private EditText itemname, itemintro, itemprice, itemdetail;
    private Button confirm;

    private static final int CHOOSE_PICTURE = 0;
    private static final int TAKE_PHOTO = 1;
    private static final int TAKE_PHOTO_1 = 4;
    private static final int TAKE_PHOTO_2 = 2;
    private static final int TAKE_PHOTO_3 = 3;
    private static final int CHOOSE_PICTURE_1 = 5;
    private static final int CHOOSE_PICTURE_2 = 6;
    private static final int CHOOSE_PICTURE_3 = 7;
    private static final int SAVE_SUCCESS = 11;
    private static final int SAVE_FAILED = 12;
    private Uri imageUri;

    private Bitmap itemiamge1 = null;
    private Bitmap itemiamge2 = null;
    private Bitmap itemiamge3 = null;

    // 初始化添加商品的等待控件
    private ProgressDialog loadingForAddCommodity;

    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    List<String> mPermissionList = new ArrayList<>();

    private final int mRequestCode = 100;//权限请求码

    private String ownerID;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case SAVE_SUCCESS:
                    TastyToast.makeText(Activity_Market_AddCommodity.this,"商品上传成功！",Toast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                    loadingForAddCommodity.dismiss();
                    finish();
                    break;
                case SAVE_FAILED:
                    TastyToast.makeText(Activity_Market_AddCommodity.this,"网络错误，再试试？",Toast.LENGTH_SHORT, TastyToast.ERROR).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__market_add_commodity);

        back = findViewById(R.id.iv_market_add_goods_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        if (Build.VERSION.SDK_INT >= 23){
            initPermission();
        }

        init();

//        // 创建专属文件夹，这个以后就是存放照片的地方，这个函数暂且先放这儿，后期应放入登录注册界面为正解
//        initdir();


        //TODO 获取用户ID
        ownerID = "17690710589";

        addimageview1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePicDialog(TAKE_PHOTO_1,CHOOSE_PICTURE_1);
            }
        });

        addimageview2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePicDialog(TAKE_PHOTO_2,CHOOSE_PICTURE_2);
            }
        });

        addimageview3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePicDialog(TAKE_PHOTO_3,CHOOSE_PICTURE_3);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((!itemname.getText().toString().equals(null))
                        &&(!itemintro.getText().toString().equals(null))
                        &&(!itemdetail.getText().toString().equals(null))
                        &&(!(itemiamge1 == null))
                        &&(!(itemiamge2 == null))
                        &&(!(itemiamge3 == null))
                        &&(!itemprice.getText().toString().equals(null))) {
                    List<File> imagelist = new ArrayList<>();
                    imagelist.add(compressImage(itemiamge1,"0"));
                    imagelist.add(compressImage(itemiamge2,"1"));
                    imagelist.add(compressImage(itemiamge3,"2"));
                    addItemToWeb(imagelist);
                }else {
                    TastyToast.makeText(Activity_Market_AddCommodity.this,"您的信息尚未输入完全，请补充完毕再上传",Toast.LENGTH_SHORT, TastyToast.CONFUSING).show();
                }
            }
        });
    }


    public void addItemToWeb(final List<File> imagelist){

        /*
        等待界面，因为登录操作是耗时操作
         */
        loadingForAddCommodity = new ProgressDialog(Activity_Market_AddCommodity.this);  //初始化等待动画
        loadingForAddCommodity.setCanceledOnTouchOutside(false); //
        loadingForAddCommodity.setMessage("正在上传....");  //等待动画的标题
        loadingForAddCommodity.show();  //显示等待动画

        /*
        开启网络线程，发送添加商品请求
         */
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("itemName", itemname.getText().toString())
                .addFormDataPart("ownerID", ownerID)
                .addFormDataPart("price", itemprice.getText().toString())
                .addFormDataPart("briefintroduction", itemintro.getText().toString())
                .addFormDataPart("detailIntroduction", itemdetail.getText().toString())
                .addFormDataPart("Image1", "image1", RequestBody.create(MediaType.parse("image/jpeg"), imagelist.get(0)))
                .addFormDataPart("Image2", "image2", RequestBody.create(MediaType.parse("image/jpeg"), imagelist.get(1)))
                .addFormDataPart("Image3", "image3", RequestBody.create(MediaType.parse("image/jpeg"), imagelist.get(2)))
                .build();   //构建请求体

        Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/uploadItem", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 得到服务器返回的具体内容
                boolean responseData = Boolean.parseBoolean(response.body().string());
                System.out.println(responseData);

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

    protected void showChoosePicDialog(final int imagecode, final int choosecode){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("上传商品图片");
        String[] items = { "选择本地照片", "拍照" };
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case CHOOSE_PICTURE: // 选择本地照片
                        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        openAlbumIntent.setType("image/*");
                        startActivityForResult(openAlbumIntent,choosecode);
                        break;
                    case TAKE_PHOTO:
                        if(ContextCompat.checkSelfPermission(Activity_Market_AddCommodity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        {
                            ActivityCompat.requestPermissions(Activity_Market_AddCommodity.this, new String[]{Manifest.permission.CAMERA}, imagecode);
                        }
                        else {
                            takephoto(imagecode);
                        }
                }
            }
        });
        builder.create().show();
    }


    public void onRequestPermissionsResult
            (int requestCode,String[] permissions, int[] grantResults) {

        boolean hasPermissionDismiss = false;//有权限没有通过

        if (requestCode ==TAKE_PHOTO_1||requestCode == TAKE_PHOTO_2||requestCode == TAKE_PHOTO_3) {



            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //权限获取成功
                takephoto(requestCode);

            } else {
                //权限被拒绝
                TastyToast.makeText(this, "获得权限失败！", Toast.LENGTH_SHORT, TastyToast.ERROR).show();
            }
        }else if(requestCode == mRequestCode){
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                System.out.println("权限未通过");
            }else{
                //全部权限通过，可以进行下一步操作。。。
            }


        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case CHOOSE_PICTURE_1:
                if (resultCode == RESULT_OK){
                    Bitmap bm = null;
                    try {
                        bm = getBitmapFormUri(data.getData());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addimageview1.setImageBitmap(bm);
                    itemiamge1 = bm;
                }
                break;
            case TAKE_PHOTO_1:
                if (resultCode == RESULT_OK){
                    Bitmap bm = null;
                    try {
                        bm = getBitmapFormUri(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addimageview1.setImageBitmap(bm);
                    itemiamge1 = bm;
                }
                break;
            case CHOOSE_PICTURE_2:
                if (resultCode == RESULT_OK){
                    Bitmap bm = null;
                    try {
                        bm = getBitmapFormUri(data.getData());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addimageview2.setImageBitmap(bm);
                    itemiamge2 = bm;
                }
                break;
            case TAKE_PHOTO_2:
                if (resultCode == RESULT_OK){
                    Bitmap bm = null;
                    try {
                        bm = getBitmapFormUri(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addimageview2.setImageBitmap(bm);
                    itemiamge2 = bm;
                }
                break;
            case CHOOSE_PICTURE_3:
                if (resultCode == RESULT_OK){
                    Bitmap bm = null;
                    try {
                        bm = getBitmapFormUri(data.getData());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addimageview3.setImageBitmap(bm);
                    itemiamge3 = bm;
                }
                break;
            case TAKE_PHOTO_3:
                if (resultCode == RESULT_OK){
                    Bitmap bm = null;
                    try {
                        bm = getBitmapFormUri(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addimageview3.setImageBitmap(bm);
                    itemiamge3 = bm;
                }
                break;
        }
    }


    public  File compressImage(Bitmap bitmap,String name) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 300) {  //循环判断如果压缩后图片是否大于500kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            long length = baos.toByteArray().length;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

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

    public static void recycleBitmap(Bitmap... bitmaps) {
        if (bitmaps == null) {
            return;
        }
        for (Bitmap bm : bitmaps) {
            if (null != bm && !bm.isRecycled()) {
                bm.recycle();
            }
        }
    }

    public void takephoto(int TAKE_PHOTO){
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
            imageUri = FileProvider.getUriForFile(Activity_Market_AddCommodity.this,
                    "com.example.classchat.FileProvider",outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

    public void init(){
        addimageview1 = findViewById(R.id.iv_market_add_itemPic1);
        addimageview2 = findViewById(R.id.iv_market_add_itemPic2);
        addimageview3 = findViewById(R.id.iv_market_add_itemPic3);
        itemname = findViewById(R.id.et_market_add_name);
        itemintro = findViewById(R.id.et_market_add_intro);
        itemprice = findViewById(R.id.et_market_add_price);
        itemdetail = findViewById(R.id.et_market_add_detail);
        confirm = findViewById(R.id.b_market_add_confirm);
    }

//    public RequestBody getRequestbody(File file){
//        MultipartBody.Builder builder = new MultipartBody.Builder();
////        for(File f :filelist){
//            builder.addFormDataPart("image" , file.getName() , RequestBody.create(MediaType.parse("image/png") , file));
////        }
//        return builder.build();
//    }

//    public Request getRequest(String url , File file){
//        Request.Builder builder = new Request.Builder();
//        builder.url(url).post(getRequestbody(file));
//        return builder.build();
//    }

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
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }else{
            //说明权限都已经通过，可以做你想做的事情去
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
        float ww = 480f;//这里设置宽度为480f
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

        return compressImage(bitmap);//再进行质量压缩
    }

    public Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
            if (options<=0)
                break;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
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