package com.newe.rangrang;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.newe.rangrang.adapter.HistoryAdapter;
import com.newe.rangrang.bean.HistoryBean;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private static List<HistoryBean> mHistoryList = new ArrayList<>();


    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.list_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new HistoryAdapter(mHistoryList));
        initList();
        return view;
    }


    private void initList(){
        HistoryBean h1 = new HistoryBean("2018年4月20日 15:28:49","杭州电子科技大学学源街",R.mipmap.img_road);
        HistoryBean h2 = new HistoryBean("2018年4月20日 18:45:46","杭州电子科技大学学源街",R.mipmap.img_road);
        HistoryBean h3 = new HistoryBean("2018年4月21日 06:18:49","杭州电子科技大学学源街",R.mipmap.img_road);
        HistoryBean h4 = new HistoryBean("2018年4月21日 19:23:10","杭州电子科技大学学源街",R.mipmap.img_road);
        HistoryBean h5 = new HistoryBean("2018年4月21日 13:22:12","杭州电子科技大学学源街",R.mipmap.img_road);
        HistoryBean h6 = new HistoryBean("2018年4月21日 16:21:09","杭州电子科技大学学源街",R.mipmap.img_road);
        mHistoryList.add(h6);
        mHistoryList.add(h5);
        mHistoryList.add(h4);
        mHistoryList.add(h3);
        mHistoryList.add(h2);
        mHistoryList.add(h1);

    }
}
