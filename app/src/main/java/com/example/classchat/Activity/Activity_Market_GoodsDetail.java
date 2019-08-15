package com.example.classchat.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bumptech.glide.Glide;
import com.example.classchat.Adapter.Adapter_GoodsDetail;
import com.example.classchat.Adapter.Adapter_ShoppingCart;
import com.example.classchat.Object.Object_Commodity;
import com.example.classchat.Object.Object_Commodity_Shoppingcart;
import com.example.classchat.R;
import com.example.classchat.Util.Util_ScreenShot;

import com.example.classchat.Util.Util_ToastUtils;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.TouchTypeDetector;
import com.hankkin.library.GradationScrollView;
import com.hankkin.library.MyImageLoader;
import com.hankkin.library.NoScrollListView;
import com.hankkin.library.ScrollViewContainer;
import com.hankkin.library.StatusBarUtil;
import com.hch.thumbsuplib.ThumbsUpCountView;
import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Activity_Market_GoodsDetail extends AppCompatActivity implements GradationScrollView.ScrollViewListener {

    private RelativeLayout llTitle;
    private LinearLayout llOffset;
    private ScrollViewContainer container;
    private  GradationScrollView scrollView;
    private String itemID, itemName, itemDetailInfo, itemPic1, itemPic2, itemPic3,ownerID;
    private Double itemPrice;
    private Boolean isThumbed;
    private int ThumbsCount;
    private ImageView frontImage;//顶头第一张图
    NoScrollListView nlvImgs;//图片详情

    private TextView tvGoodTitle, itemname, itemprice, iteminfo, buy, addToShoppingCart;
    private ImageView back, shoppingCart, share;
    private ThumbsUpCountView thumbs;
    private Object_Commodity item;
    private boolean isAdded ;
    private int height;
    private int width;

    protected final static int RECEIVE_SUCCESS = 1;
    protected final static int PURCHASE_SUCCESS = 2;
    protected final static int GET_BLOCK = 3;
    protected final static int SURE_TO_BUY = 4;
    protected final static int CONCEL_TO_BUY = 5;

    private String shareImage;

    //handler处理反应回来的信息
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case RECEIVE_SUCCESS:
                    setView();
                    break;
                case PURCHASE_SUCCESS:
                    setView();
                    break;
                case GET_BLOCK:
                    canpurchase();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__market__goods_detail);

        //手势返回
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

        //得到上一个Activity传入得到的商品信息
        Intent intent = getIntent();

        item = JSON.parseObject(intent.getStringExtra("item"), Object_Commodity.class);
        ownerID = item.getOwnerID();
        itemID = item.getItemID();
        System.out.println("这里是itemID在商品详情里：" +itemID);
        itemName = item.getItemName();
        itemPrice = item.getPrice();
        itemDetailInfo = item.getDetailIntroduction();
        itemPic1 = item.getImageList().get(0);
        itemPic2 = item.getImageList().get(1);
        itemPic3 = item.getImageList().get(2);
        ThumbsCount = item.getThumbsUpCount();

