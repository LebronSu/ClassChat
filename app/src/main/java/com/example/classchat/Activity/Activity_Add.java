package com.example.classchat.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

import com.example.classchat.R;
import com.example.classchat.Util.Util_NotesDB;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Activity_Add extends AppCompatActivity implements View.OnClickListener{


    private String val;
    private Button savebtn, deletebtn;
    private EditText ettext;
    private ImageView c_img;
    private VideoView v_video;
    private Util_NotesDB notesDB;
    private SQLiteDatabase dbWriter;
    private File phoneFile, videoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__add);
        //获取传递过来的数据
        val = getIntent().getStringExtra("flag");
        savebtn = (Button) findViewById(R.id.save);
        deletebtn = (Button) findViewById(R.id.delete);
        ettext = (EditText) findViewById(R.id.ettext);
        c_img = (ImageView) findViewById(R.id.c_img);
        v_video = (VideoView) findViewById(R.id.c_video);

        savebtn.setOnClickListener(this);
        deletebtn.setOnClickListener(this);

        notesDB = new Util_NotesDB(this);
        dbWriter = notesDB.getWritableDatabase();

        initView();
    }
    //判断存储的是文字，图片，还是视频，启动相对应的控件
    public void initView() {
        if (val.equals("1")) { // 文字
            c_img.setVisibility(View.GONE);
            v_video.setVisibility(View.GONE);
        }
        if (val.equals("2")) {
            c_img.setVisibility(View.VISIBLE);
            v_video.setVisibility(View.GONE);

            Intent img = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            phoneFile = new File(Environment.getExternalStorageDirectory()
                    .getAbsoluteFile() + "/" + getTime() + ".jpg");
            img.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(phoneFile));
            startActivityForResult(img, 1);
        }
        if (val.equals("3")) {
            c_img.setVisibility(View.GONE);
            v_video.setVisibility(View.VISIBLE);

            Intent video = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            videoFile = new File(Environment.getExternalStorageDirectory()
                    .getAbsoluteFile() + "/" + getTime() + ".mp4");
            video.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
            startActivityForResult(video, 2);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                addDB();
                finish();
                break;

            case R.id.delete:
                finish();
                break;
        }
    }
    //存储数据
    public void addDB() {
        ContentValues cv = new ContentValues();
        cv.put(Util_NotesDB.CONTENT, ettext.getText().toString());
        cv.put(Util_NotesDB.TIME, getTime());
        cv.put(Util_NotesDB.PATH, phoneFile + "");
        cv.put(Util_NotesDB.VIDEO, videoFile + "");
        dbWriter.insert(Util_NotesDB.TABLE_NAME, null, cv);
    }
    //时间的显示
    private String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date curDate = new Date();
        String str = format.format(curDate);
        return str;
    }
    //完成数据拍摄后直接显示数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            Bitmap bitmap = BitmapFactory.decodeFile(phoneFile
                    .getAbsolutePath());
            c_img.setImageBitmap(bitmap);
        }
        if (requestCode == 2) {
            v_video.setVideoURI(Uri.fromFile(videoFile));
            v_video.start();
        }
    }


}
