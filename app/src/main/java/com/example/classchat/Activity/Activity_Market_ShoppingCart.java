package com.example.classchat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.classchat.Adapter.Adapter_ShoppingCart;
import com.example.classchat.Object.Object_Commodity;
import com.example.classchat.Object.Object_Commodity_Shoppingcart;
import com.example.classchat.R;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.TouchTypeDetector;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Activity_Market_ShoppingCart extends Activity implements View.OnClickListener {

    private ImageButton ibShopcartBack;
    private TextView tvShopcartEdit;
    private RecyclerView recyclerview;
    private CheckBox checkboxAll;
    private TextView tvShopcartTotal;
    private LinearLayout ll_check_all;
    private LinearLayout ll_delete;
    private CheckBox cb_all;
    private Button btn_delete;
    private Button btnCheckOut;
    private Adapter_ShoppingCart adapter;
    private LinearLayout ll_empty_shopcart;
    private TextView tv_empty_cart_tobuy;

//    public static  List<Object_Commodity_Shoppingcart> datas = new ArrayList<>();

    /**
     * 编辑状态
     */
    private static final int ACTION_EDIT = 0;
    /**
     * 完成状态
     */
    private static final int ACTION_COMPLETE = 1;

    /**
     * Find the Views in the layout
     */
    private void findViews() {
        ibShopcartBack = findViewById(R.id.ib_shopcart_back);
        tvShopcartEdit = findViewById(R.id.tv_shoppingcart_edit);
        recyclerview = findViewById(R.id.recyclerview);
        checkboxAll = findViewById(R.id.checkbox_all);
        tvShopcartTotal = findViewById(R.id.tv_shopcart_total);
        btnCheckOut = findViewById(R.id.btn_check_out);
        ll_check_all = findViewById(R.id.ll_check_all);
        ll_delete = findViewById(R.id.ll_delete);
        cb_all = findViewById(R.id.cb_all);
        btn_delete = findViewById(R.id.btn_delete);
        ll_empty_shopcart = findViewById(R.id.ll_empty_shopcart);
        tv_empty_cart_tobuy = findViewById(R.id.tv_empty_cart_tobuy);

        ibShopcartBack.setOnClickListener(this);
        btnCheckOut.setOnClickListener(this);
        tvShopcartEdit.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        tv_empty_cart_tobuy.setClickable(true);
        tv_empty_cart_tobuy.setOnClickListener(this);
    }

    /**
     * Handle button click events
     */
    @Override
    public void onClick(View v) {
        if (v == ibShopcartBack) {
            finish();
        } else if (v == btnCheckOut) {
            Toast.makeText(Activity_Market_ShoppingCart.this, "结算", Toast.LENGTH_SHORT).show();
        } else if (v == tvShopcartEdit) {
            //设置编辑的点击事件
            int tag = (int) tvShopcartEdit.getTag();
            if (tag == ACTION_EDIT) {
                //变成完成状态
                showDelete();
            } else {
                //变成编辑状态
                hideDelete();
            }
        } else if (v == btn_delete) {
            try {
                adapter.deleteData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.showTotalPrice();
            //显示空空如也
            checkData();
            adapter.checkAll();
        } else if (v == tv_empty_cart_tobuy) {
            /**
             * 这里是跳转到商城首页的函数
             */
            Intent intent = new Intent(Activity_Market_ShoppingCart.this, MainActivity.class);
            intent.putExtra("id",1);
            startActivity(intent);
        }
    }

    private void hideDelete() {
        tvShopcartEdit.setText("编辑");
        tvShopcartEdit.setTag(ACTION_EDIT);

        adapter.checkAll_none(true);
        ll_delete.setVisibility(View.GONE);
        ll_check_all.setVisibility(View.VISIBLE);

        adapter.showTotalPrice();
    }

    private void showDelete() {
        tvShopcartEdit.setText("完成");
        tvShopcartEdit.setTag(ACTION_COMPLETE);

        adapter.checkAll_none(false);
        cb_all.setChecked(false);
        checkboxAll.setChecked(false);

        ll_delete.setVisibility(View.VISIBLE);
        ll_check_all.setVisibility(View.GONE);

        adapter.showTotalPrice();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity__market__shopping_cart);

        TouchTypeDetector.TouchTypListener touchTypListener = new TouchTypeDetector.TouchTypListener() {
            @Override
            public void onDoubleTap() {}
            @Override
            public void onLongPress() {}
            @Override
            public void onScroll(int scrollDirection) {}
            @Override
            public void onSingleTap() {}
            @Override
            public void onSwipe(int swipeDirection) {
                switch (swipeDirection) {
                    case TouchTypeDetector.SWIPE_DIR_RIGHT:
                        finish();
                        Sensey.getInstance().stopTouchTypeDetection();
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onThreeFingerSingleTap() {}
            @Override
            public void onTwoFingerSingleTap() {}
        };
        Sensey.getInstance().startTouchTypeDetection(this,touchTypListener);

        findViews();
        try {
            showData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("尝试成功", "try successfully");
        checkData();

        tvShopcartEdit.setTag(ACTION_EDIT);
        tvShopcartEdit.setText("编辑");

    }

    //-----------------------------------------
    private void checkData() {
        if (adapter != null && adapter.getItemCount() > 0) {
            tvShopcartEdit.setVisibility(View.VISIBLE);
            ll_empty_shopcart.setVisibility(View.GONE);
            ll_check_all.setVisibility(View.VISIBLE);
        } else {
            ll_empty_shopcart.setVisibility(View.VISIBLE);
            tvShopcartEdit.setVisibility(View.GONE);
            ll_check_all.setVisibility(View.GONE);
            ll_delete.setVisibility(View.GONE);
        }
    }

    /**
     * 显示数据
     */
    private void showData() throws JSONException {

        List<Object_Commodity_Shoppingcart> datas = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences("shopping_cart_cache" , MODE_MULTI_PROCESS);
        String information = sp.getString("cart_information" ,"error");
        // debug专用
        System.out.println("这里是从缓存取出来的information在showData里面：" +information);

        if(!information.equals("error")){

//            List<JSONObject> commodityList = JSON.parseObject(information , new TypeReference<List<JSONObject>>(){});
//            List<Object_Commodity_Shoppingcart> datas = new ArrayList<>();
//            for(JSONObject object : commodityList){
//                Object_Commodity_Shoppingcart obj = new Object_Commodity_Shoppingcart();
//                obj.setImageList(JSON.parseObject(object.getString("url") , new TypeReference<List<String>>(){}));
//                obj.setItemID(object.getString("itemId"));
//                obj.setItemName(object.getString("itemName"));
//                obj.setOwnerID(object.getString("ownerId"));
//                obj.setPrice(object.getDouble("price"));
//                datas.add(obj);
//                Log.e("添加成功", "add successfully");
//                System.out.println(datas.size());
//            }

            List<Object_Commodity> commodityList = JSON.parseObject(information , new TypeReference<List<Object_Commodity>>(){});

            /**
             * 把缓存里面的每一个对象都取出来
             */
            for(Object_Commodity object_commodity: commodityList){
                Object_Commodity_Shoppingcart object_commodity_shoppingcart = new Object_Commodity_Shoppingcart();

//                boolean isAdded = false ;
//
//                for(int i = 0 ; i<datas.size() ; i++){
//                    if((object_commodity.getItemID()).equals(datas.get(i).getItemID())){
//                        isAdded = true;
//                    }
//                }

                object_commodity_shoppingcart.setImageList(object_commodity.getImageList());
                object_commodity_shoppingcart.setItemID(object_commodity.getItemID());
                object_commodity_shoppingcart.setItemName(object_commodity.getItemName());
                object_commodity_shoppingcart.setOwnerID(object_commodity.getOwnerID());
                object_commodity_shoppingcart.setPrice(object_commodity.getPrice());

                datas.add(object_commodity_shoppingcart);
            }

            if (datas != null && datas.size() > 0) {
                Log.e("不为空", "not null");
                tvShopcartEdit.setVisibility(View.VISIBLE);
                ll_empty_shopcart.setVisibility(View.GONE);
                adapter = new Adapter_ShoppingCart(this, datas, tvShopcartTotal,  checkboxAll, cb_all);
                recyclerview.setLayoutManager(new LinearLayoutManager(this));
                recyclerview.setAdapter(adapter);
            } else {
                //显示空的
                tvShopcartEdit.setVisibility(View.GONE);
                ll_empty_shopcart.setVisibility(View.VISIBLE);
                ll_check_all.setVisibility(View.GONE);
                ll_delete.setVisibility(View.GONE);
            }
        }
    }

    //用于手势监听
    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        // Setup onTouchEvent for detecting type of touch gesture
        Sensey.getInstance().setupDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

}
