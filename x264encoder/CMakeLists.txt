cmake_minimum_required(VERSION 3.4.1)

add_library(x264encoder
             SHARED
             src/main/cpp/example_sszpf_x264_JNI.cpp
             src/main/cpp/PUX264Encoder.cpp
             src/main/cpp/x264encoder.cpp
             )

find_library( log-lib
              log )
find_library( jnigraphics-lib
              jnigraphics)
find_library( android-lib
              android)
find_library( math-lib
              m )

add_library(add STATIC IMPORTED)
set_target_properties(add PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libx264.a)

target_link_libraries(x264encoder
                       ${log-lib}
                       ${jnigraphics-lib}
                       ${android-lib}
                       ${math-lib}
                        add)
