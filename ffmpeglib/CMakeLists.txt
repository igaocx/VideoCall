cmake_minimum_required(VERSION 3.4.1)


add_library( codec
             SHARED
             src/main/cpp/android_native_window.cpp
             src/main/cpp/com_vonchenchen_android_video_demos_codec_CodecWrapper.cpp
             src/main/cpp/decoder.cpp
             src/main/cpp/yuv_2_rgb.cpp
             )

find_library( log-lib
              log )
find_library( jnigraphics-lib
              jnigraphics )
find_library( android-lib
              android )
find_library( math-lib
              m )

set(distribution_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)

MESSAGE("path:" ${distribution_DIR})

target_include_directories(codec PRIVATE
                           ${distribution_DIR}/include)

add_library(ffmpeg SHARED IMPORTED)
set_target_properties(ffmpeg PROPERTIES IMPORTED_LOCATION
    ${distribution_DIR}/${ANDROID_ABI}/libffmpeg.so)



target_link_libraries( codec
                       ${log-lib}
                       ${jnigraphics-lib}
                       ${android-lib}
                       ${math-lib}
                        ffmpeg)
