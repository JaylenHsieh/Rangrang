package com.newe.rangrang.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.newe.rangrang.R;
import com.newe.rangrang.bean.HistoryBean;

import java.util.List;

/**
 * 用来适配历史记录实体类的适配器，适配器相关内容参见《第一行代码》第三章，不理解可以百度 “适配器模式” 理解理解
 *
 * @author Jaylen Hsieh
 * @date 2018/04/22.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private List<HistoryBean> mHistoryList;
//将数据转化为视图的类
    static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView photo;
        TextView time;
        TextView location;
        TextView btnDelete;

        MyViewHolder(View view){
            super(view);
            photo = view.findViewById(R.id.img_photo);
            time = view.findViewById(R.id.tv_time);
            location = view.findViewById(R.id.tv_location);
            btnDelete = view.findViewById(R.id.tv_delete);
        }
    }

    public HistoryAdapter(List<HistoryBean> historyList){
        //在构造方法中把数据源保存到类内部
        mHistoryList = historyList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent,false);
        final MyViewHolder holder = new MyViewHolder(view);
//删除按钮逻辑设置
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                mHistoryList.remove(position);
                notifyItemRemoved(position);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        HistoryBean historyBean = mHistoryList.get(position);
        holder.photo.setImageResource(historyBean.getPhoto());
        holder.time.setText(historyBean.getTime());
        holder.location.setText(historyBean.getLocation());
    }

    @Override
    public int getItemCount() {
        return mHistoryList.size();
    }
}
