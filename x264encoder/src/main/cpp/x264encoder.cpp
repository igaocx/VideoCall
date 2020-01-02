#include "x264encoder.h"
#include "PUX264Encoder.h"

#define TAG "www" // 这个是自定义的LOG的标识
#define LOGI(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
x264Encode *_x264Encoder;
H264DataCallBack h264callbackFunc;

void initX264Encode(int width, int height, int fps, int bite, H264DataCallBack h264callback) {
    LOGI("/*****initX264Encode*****/");
    /*if (NULL != _x264Encoder ){
        releaseX264Encode();
    }*/
    _x264Encoder = new x264Encode();
    _x264Encoder->initX264Encode(width, height, fps, bite);
    h264callbackFunc = h264callback;
}
//旋转90度
uint8_t * rotateYUVDegree90(uint8_t *data, int imageWidth, int imageHeight) {
    uint8_t *yuv = new uint8_t[imageWidth * imageHeight * 3 / 2];
// Rotate the Y luma
    int i = 0;
    for (int x = 0; x < imageWidth; x++) {
        for (int y = imageHeight - 1; y >= 0; y--) {
            yuv[i] = data[y * imageWidth + x];
            i++;
        }
    }
// Rotate the U and V color components
    i = imageWidth * imageHeight * 3 / 2 - 1;
    for (int x = imageWidth - 1; x > 0; x = x - 2) {
        for (int y = 0; y < imageHeight / 2; y++) {
            yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
            i--;
            yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
            i--;
        }
    }
    return yuv;
}
//yuv420sp转换成yuv420
uint8_t * YUV420SP2YUV420(uint8_t *yuv420sp, int width, int height) {
    uint8_t *yuv420 = new uint8_t[width*height*3/2];
    int framesize = width * height;
    int i = 0, j = 0;
    //copy y
    for (i = 0; i < framesize; i++) {
        yuv420[i] = yuv420sp[i];
    }
    i = 0;
    for (j = 0; j < framesize / 2; j += 2) {
        yuv420[i + framesize * 5 / 4] = yuv420sp[j + framesize];
        i++;
    }
    i = 0;
    for (j = 1; j < framesize / 2; j += 2) {
        yuv420[i + framesize] = yuv420sp[j + framesize];
        i++;
    }
    return yuv420;
}

void encoderH264(uint8_t *pdata, unsigned int datalen, long long time) {
    if (_x264Encoder == NULL) {
        LOGI("_x264Encoder is Null");
        return;
    }
    int i = 0;
    char *bufdata = NULL;
    int buflen = -1;
    int isKeyFrame;

    int width = 640;
    int height = 480;
    uint8_t * yuv420sp =rotateYUVDegree90(pdata,width,height);
    uint8_t *yuv420 = YUV420SP2YUV420(yuv420sp,width,height);
    //LOGI("/**********************PostOriginalSlice************************%d",datalen);
    _x264Encoder->startEncoder(yuv420, *&bufdata, *&buflen, *&isKeyFrame);
    if (buflen != -1) {
        if (NULL != h264callbackFunc) {
            h264callbackFunc(bufdata, buflen);
        }
        if (bufdata) {
            delete[] bufdata;
        }
    }
}

void releaseX264Encode() {
    if (_x264Encoder) {
        _x264Encoder->releaseEncoder();
        delete _x264Encoder;
        _x264Encoder = NULL;
    }
}