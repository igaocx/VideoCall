package com.ggh.video.net;

import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;
import com.ggh.video.device.AudioConfig;

public class AudioPlayer  {
    String LOG = "www ";

    private AudioTrack audioTrack;

    public AudioPlayer() {
        initAudioTrack();
    }

    public void playData(short[] rawData, int size) {
        audioTrack.write(rawData, 0, size);
    }

    /*
     * init Player parameters
     */
    private boolean initAudioTrack() {
        int bufferSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
                AudioConfig.PLAYER_CHANNEL_CONFIG2,
                AudioConfig.AUDIO_FORMAT);
        if (bufferSize < 0) {
            Log.e(LOG, LOG + "initialize error!");
            return false;
        }
        Log.i(LOG, "Player初始化的 buffersize大小" + bufferSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                AudioConfig.SAMPLERATE, AudioConfig.PLAYER_CHANNEL_CONFIG2,
                AudioConfig.AUDIO_FORMAT, bufferSize, AudioTrack.MODE_STREAM);
        // set volume:设置播放音量
        audioTrack.setStereoVolume(1.0f, 1.0f);
        audioTrack.play();
        return true;
    }


    public void release(){
        if (this.audioTrack != null) {
            if (this.audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                this.audioTrack.stop();
                this.audioTrack.release();
            }
        }
    }
}
