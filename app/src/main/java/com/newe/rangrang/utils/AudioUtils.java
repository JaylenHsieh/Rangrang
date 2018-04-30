package com.newe.rangrang.utils;


import android.content.Context;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
/**
 * @author Jaylen Hsieh
 * @date 2018/04/30
 */
public class AudioUtils {
    /**
     * 初始化player
     */
    private void initPlayer(Context context) {
        //1.创建一个默认的 TrackSelector
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        // 2.创建Player
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
    }
}
