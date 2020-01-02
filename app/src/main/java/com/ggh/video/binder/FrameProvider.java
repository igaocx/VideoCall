package com.ggh.video.binder;

import android.util.Log;

import com.ggh.video.device.CameraConfig;
import com.ggh.video.encode.AndroidHradwareEncode;
import com.ggh.video.net.udp.NettyClient;
import com.ggh.video.net.udp.NettyReceiverHandler;
import com.ggh.video.utils.MyConstants;
import com.ggh.video.utils.MyUtils;
import example.sszpf.x264.x264sdk;

/**
 * Created by ZQZN on 2018/2/1.
 */

public class FrameProvider {
    public static final String ENCEDE_TYPE_ANDROIDHARDWARE = "ENCEDE_TYPE_ANDROIDHARDWARE";
    public static final String ENCEDE_TYPE_FFMEPG = "ENCEDE_TYPE_FFMEPG";
    public static final String ENCEDE_TYPE_X264 = "ENCEDE_TYPE_X264";

    private AndroidHradwareEncode mEncode;
    private x264sdk x264Sdk;
    private boolean isfinish = false;
    private long time;
    public final int timespan = CameraConfig.bitrate / CameraConfig.fps;
    private static FrameProvider provider;
    public String currenType;

    public static FrameProvider getProvider() {
        if (provider != null) {
            return provider;
        }
        return null;
    }

    /**
     * 外界初始化
     * @param type
     */
    public FrameProvider(NettyReceiverHandler.AudioDataCallback audioDataCallback,
                                NettyReceiverHandler.VideoDataCallback videoDataCallback, String type) {
        NettyClient.getIns().setAudioDataCallback(audioDataCallback);
        NettyClient.getIns().setVideoDataCallback(videoDataCallback);
        if (type.equals(ENCEDE_TYPE_ANDROIDHARDWARE)) {//Android本身的硬编码
            currenType = ENCEDE_TYPE_ANDROIDHARDWARE;
            mEncode = new AndroidHradwareEncode(CameraConfig.WIDTH, CameraConfig.HEIGHT, CameraConfig.bitrate, CameraConfig.fps, new AndroidHradwareEncode.IEncoderListener() {
                @Override
                public void onH264(byte[] data) {
                    //发送编码后的数据
                    NettyClient.getIns().sendData(data, MyConstants.MSG_TYPE_VIDEO);
                }
            });
            isfinish = true;
        } else if (type.equals(ENCEDE_TYPE_X264)) {//x264编码
            currenType = ENCEDE_TYPE_X264;
            //因为视频经过90度的旋转，所以解码器的高宽是对调的
            x264Sdk = new x264sdk(CameraConfig.HEIGHT,CameraConfig.WIDTH,CameraConfig.fps,CameraConfig.bitrate,
                    new x264sdk.listener() {
                @Override
                public void h264data(byte[] buffer, int length) {
                    //发送编码后的数据
                    NettyClient.getIns().sendData(buffer, MyConstants.MSG_TYPE_VIDEO);
                }
            });
            isfinish = true;
        }
        provider = this;
    }

    /**
     * 接收编码的数据
     *
     * @param data
     */
    public void sendVideoFrame(byte[] data) {
        if (isfinish) {
            //发送数据
            if (currenType.equals(ENCEDE_TYPE_ANDROIDHARDWARE)){
                mEncode.encoderYUV420(data);
            } else if (currenType.equals(ENCEDE_TYPE_X264)){
                time  += timespan;
                /*byte[] yuv420sp =MyUtils.rotateYUVDegree90(data,CameraConfig.WIDTH,CameraConfig.HEIGHT);
                byte[] yuv420 = new byte[CameraConfig.WIDTH*CameraConfig.HEIGHT*3/2];
                MyUtils.YUV420SP2YUV420(yuv420sp,yuv420,CameraConfig.WIDTH,CameraConfig.HEIGHT);*/
                x264Sdk.PushOriStream(data, data.length,time);
            }
        }
    }

    /**
     * 接收编码的数据
     *
     * @param data
     */
    public void sendAudioFrame(byte[] data) {
        if (isfinish) {
            //发送编码后的数据
            NettyClient.getIns().sendData(data, MyConstants.MSG_TYPE_AUDIO);
        }
    }

    public interface OnEncodeFrameCallback {
        void onEncodeData(byte[] data);
    }

    public void destory(){
        x264Sdk.release();
    }
}
