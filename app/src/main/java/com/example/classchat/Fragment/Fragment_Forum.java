package com.example.classchat.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.classchat.Activity.MainActivity;
import com.example.classchat.R;

import org.json.JSONObject;

public class Fragment_Forum extends Fragment {

//    //  浏览器控件
//    private WebView webView;
//
//    // 用户Id和密码
//    private String userId;
//    private String password;
//
//    // 控制登录次数
//    private int i = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_fragment__forum, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        MainActivity mainActivity = (MainActivity)getActivity();
//        userId = mainActivity.getId();
//        password = mainActivity.getPassword();
//        webView = getActivity().findViewById(R.id.wv_forumfragment);
//        webView.getSettings().setJavaScriptEnabled(true); // 打开JavaScript脚本
//        webView.addJavascriptInterface(new JSObject(), "myOBj");
//        webView.loadUrl("http://www.classchat.club/LaoKe/ucp.php?mode=login");
//
//        webView.setWebViewClient(new WebViewClient() {
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                String js = "let usn = document.getElementById(\"username\");\n" +
//                        "usn.value = \"LaoKE-admin\";\n" +
//                        "let psw = document.getElementById(\"password\");\n" +
//                        "psw.value = \"mzy108431MZY!\";\n" +
//                        "let login = document.getElementsByClassName(\"button1\")[0];\n" +
//                        "login.click();\n";
//                if (i < 1) {
//                    webView.loadUrl("javascript:" + js);
//                    i++;
//                }
//            }
//        });
    }



//    class JSObject {
//        @JavascriptInterface
//        // sdk17版本以上加上注解
//        public String getData(String txt) {
//            return "12345678";
//        }
//
//
//        @JavascriptInterface
//        // sdk17版本以上加上注解
//        public void getClose() {
//            Toast.makeText(getContext(), "dododod", Toast.LENGTH_SHORT).show();
//            // finish();
//        }
//    }

}
