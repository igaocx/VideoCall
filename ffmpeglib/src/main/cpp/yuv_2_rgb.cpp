//
// Created by 李德琛 on 5/31/17.
//
extern "C" {
#include "yuv_2_rgb.h"
}
#ifdef __cplusplus
extern "C" {
#endif

AVFrame	*yuv420p_2_argb(AVFrame	*frame, SwsContext *swsContext, AVCodecContext *avCodecContext, enum AVPixelFormat format){
    AVFrame	*pFrameRGB = NULL;
    uint8_t  *out_bufferRGB = NULL;
    pFrameRGB = av_frame_alloc();

    pFrameRGB->width = frame->width;
    pFrameRGB->height = frame->height;

    //给pFrameRGB帧加上分配的内存;  //AV_PIX_FMT_ARGB
    int size = avpicture_get_size(format, avCodecContext->width, avCodecContext->height);
    //out_bufferRGB = new uint8_t[size];
    out_bufferRGB = (uint8_t  *)av_malloc(size * sizeof(uint8_t));
    avpicture_fill((AVPicture *)pFrameRGB, out_bufferRGB, format, avCodecContext->width, avCodecContext->height);
    //YUV to RGB
    sws_scale(swsContext, frame->data, frame->linesize, 0, avCodecContext->height, pFrameRGB->data, pFrameRGB->linesize);

    return pFrameRGB;
}

#ifdef __cplusplus
}
#endif