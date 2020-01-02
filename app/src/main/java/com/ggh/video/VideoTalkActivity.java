package com.ggh.video;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import com.example.videocall.R;
import com.ggh.video.binder.FrameProvider;
import com.ggh.video.decode.AudioDecoder;
import com.ggh.video.device.AudioRecorder;
import com.ggh.video.device.CameraManager;
import com.ggh.video.net.udp.NettyClient;
import com.ggh.video.net.udp.NettyReceiverHandler;
import com.ggh.video.utils.MyConstants;
import com.vonchenchen.android_video_demos.codec.FFmpegDecodeFrame;

/**
 * Created by ZQZN on 2017/12/12.
 */
public class VideoTalkActivity extends Activity implements CameraManager.OnFrameCallback, View.OnClickListener {
    SurfaceView sv_local, sv_received;
    CameraManager manager;
    private FrameProvider provider;
    //private AndroidHradwareDecode mDecode; //硬遍
    private FFmpegDecodeFrame fFmpegDecodeFrame;//ffmpeg 软编
    private AudioRecorder audioRecorder = AudioRecorder.getInstance();
    private AudioDecoder audioDecoder = new AudioDecoder();
    private int delayTime = 1000;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        Log.d("www","onCreate()");
        sv_local = findViewById(R.id.sv_local);
        sv_received = findViewById(R.id.sv_received);
        findViewById(R.id.button).setOnClickListener(this);

        fFmpegDecodeFrame = new FFmpegDecodeFrame(sv_received);
        manager = new CameraManager(sv_local, this);

        provider = new FrameProvider(
                new NettyReceiverHandler.AudioDataCallback() {
                    @Override
                    public void onAudioData(byte[] data) {
                        audioDecoder.decodeStream(data, data.length);
                    }
                },
                new NettyReceiverHandler.VideoDataCallback() {
                    @Override
                    public void onVideoData(byte[] data) {
                        fFmpegDecodeFrame.decodeStream(data, data.length);
                    }
                },
                FrameProvider.ENCEDE_TYPE_X264);

        NettyClient.getIns().setHangupCallback(new NettyReceiverHandler.HangupCallback() {
            @Override
            public void hangup() {
                exit();
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                audioRecorder.startRecording();
                manager.startPreview();
            }
        }, delayTime);
    }

    //摄像头回调yuv数据
    @Override
    public void onCameraFrame(byte[] data) {
        provider.sendVideoFrame(data);//发送去编码
    }


    @Override
    public void onClick(View view) {
        exit();
    }

    private void exit() {
        manager.destroy();
        audioRecorder.stopRecording();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, delayTime);
        NettyClient.getIns().sendData("hangup".getBytes(), MyConstants.MSG_TYPE_NORMAL);
    }

    @Override
    protected void onDestroy() {
        Log.d("www","onDestroy()");
        fFmpegDecodeFrame.release();
        audioDecoder.release();
        provider.destory();
        super.onDestroy();
    }
}