//        ownerID = intent.getStringExtra("ownerID");
//        System.out.println(ownerID); //null
//        itemID = intent.getStringExtra("itemID");
//        itemName = intent.getStringExtra("itemName");
//        itemPrice = intent.getDoubleExtra("itemPrice",0);
//        itemDetailInfo = intent.getStringExtra("itemDetailInfo");
//        itemPic1 = intent.getStringExtra("itemPic1");
//        itemPic2 = intent.getStringExtra("itemPic2");
//        itemPic3 = intent.getStringExtra("itemPic3");

        //ThumbsCount = Integer.parseInt(intent.getStringExtra("ThumbsCount"));
        //TODO
        //isThumbed = intent.getBooleanExtra("isThumbed");

        //初始化控件

        init();

        /**
         * 判断加入购物车按钮的状态
         */
        SharedPreferences sp = getSharedPreferences("shopping_cart_cache" , MODE_MULTI_PROCESS);
        String jsonString = sp.getString("cart_information","error");
        SharedPreferences.Editor editor = sp.edit();
        List<Object_Commodity_Shoppingcart> datas = new ArrayList<>();
        if(!jsonString.equals("error")) {
            List<Object_Commodity> commodityList = JSON.parseObject(jsonString, new TypeReference<List<Object_Commodity>>() {
            });
            // 把缓存里的对象取出
            for (Object_Commodity object_commodity : commodityList) {
                Object_Commodity_Shoppingcart object_commodity_shoppingcart = new Object_Commodity_Shoppingcart();
                object_commodity_shoppingcart.setImageList(object_commodity.getImageList());
                object_commodity_shoppingcart.setItemID(object_commodity.getItemID());
                object_commodity_shoppingcart.setItemName(object_commodity.getItemName());
                object_commodity_shoppingcart.setOwnerID(object_commodity.getOwnerID());
                object_commodity_shoppingcart.setPrice(object_commodity.getPrice());
                datas.add(object_commodity_shoppingcart);
            }

            /**
             * 判断是否已添加
             */
            isAdded = false;
            if (datas != null) {
                for (int i = 0; i < datas.size(); i++) {
                    if ((item.getItemID()).equals(datas.get(i).getItemID())) {
                        isAdded = true;
                    }
                }
            }

            if (isAdded == true) {
                addToShoppingCart.setText("移出购物车");
            }else{
                addToShoppingCart.setText("添加购物车");
            }
        }

        //透明状态栏
        StatusBarUtil.setTranslucentForImageView(this,llOffset);
        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) llOffset.getLayoutParams();
        params1.setMargins(0,-StatusBarUtil.getStatusBarHeight(this)/4,0,0);
        llOffset.setLayoutParams(params1);

        container = new ScrollViewContainer(getApplicationContext());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage = Util_ScreenShot.shoot(Activity_Market_GoodsDetail.this);
                Intent intent  = new Intent(Intent.ACTION_SEND);
                File file = new File(shareImage);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                intent.setType("image/jpeg");
                Intent chooser = Intent.createChooser(intent, "分享商品截图");
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivity(chooser);
                }
            }
        });

        shoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Activity_Market_ShoppingCart.class);
                startActivity(intent);
            }
        });


        addToShoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 将商品加入购物车
                 */
                SharedPreferences sp = getSharedPreferences("shopping_cart_cache" , MODE_MULTI_PROCESS);
                String jsonString = sp.getString("cart_information","error");
                SharedPreferences.Editor editor = sp.edit();
                System.out.println("这里是jsonstring： "+jsonString);

                if(!jsonString.equals("error") && !jsonString.equals("[{}]")){
                    List<Object_Commodity> commodityList = JSON.parseObject(jsonString , new TypeReference<List<Object_Commodity>>(){});

                    if(isAdded == true) {
                        for(int k = 0 ; k < commodityList.size() ; k++){
                            if(item.getItemID().equals(commodityList.get(k).getItemID())){
                                commodityList.remove(k);
                                System.out.println(k);
                                break;
                            }
                        }
                        editor.clear().commit();
                        editor.putString("cart_information", JSON.toJSONString(commodityList)).commit();
                        Util_ToastUtils.showToast(getApplicationContext(),"已移出购物车");
                        isAdded = false;
                        addToShoppingCart.setText("添加购物车");
                    }else {
                        commodityList.add(item);
                        editor.clear().commit();
                        editor.putString("cart_information", JSON.toJSONString(commodityList)).commit();
                        Util_ToastUtils.showToast(getApplicationContext(),"已加入购物车");
                        isAdded = true;
                        addToShoppingCart.setText("移出购物车");
                    }
                }else{
                    editor.clear().commit();
                    List<Object_Commodity> list = new ArrayList<>();
                    list.add(item);
                    editor.putString("cart_information",JSON.toJSONString(list)).commit();
                    Util_ToastUtils.showToast(getApplicationContext(),"已加入购物车");
                    isAdded = true;
                    addToShoppingCart.setText("移出购物车");
                }

            }
        });

        //从网络上获取物品的信息
        getINFOfromweb();

        initListeners();

        //TODO 点击购买按钮，出现一个对话框，调用网络线程，去区块链数据库获得完整的区块链，本地用map存储，

        //TODO  获得区块链之后，调用BLOCKCHAIN.getALLUTXO 判断能否支付

        //TODO 如果能够支付，调用Blockchain.createnewTRANSACTION 生成一笔交易

        //TODO 把这笔交易放入违背打包的交易的数据库，把对应商品删除

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //现在去获得用户的余额
//                blockMap = getBlockchainUtil.getBlockchain(ItemInformationActivity.this);
                canpurchase();

            }
        });
    }

    private void init(){
        scrollView = findViewById(R.id.scrollview);
        nlvImgs = findViewById(R.id.nlv_good_detial_imgs);
        llTitle = findViewById(R.id.ll_good_detail);
        tvGoodTitle = findViewById(R.id.tv_good_detail_title_good);
        llOffset = findViewById(R.id.ll_offset);
        frontImage = findViewById(R.id.iv_good_detail_front_img);
        container = findViewById(R.id.sv_container);
        back = findViewById(R.id.iv_goods_detail_back);
        shoppingCart = findViewById(R.id.iv_good_detail_shopping_cart);
        share = findViewById(R.id.iv_good_detail_share);
        addToShoppingCart = findViewById(R.id.tv_good_detail_add_shop_cart);
        buy = findViewById(R.id.tv_good_detail_buy);
        itemname = findViewById(R.id.tv_market_goods_detail_item_name);
        itemprice = findViewById(R.id.tv_market_goods_detail_item_price);
        thumbs = findViewById(R.id.market_goods_detail_item_thumb);
        iteminfo = findViewById(R.id.tv_market_goods_detail_item_detail_info);
        //TODO frontImage载入url为itemPic1的图片
        if(item.getImageList() != null){
            Glide.with(getApplicationContext())
                    .load(item.getImageList().get(0)) //获得图片
                    .into(frontImage);
        }
    }

    private void setView(){
        itemname.setText(itemName);
        itemprice.setText(String.valueOf(itemPrice));
        iteminfo.setText(itemDetailInfo);
//      thumbs.initData(isThumbed,ThumbsCount);
        thumbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thumbs.priseChange();
                //TODO 点赞设置
//                if(isThumbed)
//                    item.removeFromThumbedList("17690710589");
//                else
//                    item.addToThumbedList("17690710589");
            }
        });

        //TODO 图片高度有待优化!!不能很好的完全显示

        nlvImgs.setAdapter(new Adapter_GoodsDetail(getApplicationContext(), item.getImageList()));
    }

    private void initListeners() {

        ViewTreeObserver vto = frontImage.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                llTitle.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);
                height = frontImage.getHeight();

                scrollView.setScrollViewListener(Activity_Market_GoodsDetail.this);
            }
        });
    }

    /**
     * 判断能够是否购买的函数
     */
    private void canpurchase() {
////        result = Blockchain.getAllUTXO(blockMap,ownerID);
////        Double nowdebit = result.getDebit();
//        if(nowdebit >= item.getItemprice()){
////            AlertDialog.Builder builder = new AlertDialog.Builder(ItemInformationActivity.this);
////            builder.setTitle("确认购买");
////            builder.setMessage("您当前的余额有：" + nowdebit +"LKB, 购买后还剩：" + String.valueOf(nowdebit-item.getItemprice()) + "LKB, 确认购买？" );
////            String[] items = { "确认", "取消" };
////            builder.setItems(items, new DialogInterface.OnClickListener() {
////                @Override
////                public void onClick(DialogInterface dialog, int which) {
////                    switch (which){
////                        case SURE_TO_BUY:
////                            try {
////                                purchase();
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            }
////                            break;
////                        case CONCEL_TO_BUY:
////                            break;
////                    }
////                }
////            });
////            builder.create().show();
//            AlertDialog.Builder builder = new AlertDialog.Builder(ItemInformationActivity.this);
//            builder.setTitle("余额不足，购买失败");
//            builder.setNegativeButton("确定",null);
//            builder.setMessage("您当前的余额仅有：" + nowdebit + "LKB, 不能购买");
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        }else{
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Market_GoodsDetail.this);
        builder.setTitle("余额不足，购买失败");
        builder.setNegativeButton("确定", null);
        builder.setMessage("您当前的余额仅有：" + 0 + "LKB, 不能购买");
        AlertDialog dialog = builder.create();
        dialog.show();
