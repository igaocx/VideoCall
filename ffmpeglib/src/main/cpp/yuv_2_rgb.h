//
// Created by 李德琛 on 5/31/17.
//

#ifndef MYAPPLICATION2_YUV_2_RGB_H
#define MYAPPLICATION2_YUV_2_RGB_H
extern "C"
{
//#ifdef PLATFORM_ANDROID
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"

#include "log.h"

/*#else
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#endif*/
};

extern "C"
{
    AVFrame	*yuv420p_2_argb(AVFrame	*frame, SwsContext *swsContext, AVCodecContext *avCodecContext, enum AVPixelFormat format);
}

#endif //MYAPPLICATION2_YUV_2_RGB_H
