package com.example.classchat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.classchat.R;

import java.util.List;

public class Adapter_GoodsDetail extends BaseAdapter {

    private Context mContext;
    private List<String>imageList;
    private LayoutInflater mLayoutInflater;

    public Adapter_GoodsDetail(Context context, List<String>imageList){
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.imageList = imageList;
    }

    @Override
    public int getCount() {
        if(imageList !=null)
            return imageList.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder{
        public ImageView pic;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.adapter_good_detail_imgs, null);
            holder = new ViewHolder();
            holder.pic = convertView.findViewById(R.id.iv_adapter_good_detail_img);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        Glide.with(mContext).load(imageList.get(position)).override(720,480).into(holder.pic);
        return convertView;
    }
}
