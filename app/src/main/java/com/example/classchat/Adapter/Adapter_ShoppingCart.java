package com.example.classchat.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bumptech.glide.Glide;
import com.example.classchat.Activity.Activity_Market_ShoppingCart;
import com.example.classchat.Activity.MainActivity;
import com.example.classchat.Object.Object_Commodity;
import com.example.classchat.Object.Object_Commodity_Shoppingcart;
import com.example.classchat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import static android.content.Context.MODE_MULTI_PROCESS;

/**
 * 用到了商品数据提供类，这里需要修改
 */
public class Adapter_ShoppingCart extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Object_Commodity_Shoppingcart> datas;
    private TextView tvShopcartTotal;
    private CheckBox checkboxAll;
    //完成状态下的删除checkbox
    private CheckBox cb_all;

    SharedPreferences sp ;
    String jsonString ;
    List<JSONObject> list ;
    SharedPreferences.Editor editor ;

    public Adapter_ShoppingCart(Context context, final List<Object_Commodity_Shoppingcart> datas, TextView tvShopcartTotal,  CheckBox checkboxAll, CheckBox cb_all) {
        //接收
        this.mContext = context;
        this.datas = datas;
        this.tvShopcartTotal = tvShopcartTotal;
        this.checkboxAll = checkboxAll;
        this.cb_all = cb_all;
        sp = mContext.getSharedPreferences("shopping_cart_cache" , Context.MODE_PRIVATE );
        jsonString = sp.getString("cart_information" , "error");
        list = JSON.parseObject(jsonString , new TypeReference<List<JSONObject>>(){});
        editor = sp.edit();

        //首次加载数据
        showTotalPrice();
        checkboxAll.setChecked(true);
        for (int i = 0; i < datas.size(); i++) {
            datas.get(i).setIsChildSelected(true);
        }
        showTotalPrice();

        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                //根据位置找到相应的commodity对象
                Object_Commodity_Shoppingcart Commodity = datas.get(position);
                //设置取反状态
                Commodity.setIsChildSelected(!Commodity.isChildSelected());
                //刷新状态
                notifyItemChanged(position);
                //校验是否全选
                checkAll();
                //重新计算总价格
                showTotalPrice();
            }
        });

        //设置全选点击事件
        checkboxAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = getCheckboxAll().isChecked();
                checkAll_none(checked);
                showTotalPrice();
            }
        });

        //全部选中就把全选框勾上
        cb_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //得到状态
                boolean checked = getCb_all().isChecked();
                checkAll_none(checked);
                //根据状态设置全选或者非全选
                showTotalPrice();
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(View.inflate(mContext, R.layout.item_shopping_cart, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //new一个viewholder
        ViewHolder viewHolder = (ViewHolder) holder;
        //根据位置得到对应的对象并设置数据
        viewHolder.setData(datas.get(position));
    }

    @Override
    public int getItemCount() {
        //返回数据大小
        return datas.size();
    }

    //是否全选，以下checkAll函数的反面
    public void checkAll_none(boolean checked) {
        if (datas != null && datas.size() > 0) {
            for (int i = 0; i < datas.size(); i++) {
                datas.get(i).setIsChildSelected(checked);
                checkboxAll.setChecked(checked);
                notifyItemChanged(i);
            }
        } else {
            checkboxAll.setChecked(false);

        }
    }

    //删除选中的data
    public void deleteData() throws JSONException {
        if (datas.size() > 0) {

            for ( int k = datas.size() -1 ; k >= 0 ; k-- ) {

                Object_Commodity_Shoppingcart cart = datas.get(k);
                if (cart.isChildSelected()) {
                    // 如果被选中，就从缓存里移掉
                    datas.remove(k);
                    editor.clear().commit();
                    editor.putString("cart_information", JSON.toJSONString(datas)).commit();
                    //刷新数据
                    notifyItemRemoved(k);
                }
            }
        }
    }

    //校验是否全选
    public void checkAll() {
        if (datas != null && datas.size() > 0) {
            for (int i = 0; i < datas.size(); i++) {
                if (!datas.get(i).isChildSelected()) {
                    checkboxAll.setChecked(false);
                    cb_all.setChecked(false);
                    return;
                } else {
                    checkboxAll.setChecked(true);
                    cb_all.setChecked(true);
                }
            }
        }
    }


    public void showTotalPrice() {
        //设置文本
        tvShopcartTotal.setText(getTotalPrice() + "");
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private CheckBox cbGov;
        private ImageView ivGov;
        private TextView tvDescGov;
        private TextView tvPriceGov;

        ViewHolder(View itemView) {
            //转换
            super(itemView);
            cbGov = (CheckBox) itemView.findViewById(R.id.cb_gov);
            ivGov = (ImageView) itemView.findViewById(R.id.iv_gov);
            tvDescGov = (TextView) itemView.findViewById(R.id.tv_desc_gov);
            tvPriceGov = (TextView) itemView.findViewById(R.id.tv_price_gov);
            //设置item的点击事件
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        //回传
                        onItemClickListener.onItemClickListener(v, getLayoutPosition());
                    }
                }
            });
        }

        /**
         * 这个是一个设置数据的函数
         * @param Commodity
         */
        public void setData(final Object_Commodity_Shoppingcart Commodity) {

            if(Commodity != null){
                //检查是否被选上
                cbGov.setChecked(Commodity.isChildSelected());

                //设置图片
                if(Commodity.getImageList() != null){
                    Glide.with(mContext)
                            .load(Commodity.getImageList().get(0)) //获得图片
                            .into(ivGov);
                }

                //设置文本
                tvDescGov.setText(Commodity.getItemName());
                tvPriceGov.setText("LKB " + Commodity.getPrice());
            }
        }
    }

    //展示总价格
    //计算总价格
    private double getTotalPrice() {
        double total = 0;
        //条件
        if (datas != null && datas.size() > 0) {
            for (int i = 0; i < datas.size(); i++) {
                Object_Commodity_Shoppingcart commodity = datas.get(i);
                //只计算选中的
                if (commodity.isChildSelected())
                    total += commodity.getPrice();
            }
        }
        return total;
    }

    //回调点击事件的监听
    private OnItemClickListener onItemClickListener;

    interface OnItemClickListener {
        void onItemClickListener(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public CheckBox getCb_all() {
        return cb_all;
    }

    public void setCb_all(CheckBox cb_all) {
        this.cb_all = cb_all;
    }

    public CheckBox getCheckboxAll() {
        return checkboxAll;
    }

    public void setCheckboxAll(CheckBox checkboxAll) {
        this.checkboxAll = checkboxAll;
    }
}
