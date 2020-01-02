package com.vonchenchen.android_video_demos.codec;

import android.graphics.Paint;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

/**
 * Created by lidechen on 5/31/17.
 */

public class FFmpegDecodeFrame extends CodecWrapper {

    private static final String TAG = "FFmpegDecodeFrame";


    public FFmpegDecodeFrame(SurfaceView surfaceView){
        super(surfaceView);
    }
}
