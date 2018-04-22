package com.newe.rangrang;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScreenFragment extends Fragment {

    private FloatingActionButton mFab;

    /**
     * 默认关闭提示音
     */
    private boolean isTrumpetOn = false;


    public ScreenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_screen, container, false);
        mFab = view.findViewById(R.id.fab_sound);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTrumpetOn){
                    // 点击后，如果扬声器是打开的，关闭提示音，图标显示为喇叭关闭，提示用户已关闭提示音
                    mFab.setImageDrawable(getResources().getDrawable(R.mipmap.ic_volume_off_white_24dp));
                    Toast.makeText(getContext(), "已关闭提示音", Toast.LENGTH_SHORT).show();
                    isTrumpetOn  = false;
                }else{
                    mFab.setImageDrawable(getResources().getDrawable(R.mipmap.ic_volume_up_white_24dp));
                    Toast.makeText(getContext(), "已打开提示音", Toast.LENGTH_SHORT).show();
                    isTrumpetOn = true;
                }
            }
        });
    }
}
