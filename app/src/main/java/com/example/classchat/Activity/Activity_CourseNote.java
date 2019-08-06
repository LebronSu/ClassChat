package com.example.classchat.Activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.classchat.Adapter.Adapter_MyAdapter;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NotesDB;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

/**
 * 这是课程笔记界面
 */
public class Activity_CourseNote extends AppCompatActivity implements View.OnClickListener{

    private Button textbtn, imgbtn, videobtn;
    private ListView lv;
    private Intent i;
    private Adapter_MyAdapter adapter;
    private Util_NotesDB notesDB;
    private SQLiteDatabase dbReader;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__course_note);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //After LOLLIPOP not translucent status bar
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //Then call setStatusBarColor.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.theme));
        }
        initView();
    }

    public void initView() {
        lv = (ListView) findViewById(R.id.list);
        textbtn = (Button) findViewById(R.id.text);
        imgbtn = (Button) findViewById(R.id.img);
        videobtn = (Button) findViewById(R.id.video);

        textbtn.setOnClickListener(this);
//        imgbtn.setOnClickListener(this);
//        videobtn.setOnClickListener(this);

        notesDB = new Util_NotesDB(this);
        dbReader = notesDB.getReadableDatabase();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                cursor.moveToPosition(position);
                Intent i = new Intent(Activity_CourseNote.this, Activity_Select.class);
                i.putExtra(Util_NotesDB.ID,
                        cursor.getInt(cursor.getColumnIndex(Util_NotesDB.ID)));
                i.putExtra(Util_NotesDB.CONTENT, cursor.getString(cursor
                        .getColumnIndex(Util_NotesDB.CONTENT)));
                i.putExtra(Util_NotesDB.TIME,
                        cursor.getString(cursor.getColumnIndex(Util_NotesDB.TIME)));
                i.putExtra(Util_NotesDB.PATH,
                        cursor.getString(cursor.getColumnIndex(Util_NotesDB.PATH)));
                i.putExtra(Util_NotesDB.VIDEO,
                        cursor.getString(cursor.getColumnIndex(Util_NotesDB.VIDEO)));
                startActivity(i);
            }
        });

    }

    /** 点击添加数据 */
    @Override
    public void onClick(View v) {
        i = new Intent(this, Activity_Add.class);
        switch (v.getId()) {
            case R.id.text:
                i.putExtra("flag", "1");
                startActivity(i);
                break;

            case R.id.img:
                i.putExtra("flag", "2");
                startActivity(i);
                break;

            case R.id.video:
                i.putExtra("flag", "3");
                startActivity(i);
                break;
        }
    }

    /** listview的数据源 */
    public void selectDB() {
        cursor = dbReader.query(Util_NotesDB.TABLE_NAME, null, null, null, null,
                null, null);
        adapter = new Adapter_MyAdapter(this, cursor);
        lv.setAdapter(adapter);
    }

    /** 在点击保存数据 回到主界面后调用 */
    @Override
    protected void onResume() {
        super.onResume();
        selectDB();
    }

}
