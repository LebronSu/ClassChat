package com.example.classchat.Util;

import android.content.Context;
import android.widget.Toast;

//多次点击只显示一次
public class Util_ToastUtils {
    private static Context context = null;
    private static Toast toast = null;

    public static void showToast(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            toast.cancel();
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }
        toast.show();
    }

}
