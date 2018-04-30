package com.newe.rangrang.utils;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.Util;
import com.newe.rangrang.R;

import java.io.IOException;

import static android.support.constraint.Constraints.TAG;

/**
 * @author Jaylen Hsieh
 * @date 2018/04/30
 */
public class AudioUtils {
    /**
     * 初始化 player
     */
    public static void initPlayer(Context context) {
        //1.创建一个默认的 TrackSelector
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        // 2.创建Player
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "Multimedia"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource = new ExtractorMediaSource(
                Uri.parse("file:///android_raw/crossing_road.mp3"),
                dataSourceFactory, extractorsFactory, new Handler(),
                new ExtractorMediaSource.EventListener() {
                    @Override
                    public void onLoadError(IOException error) {
                        Log.e(TAG, "onLoadError: " + error.getMessage());
                    }
                });
        player.prepare(videoSource);
    }

//    private void play(Context context){
//        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory selectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector = new DefaultTrackSelector(selectionFactory);
//        SimpleExoPlayer currentPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
//        try {
//            DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(context.resId));
//            RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(context);
//            rawResourceDataSource.open(dataSpec);
//            DataSource.Factory factory = () -> rawResourceDataSource;
//
//            ExtractorMediaSource mediaSource = new ExtractorMediaSource(rawResourceDataSource.getUri(),
//                    factory, new DefaultExtractorsFactory(), null, null);
//            LoopingMediaSource loopingMediaSource = new LoopingMediaSource(mediaSource);
//            currentPlayer.prepare(loopingMediaSource);
//            currentPlayer.addListener(this);
//            currentPlayer.setPlayWhenReady(true);
//        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
//            e.printStackTrace();
//        }
//    }
}
