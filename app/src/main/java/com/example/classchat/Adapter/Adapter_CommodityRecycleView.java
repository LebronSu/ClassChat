package com.example.classchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.example.classchat.Activity.Activity_Market_GoodsDetail;
import com.example.classchat.Object.Object_Commodity;
import com.example.classchat.R;
import com.hch.thumbsuplib.ThumbsUpCountView;

import java.util.List;

public class Adapter_CommodityRecycleView extends RecyclerView.Adapter<Adapter_CommodityRecycleView.ViewHolder> {

    private Context mContext;
    private List<Object_Commodity> itemList;

    public Adapter_CommodityRecycleView(Context context, List<Object_Commodity> itemList ) {
        this.itemList = itemList;
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemName, itemIntroduction, itemPrice;
        public ImageView itemPic;
//        ThumbsUpCountView thumbs;

        public ViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.tv_market_item_name);
            itemIntroduction = itemView.findViewById(R.id.tv_market_item_intro);
            itemPrice = itemView.findViewById(R.id.tv_market_item_price);
            itemPic = itemView.findViewById(R.id.iv_market_item_pic);
//            thumbs =  itemView.findViewById(R.id.market_item_thumb);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.staggered_recycler_view_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Object_Commodity item = itemList.get(position);
        holder.itemName.setText(item.getItemName());
        holder.itemIntroduction.setText(item.getBriefIntroduction());
        holder.itemPrice.setText(Double.toString(item.getPrice()));

        Glide.with(mContext).load(item.getImageList().get(0)).override(720,480).into(holder.itemPic);
//        //TODO 点赞设置

//        //TODO 获取用户信息
//        holder.thumbs.initData(item.getThumbsUpState("17690710589"),item.getThumbsUpCount());
//        holder.thumbs.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                holder.thumbs.priseChange();
//                if(item.getThumbsUpState("17690710589"))
//                    item.removeFromThumbedList("17690710589");
//                else
//                 item.addToThumbedList("17690710589");
//            }
//        });

        holder.itemPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, Activity_Market_GoodsDetail.class);

                intent.putExtra("item", JSON.toJSONString(item));
                System.out.println("这里是价格在rv适配器里："+ item.getPrice());

                intent.putExtra("itemID",item.getItemID());
                System.out.println("这里是itemID在rv适配器里：" + item.getItemID());
                intent.putExtra("itemName",item.getItemName());
                intent.putExtra("itemPrice",item.getPrice());
                intent.putExtra("itemDetailInfo",item.getDetailIntroduction());
                intent.putExtra("itemPic1",item.getImageList().get(0));
                intent.putExtra("itemPic2",item.getImageList().get(1));
                intent.putExtra("itemPic3",item.getImageList().get(2));
                intent.putExtra("ThumbsCount",item.getThumbsUpCount());

                //TODO 获取user id
//                intent.putExtra("isThumbed",item.getThumbsUpState(user id));

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(itemList !=null)
          return itemList.size();
        else
            return 0;
    }


}

