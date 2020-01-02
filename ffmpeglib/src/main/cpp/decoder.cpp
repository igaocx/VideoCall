#include "decoder.h"

decoder::decoder() :
        codec(NULL), codecContext(NULL), frame_count(0),  frame(
                NULL), parser(NULL) {
}

void decoder::initialize(enum AVPixelFormat format) {

    /* register all the codecs */
    avcodec_register_all();
    av_init_packet(&avpkt);

    renderParam = NULL;

    /* set end of buffer to 0 (this ensures that no overreading happens for damaged mpeg streams) */
    memset(inbuf , 0, INBUF_SIZE);

    memset(inbuf + INBUF_SIZE, 0, FF_INPUT_BUFFER_PADDING_SIZE);

    /* find the x264 video decoder */
    codec = avcodec_find_decoder(AV_CODEC_ID_H264);
    if (!codec) {
        fprintf(stderr, "Codec not found\n");
        exit(1);
    }

    codecContext = avcodec_alloc_context3(codec);
    if (!codecContext) {
        fprintf(stderr, "Could not allocate video codec context\n");
        exit(1);
    }

    /* put sample parameters */
    //codecContext->bit_rate = 500000;
    //codecContext->width = 640;
    //codecContext->height = 480;
    //codecContext->time_base = (AVRational ) { 1, 15 };
    //codecContext->framerate = (AVRational ) { 1, 15 };
    //codecContext->gop_size = 1;
    //codecContext->max_b_frames = 0;
    //codecContext->pix_fmt = AV_PIX_FMT_YUV420P;
    //codecContext->ticks_per_frame = 0;
    //codecContext->delay = 0;
    //codecContext->b_quant_offset = 0.0;
    //codecContext->refs = 0;
    //codecContext->slices = 1;
    //codecContext->has_b_frames = 0;
    //codecContext->thread_count = 2;
    //av_opt_set(codecContext->priv_data, "zerolatency", "ultrafast", 0);

    /* we do not send complete frames */
    if (codec->capabilities & CODEC_CAP_TRUNCATED)
        codecContext->flags |= CODEC_FLAG_TRUNCATED;

    /* open it */
    if (avcodec_open2(codecContext, codec, NULL) < 0) {
        fprintf(stderr, "Could not open codec\n");
        exit(1);
    }

    frame = av_frame_alloc();
    if (!frame) {
        fprintf(stderr, "Could not allocate video frame\n");
        exit(1);
    }

    parser = av_parser_init(AV_CODEC_ID_H264);
    if (!parser) {
        std::cout << "cannot create parser" << std::endl;
        exit(1);
    }
//    parser->flags |= PARSER_FLAG_ONCE;
printf(" decoder init ..........\n");
    frame_count = 0;
    img_convert_ctx =NULL;

    pFrameRGB = NULL;

    //pixelFormat = AV_PIX_FMT_BGRA;
    //pixelFormat = AV_PIX_FMT_RGB565LE;
    //pixelFormat = AV_PIX_FMT_BGR24;
    pixelFormat = format;
}

