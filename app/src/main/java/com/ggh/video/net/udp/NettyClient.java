package com.ggh.video.net.udp;

import android.util.Log;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class NettyClient {

    private static NettyClient instance;

    private NettyReceiverHandler handler;

    private int localPort = 7777;//绑定本地端口
    private String targetIp = "127.0.0.1";//对方ip地址
    private int targetPort = 7777; // 对方端口
    private ChannelFuture ch;

    private NettyClient(){
        init();
    }

    public static NettyClient getIns(){
        if(instance==null){
            instance = new NettyClient();
        }
        return instance;
    }

    /**
     * 初始化
     */
    private void init() {
        handler = new NettyReceiverHandler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                final NioEventLoopGroup group = new NioEventLoopGroup();
                try {
                    b.group(group)
                            .channel(NioDatagramChannel.class)
                            .option(ChannelOption.SO_BROADCAST, true)
                            //接收区5m缓存
                            .option(ChannelOption.SO_RCVBUF, 1024 * 1024*5)
                            //加上这个，里面是最大接收、发送的长度
                            .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                            .handler(handler);
                    ch = b.bind(localPort).sync();
                    ch.channel().closeFuture().addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future){
                            Log.d("www","netty close");
                            group.shutdownGracefully();
                        }
                    });
                    ch.channel().closeFuture().await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void close(){
        ch.channel().close();
        handler.close();
        instance=null;
    }
    /**
     * 发送数据
     *
     * @param data
     */
    public void sendData(byte[] data, byte msgType) {
        handler.sendData(targetIp, targetPort, data, msgType);
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }


    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public void setVideoDataCallback(NettyReceiverHandler.VideoDataCallback videoDataCallback) {
        handler.setVideoDataCallback(videoDataCallback);
    }

    public void setAudioDataCallback(NettyReceiverHandler.AudioDataCallback audioDataCallback) {
        handler.setAudioDataCallback(audioDataCallback);
    }

    public void setCallCallback(NettyReceiverHandler.CallCallback callCallback) {
        handler.setCallCallback(callCallback);
    }

    public void setHangupCallback(NettyReceiverHandler.HangupCallback hangupCallback) {
      handler.setHangupCallback(hangupCallback);
    }
}
