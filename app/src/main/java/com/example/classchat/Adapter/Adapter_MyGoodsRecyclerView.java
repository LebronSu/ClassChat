package com.example.classchat.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.example.classchat.Object.Object_Commodity;
import com.example.classchat.R;
import com.example.classchat.Util.Util_NetUtil;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Adapter_MyGoodsRecyclerView extends RecyclerView.Adapter<Adapter_MyGoodsRecyclerView.ViewHolder> {

    private static final String TAG = "Adapter_MyGoodsRecycler";

    private Context mContext;
    private List<Object_Commodity> itemList;


    public Adapter_MyGoodsRecyclerView(Context context, List<Object_Commodity> itemList ) {
        this.itemList = itemList;
        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemName;
        private ImageView itemPic;
        private EditText itemPrice;
        private Button changeConfirm, edit, delete;


        public ViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.tv_market_my_goods_name);
            itemPic = itemView.findViewById(R.id.iv_market_my_goods_pic);
            itemPrice = itemView.findViewById(R.id.et_market_my_goods_price);
            edit = itemView.findViewById(R.id.b_my_goods_edit);
            delete = itemView.findViewById(R.id.b_my_goods_delete);
            changeConfirm = itemView.findViewById(R.id.b_my_goods_changePrice);
            itemPrice.setEnabled(false);
        }
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_goods_recycler_view_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Object_Commodity item = itemList.get(position);
        holder.itemName.setText(item.getItemName());
        holder.itemPrice.setText(Double.toString(item.getPrice()));
        Glide.with(mContext).load(item.getImageList().get(0)).override(720,480).into(holder.itemPic);

        //点击图片取消编辑模式
        holder.itemPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.changeConfirm.setVisibility(View.GONE);
                holder.delete.setVisibility(View.GONE);
                holder.edit.setVisibility(View.VISIBLE);
                holder.itemPrice.setEnabled(false);
            }
        });

        //TODO 价格修改有问题
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击编辑后，价格可改，修改、删除按钮出现，编辑按钮隐藏
                holder.itemPrice.setEnabled(true);
                holder.changeConfirm.setVisibility(View.VISIBLE);
                holder.delete.setVisibility(View.VISIBLE);
                holder.edit.setVisibility(View.GONE);

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //为下一个item取消编辑模式
                        holder.changeConfirm.setVisibility(View.GONE);
                        holder.delete.setVisibility(View.GONE);
                        holder.edit.setVisibility(View.VISIBLE);
                        holder.itemPrice.setEnabled(false);
                        // 构建requestbody
                        RequestBody requestBody = new FormBody.Builder()
                                .add("itemID", item.getItemID())
                                .add("ownerID", item.getOwnerID())
                                .build();

                        // 发送网络请求，联络信息
                        Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/deleteitem", requestBody, new Callback() {
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                // 得到服务器返回的具体内容
                                boolean responseData = Boolean.parseBoolean(response.body().string());
                                if (responseData) {
                                    Log.d(TAG, "删除成功");
                                    //刷新
                                    itemList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyDataSetChanged();
                                } else {
                                    Log.d(TAG, "删除失败");
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                // 在这里对异常情况进行处理
                                Log.d(TAG, "onFailure: 删除商品失败");
                            }
                        });
                    }
                });

                holder.changeConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.changeConfirm.setVisibility(View.GONE);
                        holder.delete.setVisibility(View.GONE);
                        holder.edit.setVisibility(View.VISIBLE);
                        holder.itemPrice.setEnabled(false);
                        Object_Commodity object_commodity = new Object_Commodity(item.getItemID(), item.getItemName(), item.getImageList(), item.getOwnerID(), Double.valueOf(holder.itemPrice.getText().toString()), item.getBriefIntroduction(), item.getDetailIntroduction(), item.getThumbedList());
                        String jsonstring = JSON.toJSONString(object_commodity);
                        RequestBody requestBody = new FormBody.Builder()
                                .add("itemID", item.getItemID())
                                .add("information", jsonstring)
                                .build();

                        // 发送网络请求，联络信息
                        Util_NetUtil.sendOKHTTPRequest("http://106.12.105.160:8081/updateitem", requestBody, new Callback() {
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                // 得到服务器返回的具体内容
                                boolean responseData = Boolean.parseBoolean(response.body().string());
                                if (responseData) {
                                    Log.d(TAG, "修改商品成功");
                                }
                                else {
                                    Log.d(TAG, "修改商品失败");
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                // 在这里对异常情况进行处理
                                Log.d(TAG, "onFailure: 修改商品价格失败");
                            }
                        });

                        notifyItemChanged(position);
                    }
                });
            }
        });

        //TODO 获取商品图片
    }

    @Override
    public int getItemCount() {
        if(itemList !=null)
            return itemList.size();
        else
            return 0;
    }

}