package example.sszpf.x264;

import java.nio.ByteBuffer;

public class x264sdk {
	
	public interface listener
	{
		void h264data(byte[] buffer, int length);
	}
	
	private listener _listener;
	
	public x264sdk(int width, int height, int fps, int bite,listener l){
		initX264Encode(width,height,fps,bite);
		_listener = l;
	}
	
	static {
		System.loadLibrary("x264encoder");
	}
	
	private ByteBuffer mVideobuffer = ByteBuffer.allocateDirect(1024*1024*10);
	
	
	public void PushOriStream(byte[] buffer, int length, long time)
	{
		mVideobuffer.rewind();
		mVideobuffer.put(buffer);
		encoderH264(length, time);
	}
	
	public native void initX264Encode(int width, int height, int fps, int bite);

	public native void encoderH264(int length, long time);

	public native void CloseX264Encode();
	
	private void H264DataCallBackFunc(byte[] buffer, int length){
		_listener.h264data(buffer, length);
	}

	public void release(){
		mVideobuffer.clear();
		CloseX264Encode();

	}
}
