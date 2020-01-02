package com.ggh.video.device;


import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;
import com.ggh.video.encode.AudioEncoder;
import com.gyz.voipdemo_speex.util.Speex;

public class AudioRecorder implements Runnable {

    private static AudioRecorder audioRecorder;
    //是否正在录制
    private boolean isRecording = false;
    //音频录制对象
    private AudioRecord audioRecord;

    private int audioBufSize;
    //回声消除
    private AcousticEchoCanceler canceler;

    private AudioEncoder encoder = AudioEncoder.getInstance();

    private int size;

    private short[] samples;

    private AudioRecorder(){
        //计算缓存大小
        audioBufSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
                AudioConfig.PLAYER_CHANNEL_CONFIG2, AudioConfig.AUDIO_FORMAT);
        size = Speex.getInstance().getFrameSize();
        samples = new short[size];
    }

    public static AudioRecorder getInstance(){
        if (audioRecorder == null) {
            audioRecorder = new AudioRecorder();
        }
        return audioRecorder;
    }
    public void startRecording() {
        Log.d("www", "开启录音");
        new Thread(this).start();
    }

    private boolean initAEC(int audioSession) {
        if (canceler != null) {
            return false;
        }
        if (!AcousticEchoCanceler.isAvailable()){
            return false;
        }
        canceler = AcousticEchoCanceler.create(audioSession);
        canceler.setEnabled(true);
        return canceler.getEnabled();
    }


    public void stopRecording() {
        this.isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public void run() {
        //实例化录制对象
        if (null == audioRecord && audioBufSize != AudioRecord.ERROR_BAD_VALUE) {
            audioRecord = new AudioRecord(AudioConfig.AUDIO_RESOURCE,
                    AudioConfig.SAMPLERATE,
                    AudioConfig.PLAYER_CHANNEL_CONFIG2,
                    AudioConfig.AUDIO_FORMAT, audioBufSize);

            //消回音处理
            initAEC(audioRecord.getAudioSessionId());
        }


        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e("www", "audioRecord UNINITIALIZED");
            return;
        }

        this.isRecording = true;

        audioRecord.startRecording();

        while (isRecording) {
            int bufferRead = audioRecord.read(samples, 0, size);
            if (bufferRead > 0) {
                encoder.encodeData(samples,bufferRead);
            }
        }
        audioRecord.stop();
    }
}
