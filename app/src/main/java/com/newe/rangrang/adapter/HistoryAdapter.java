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
 * Created by Jaylen Hsieh on 2018/04/22.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private List<HistoryBean> mHistoryList;

    static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView photo;
        TextView time;
        TextView location;
        TextView btnDalete;

        public MyViewHolder(View view){
            super(view);
            photo = view.findViewById(R.id.img_photo);
            time = view.findViewById(R.id.tv_time);
            location = view.findViewById(R.id.tv_location);
            btnDalete = view.findViewById(R.id.tv_delete);
        }
    }

    public HistoryAdapter(List<HistoryBean> historyList){
        mHistoryList = historyList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent,false);
        final MyViewHolder holder = new MyViewHolder(view);
        holder.btnDalete.setOnClickListener(new View.OnClickListener() {
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