//        }
    }

    /**
     * 购买函数
     */
    private void purchase() throws IOException {
//        Transaction transaction = Blockchain.createNewTransaction(userID,item.getOwnerid(),item.getItemprice(),blockMap,result,item.getItemname());
//        //打开网络线程
//        new Thread(new Runnable() {
//            private final OkHttpClient client = new OkHttpClient();
//            @Override
//            public void run() {
//                try {
//                    byte[] bytes = SerializeUtils.serialize(transaction);
//                    Request request = new Request.Builder().addHeader("Connection" , "close").url("http://203.195.184.213:8081/uploadtransaction?id=" + ownerID + item.getOwnerid() + item.getItemname() + "&ref=" + Arrays.toString(bytes)).build();
//                    client.newCall(request).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//
//                        }
//
//                        @Override
//                        public void onResponse(Call call, Response response) throws IOException {
//
//                        }
//                    });
//                    request = new Request.Builder().addHeader("Connection" , "close").url("http://203.195.184.213:8081/deleteitem?id=" + item.getItemid()).build();
//                    client.newCall(request).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//
//                        }
//
//                        @Override
//                        public void onResponse(Call call, Response response) throws IOException {
//                            Message message = new Message();
//                            message.what = PURCHASE_SUCCESS;
//                            handler.sendMessage(message);
//                        }
//                    });
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    /**
     * 从网络上获得显示的基本信息
     */
    private void getINFOfromweb(){
        //HTTP client 的申请
//        final OkHttpClient httpclient = new OkHttpClient();
//        final Request request = new Request.Builder().url("http://203.195.184.213:8081/getoneitem?id=" + itemID).build();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    //获取商品的其他信息
//                    httpclient.newCall(request).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//                            //TODO
//                        }
//                        @Override
//                        public void onResponse(Call call, Response response) throws IOException {
//                            String responsedata = response.body().string();
//                            try {
//                                JSONArray array= new JSONArray(responsedata);
//                                for(int i = 0 ; i < array.length() ; i++){
//                                    JSONObject obj = array.getJSONObject(i);
//                                    item = (TransmitItems) SerializeUtils.deserialize(SerializeUtils.splitArray(obj.getString("serializz")));
//                                }
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
        Message message = new Message();
        message.what = RECEIVE_SUCCESS;
        handler.sendMessage(message);
//                        }
//                    });
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    @Override
    public void onScrollChanged(GradationScrollView scrollView, int x, int y, int oldx, int oldy) {
        if (y <= 0) {   //设置标题的背景颜色
            llTitle.setBackgroundColor(Color.argb((int) 0, 255,255,255));
        } else if (y > 0 && y <= height) { //滑动距离小于banner图的高度时，设置背景和字体颜色颜色透明度渐变
            float scale = (float) y / height;
            float alpha = (255 * scale);
            tvGoodTitle.setTextColor(Color.argb((int) alpha, 1,24,28));
            llTitle.setBackgroundColor(Color.argb((int) alpha, 255,255,255));
        } else {    //滑动到banner下面设置普通颜色
            llTitle.setBackgroundColor(Color.argb((int) 255, 255,255,255));
        }
    }


    //用于手势监听
    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        // Setup onTouchEvent for detecting type of touch gesture
        Sensey.getInstance().setupDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
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



