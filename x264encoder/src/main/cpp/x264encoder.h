#include "Macro.h"
#include <stdio.h>
typedef void (CALLBACK *H264DataCallBack)(void* pdata,int datalen);

extern "C" 
{
	dllexport void initX264Encode(int width, int height, int fps, int bite,H264DataCallBack h264callback);

	dllexport void encoderH264(uint8_t * pdata,unsigned int datalen,long long time);

    dllexport void releaseX264Encode();
}