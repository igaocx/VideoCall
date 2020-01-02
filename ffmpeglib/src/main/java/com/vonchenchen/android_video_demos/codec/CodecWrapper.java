package com.vonchenchen.android_video_demos.codec;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by lidechen on 5/27/17.
 */

public abstract class CodecWrapper {

    static {
        System.loadLibrary("codec");
    }

    private Surface mSurface;

    private long mDecoderHandle;

    public CodecWrapper(SurfaceView surfaceView){
        SurfaceHolder mHoder = surfaceView.getHolder();
        mSurface = mHoder.getSurface();
        mHoder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        init();
    }

    public void init(){
        mDecoderHandle = get_codec();
    }

    public void decodeStream(byte[] data, int length){
        decode_stream(data, length, mDecoderHandle, mSurface);
    }

    public void release(){
        release_codec(mDecoderHandle);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            release();
        } finally {
            try {
                super.finalize();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    //this method was called by jni when one frame was decode ok
    /*public void onFrameDecode(byte[] data, int width, int height){
        getOneFrame(data, width, height);
    }

    abstract void getOneFrame(byte[] data, int width, int height);*/

    public native long get_codec();
    public native void decode_stream(byte[] data, int length, long decoder, Surface surface);
    public native void release_codec(long decoder);
}
