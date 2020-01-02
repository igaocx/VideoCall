//frameEncoder
#define FF_INPUT_BUFFER_PADDING_SIZE 0
#define CODEC_CAP_TRUNCATED 0
#define CODEC_FLAG_TRUNCATED 0

#ifndef X264_EXAMPLE_DECODER_H
#define X264_EXAMPLE_DECODER_H

#include <inttypes.h>
#include <stdio.h>
#include <iostream>
#include <stdint.h>
#include <stdlib.h>
#include <sstream>
#include <string>
#include <cstring>
#include <fstream>

//#include <opencv2/core/core.hpp>
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

#include <queue>

#define INBUF_SIZE 102400
//#define INBUF_SIZE 8192

typedef struct _RenderParam{
    SwsContext *swsContext;
    AVCodecContext *avCodecContext;
}RenderParam;

typedef struct _NalInfo{
    uint8_t forbidden_zero_bit;
    uint8_t nal_ref_idc;
    uint8_t nal_unit_type;
} NalInfo;

class decoder {

private:

    int frame_count;
    AVFrame *frame;
    uint8_t inbuf[INBUF_SIZE + FF_INPUT_BUFFER_PADDING_SIZE];
    AVPacket avpkt;
    AVCodecParserContext *parser;
    AVCodec *codec;
    AVCodecContext *codecContext;
    SwsContext *img_convert_ctx;

    enum AVPixelFormat pixelFormat;

    //rgb frame cache
    AVFrame	*pFrameRGB;
    //std::queue < cv::Mat > vecMat;
public:

    RenderParam *renderParam = NULL;

    decoder();
    void initialize(enum AVPixelFormat format);
    int decodeFrame(uint8_t* data, int length, void (*handle_data)(AVFrame *pFrame, void *param, void *ctx), void *ctx);
    void close();
    //bool getFrame(cv::Mat & curmat);

    void setFrameRGB(AVFrame *frame);
    int handleH264Header(uint8_t* ptr);
    AVFrame *getFrameRGB();
};

#endif
