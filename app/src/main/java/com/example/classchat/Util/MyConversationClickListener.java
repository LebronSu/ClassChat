package com.example.classchat.Util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.classchat.Fragment.Fragment_ClassBox;
import com.example.classchat.R;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;


public class MyConversationClickListener implements RongIM.ConversationClickListener {
    private JSONObject object;
    private Fragment_ClassBox mcontext;

    public MyConversationClickListener(Fragment fragment){
        super();
        mcontext = (Fragment_ClassBox) fragment;
        object = mcontext.getSignstatus();
    }

    private AlertDialog.Builder builder;

    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
        return false;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
        return false;
    }

    @Override
    public boolean onMessageClick(Context context, View view, Message message) {
        if (message.getContent() instanceof LocationMessage ){
            if(((System.currentTimeMillis() - message.getSentTime()) < 1200000) && (!message.getExtra().equals("dsb"))){
                Toast.makeText(context , "快去签到吧！" ,Toast.LENGTH_SHORT).show();
                object.put("groupId" ,message.getTargetId() );
                object.put("status" , true);
                object.put("la" , ((LocationMessage) message.getContent()).getLat());
                object.put("lo" , ((LocationMessage) message.getContent()).getLng());
                message.setReadTime(0000);
                message.setExtra("dsb");
            }else if (((System.currentTimeMillis() - message.getSentTime()) < 1200000) && (message.getExtra().equals("dsb"))){
                Toast.makeText(context , "请勿重复获得位置信息！" ,Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context , "小伙计！迟到了呦.." ,Toast.LENGTH_SHORT).show();
                object.put("groupId" ,message.getTargetId() );
                object.put("status" , false);
            }
            return true;
        }else{
            return false;
        }

    }

    @Override
    public boolean onMessageLinkClick(final Context context, final String s, Message message) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uri = Uri.parse(s);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
        });
        builder.setMessage("您即将离开本应用， 打开网站，是否确定？");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        return false;
    }

    @Override
    public boolean onMessageLongClick(final Context context, View view, final Message message) {
        if (message.getContent() instanceof ImageMessage || message.getContent() instanceof FileMessage) {
            // TODO 自定义对话框,并定义点击事件
            View v = LayoutInflater.from(context).inflate(R.layout.conversation_popupwindow , null ,false);
            Button btn_collect = v.findViewById(R.id.btn_collect);
            final PopupWindow popupWindow = new PopupWindow(v , ViewGroup.LayoutParams.WRAP_CONTENT ,ViewGroup.LayoutParams.WRAP_CONTENT , true );
            popupWindow.setTouchable(true);
            popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                    // 这里如果返回true的话，touch事件将被拦截
                    // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
                }
            });

            btn_collect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!message.getReceivedStatus().isRead()){
                        Toast.makeText(context , "将文件保存自动收藏.." , Toast.LENGTH_SHORT).show();
                        popupWindow.dismiss();
                    }else{
                        Toast.makeText(context , "文件已收藏至藏经阁.." , Toast.LENGTH_SHORT).show();
                        popupWindow.dismiss();
                    }


                }
            });
            popupWindow.showAsDropDown(view );


            return true;
        }else
            return false;
    }
}
