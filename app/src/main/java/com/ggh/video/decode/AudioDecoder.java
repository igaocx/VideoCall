package com.ggh.video.decode;

import com.ggh.video.net.AudioPlayer;
import com.gyz.voipdemo_speex.util.Speex;

public class AudioDecoder {
    private Speex speex = Speex.getInstance();
    private AudioPlayer player = new AudioPlayer();
    private short[] decodedData;

    public AudioDecoder(){
        decodedData = new short[speex.getFrameSize()];
    }

    public void decodeStream(byte[] data, int size) {
        int decodeSize = speex.decode(data, decodedData, size);
        if (decodeSize > 0) {
            player.playData(decodedData, decodeSize);
        }
    }

    public void release(){
        player.release();
    }
}