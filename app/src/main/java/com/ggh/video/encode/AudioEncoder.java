package com.ggh.video.encode;

import android.util.Log;

import com.ggh.video.binder.FrameProvider;
import com.gyz.voipdemo_speex.util.Speex;


/**
 * 音频编码器
 *
 * @author lqm
 */
public class AudioEncoder {
    //单例模式构造对象
    private static AudioEncoder encoder;
    private Speex speex =Speex.getInstance();
    private byte[] encodedData;

    public static AudioEncoder getInstance() {
        if (encoder == null) {
            encoder = new AudioEncoder();
        }
        return encoder;
    }

    private AudioEncoder() {
        encodedData =new byte[Speex.getInstance().getFrameSize()];
    }

    public void encodeData(short[] data, int size) {
        int encodeSize = speex.encode(data, 0, encodedData, size);
        if (encodeSize > 0) {
            if (FrameProvider.getProvider()!=null) {
                FrameProvider.getProvider().sendAudioFrame(encodedData);
            }
        }
    }
}