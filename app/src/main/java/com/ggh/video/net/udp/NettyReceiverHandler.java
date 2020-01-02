package com.ggh.video.net.udp;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import com.apkfuns.logutils.LogUtils;
import com.ggh.video.utils.MyConstants;
import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class NettyReceiverHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private ChannelHandlerContext channelHandlerContext;
    private VideoDataCallback videoDataCallback;
    private AudioDataCallback audioDataCallback;
    private CallCallback callCallback;
    private HangupCallback hangupCallback;
    private Handler handler;
    private HandlerThread myHandlerThread;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        try{
        ByteBuf buf = (ByteBuf) packet.copy().content();
        byte type = buf.readByte();//读取第一个字节
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        if (type== MyConstants.MSG_TYPE_NORMAL){
            String str = new String(data, "UTF-8");
            //LogUtils.d("接受普通消息:" + str);
            //服务器推送对方IP和PORT
            InetSocketAddress address = packet.sender();
            NettyClient.getIns().setTargetIp(address.getHostName());
            //NettyClient.getIns().setTargetPort(address.getPort());
            if(TextUtils.equals("call",str)){
                callCallback.call();
                NettyClient.getIns().sendData("call".getBytes(), MyConstants.MSG_TYPE_NORMAL);
            }else if(TextUtils.equals("hangup",str)){
                hangupCallback.hangup();
            }
        }else if (type== MyConstants.MSG_TYPE_VIDEO){
            //LogUtils.d("接受视频消息,length = "+data.length);
            videoDataCallback.onVideoData(data);
        }else if (type== MyConstants.MSG_TYPE_AUDIO){
            //LogUtils.d("接收音频消息,length ="+data.length);
            audioDataCallback.onAudioData(data);
        }
        }catch (Exception e){
            Log.e("www", e.getMessage());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channelHandlerContext = ctx;
        LogUtils.d("netty 启动");
        myHandlerThread = new HandlerThread( "handler-thread") ;
        myHandlerThread.start();
        handler = new Handler(myHandlerThread.getLooper());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Log.d("www", "通道异常关闭:"+ cause.getMessage());
    }


    public void sendData(final String ip, final int port, final byte[] data, final byte type) {
        /*byte[] sendBytes = new byte[data.length+1];
        sendBytes[0] = type;
        System.arraycopy(data, 0, sendBytes, 1, data.length);
        channelHandlerContext.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(sendBytes),
                new InetSocketAddress(ip, port)));*/
       handler.post(new Runnable() {
            @Override
            public void run() {
                byte[] sendBytes = new byte[data.length+1];
                sendBytes[0] = type;
                System.arraycopy(data, 0, sendBytes, 1, data.length);
                channelHandlerContext.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(sendBytes),
                        new InetSocketAddress(ip, port)));
            }
        });
    }

    public void setVideoDataCallback(VideoDataCallback videoDataCallback) {
        this.videoDataCallback = videoDataCallback;
    }

    public void setAudioDataCallback(AudioDataCallback audioDataCallback) {
        this.audioDataCallback = audioDataCallback;
    }

    public void setCallCallback(CallCallback callCallback) {
        this.callCallback = callCallback;
    }

    public void setHangupCallback(HangupCallback hangupCallback) {
        this.hangupCallback = hangupCallback;
    }

    public interface VideoDataCallback {
        void onVideoData(byte[] data);
    }

    public interface AudioDataCallback {
        void onAudioData(byte[] data);
    }

    public interface CallCallback {
        void call();
    }

    public interface HangupCallback {
        void hangup();
    }

    public void close(){
        myHandlerThread.quitSafely();
    }
}