int decoder::decodeFrame(uint8_t *data, int length, void (*handle_data)(AVFrame *pFrame, void *param, void *ctx), void *ctx) {

    int cur_size = length;
    int ret = 0;

    memcpy(inbuf, data, length);
    uint8_t *cur_ptr = inbuf;
    // Parse input stream to check if there is a valid frame.
    //std::cout << " in data  --  -- " << length<< std::endl;
    while(cur_size >0)
    {
        int parsedLength = av_parser_parse2(parser, codecContext, &avpkt.data,
                &avpkt.size, cur_ptr , cur_size, AV_NOPTS_VALUE,
                AV_NOPTS_VALUE, AV_NOPTS_VALUE);

                 handleH264Header(cur_ptr);
                 cur_ptr += parsedLength;
                 cur_size -= parsedLength;
        //std::cout <<" avpkt.size  "<< avpkt.size << " -- cur_size "<< cur_size <<" "<<parsedLength<< std::endl;
        // 67 sps
        // 68 pps
        // 65 i
        // 61 p
        //LOGE("parsedLength %d    %x %x %x %x %x %x %x %x", parsedLength, cur_ptr[0], cur_ptr[1], cur_ptr[2], cur_ptr[3], cur_ptr[4], cur_ptr[5], cur_ptr[6], cur_ptr[7]);
        //LOGE("parsedLength %d    %x %x %x %x %x %x %x %x", parsedLength, *(cur_ptr-parsedLength), *(cur_ptr-parsedLength+1), *(cur_ptr-parsedLength+2), *(cur_ptr-parsedLength+3), *(cur_ptr-parsedLength+4), *(cur_ptr-parsedLength+5), *(cur_ptr-parsedLength+6), *(cur_ptr-parsedLength+7));
        if (!avpkt.size) {
            continue;
        } else {
                int len, got_frame;
                len = avcodec_decode_video2(codecContext, frame, &got_frame,
                        &avpkt);

                if (len < 0) {
                    fprintf(stderr, "Error while decoding frame %d\n", frame_count);
                    //break;
                    continue;
                    // exit(1);
                }

                if (got_frame) {
                    frame_count++;

                    if(img_convert_ctx == NULL){
                        img_convert_ctx = sws_getContext(codecContext->width, codecContext->height,
                                                         codecContext->pix_fmt, codecContext->width, codecContext->height,
                                                         pixelFormat, SWS_BICUBIC, NULL, NULL, NULL);

                        renderParam = (RenderParam *)malloc(sizeof(RenderParam));
                        renderParam->swsContext = img_convert_ctx;
                        renderParam->avCodecContext = codecContext;
                    }

                    if(img_convert_ctx != NULL) {
                        handle_data(frame, renderParam, ctx);
                    }

                    //根据编码信息设置渲染格式
                    /*if(img_convert_ctx == NULL){
                        //std::cout << " init img_convert_ctx \n";
                        img_convert_ctx = sws_getContext(codecContext->width, codecContext->height,
                                codecContext->pix_fmt, codecContext->width, codecContext->height,
                                AV_PIX_FMT_BGR24, SWS_BICUBIC, NULL, NULL, NULL);
                    }

                    //----------------------opencv
                    cv::Mat curmat;
                    curmat.create(cv::Size(codecContext->width, codecContext->height),CV_8UC3);

                    if(img_convert_ctx != NULL)
                    {
                        AVFrame	*pFrameRGB = NULL;
                        uint8_t  *out_bufferRGB = NULL;
                        pFrameRGB = av_frame_alloc();
                        //给pFrameRGB帧加上分配的内存;
                        int size = avpicture_get_size(AV_PIX_FMT_BGR24, codecContext->width, codecContext->height);
                        out_bufferRGB = new uint8_t[size];
                        avpicture_fill((AVPicture *)pFrameRGB, out_bufferRGB, AV_PIX_FMT_BGR24, codecContext->width, codecContext->height);

                        //YUV to RGB
                        sws_scale(img_convert_ctx, frame->data, frame->linesize, 0, codecContext->height, pFrameRGB->data, pFrameRGB->linesize);

                        memcpy(curmat.data,out_bufferRGB,size);

                        vecMat.push(curmat);

                        delete[] out_bufferRGB;
                        av_free(pFrameRGB);
                    }*/
                }
        }
    }
    return length;
}

/*bool decoder::getFrame(cv::Mat & curmat)
{
    if(vecMat.empty())
        return false;
    curmat = vecMat.front();
    vecMat.pop();
    return true;
}*/
void decoder::close() {
    av_free_packet(&avpkt);
    avpkt.data = NULL;
    avpkt.size = 0;
    if (parser) {
        av_parser_close(parser);
        parser = NULL;
    }

    if(renderParam){
        free(renderParam);
        renderParam = NULL;
    }

    if(pFrameRGB){
        delete pFrameRGB;
        pFrameRGB = NULL;
    }

    avcodec_close(codecContext);
    av_free(codecContext);
    av_frame_free(&frame);
    //while(!vecMat.empty())vecMat.pop();
    if(img_convert_ctx!=NULL)
    {
        sws_freeContext(img_convert_ctx);
        img_convert_ctx = NULL;
    }
     LOGE(" decoder close ..........\n");
}

AVFrame * decoder::getFrameRGB() {
    return pFrameRGB;
}

int decoder::handleH264Header(uint8_t* ptr){
    int startIndex = 0;
    uint32_t *checkPtr = (uint32_t *)ptr;
    if(*checkPtr == 0x01000000){  // 00 00 00 01
        startIndex = 4;
    }else if(*(checkPtr) == 0 && *(checkPtr+1)&0x01000000){  // 00 00 00 00 01
        startIndex = 5;
    }

    if(!startIndex){
        return -1;
    }else{
        ptr = ptr + startIndex;
    }
    return 0;
}

void decoder::setFrameRGB(AVFrame *frame) {
    pFrameRGB = frame;
}