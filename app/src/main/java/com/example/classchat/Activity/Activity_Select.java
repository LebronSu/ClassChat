package com.example.classchat.Activity;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.classchat.R;
import com.example.classchat.Util.Util_NotesDB;

public class Activity_Select extends AppCompatActivity implements View.OnClickListener{

    private Button s_delete, s_back;
    private ImageView s_img;
    private VideoView s_video;
    private TextView s_tv;
    private Util_NotesDB notesDB;
    private SQLiteDatabase dbWriter;
    /** 点击item 的详情页 对数据进行删除操作*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__select);
        s_delete = (Button) findViewById(R.id.s_delete);
        s_back = (Button) findViewById(R.id.s_back);
        s_img = (ImageView) findViewById(R.id.s_img);
        s_video = (VideoView) findViewById(R.id.s_video);
        s_tv = (TextView) findViewById(R.id.s_tv);
        /**读写权限*/
        notesDB = new Util_NotesDB(this);
        dbWriter = notesDB.getWritableDatabase();

        s_back.setOnClickListener(this);
        s_delete.setOnClickListener(this);
        //判断是否存在图片
        if (getIntent().getStringExtra(Util_NotesDB.PATH).equals("null")) {
            s_img.setVisibility(View.GONE);
        } else {
            s_img.setVisibility(View.VISIBLE);
        }

        if (getIntent().getStringExtra(Util_NotesDB.VIDEO).equals("null")) {
            s_video.setVisibility(View.GONE);
        } else {
            s_video.setVisibility(View.VISIBLE);
        }
        // 设置需要显示的图片
        s_tv.setText(getIntent().getStringExtra(Util_NotesDB.CONTENT));

        Bitmap bitmap = BitmapFactory.decodeFile(getIntent().getStringExtra(
                Util_NotesDB.PATH));
        s_img.setImageBitmap(bitmap);

        s_video.setVideoURI(Uri
                .parse(getIntent().getStringExtra(Util_NotesDB.VIDEO)));

        s_video.start();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.s_delete:
                deleteDate();
                finish();
                break;

            case R.id.s_back:
                finish();
                break;
        }
    }
    //数据的删除
    public void deleteDate() {
        dbWriter.delete(Util_NotesDB.TABLE_NAME,
                "_id=" + getIntent().getIntExtra(Util_NotesDB.ID, 0), null);
    }
}
