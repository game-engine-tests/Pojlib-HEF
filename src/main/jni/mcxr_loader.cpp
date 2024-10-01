#include <jni.h>
#include <thread>

//#define GL_GLES_PROTOTYPES 1
//#undef GL_EXT_texture_array
//#undef GL_VERSION_3_0

#include <GLES3/gl3.h>
//#include "GL/gl.h"
//#include "GL/glext.h"

//
// Created by Judge on 23rd December 2021
// Modified by Nova on 17th August 2024
//

static jobject* app;
static JavaVM* jvm;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    if (jvm == nullptr) {
        jvm = vm;
    }
    return JNI_VERSION_1_4;
}

JNIEXPORT JNICALL
extern "C" jlong
Java_net_sorenon_MCOpenXRNativeLoader_getJVMPtr(JNIEnv *env, jclass clazz) {
    return reinterpret_cast<jlong>(&jvm);
}

JNIEXPORT JNICALL
extern "C" void
Java_net_sorenon_MCOpenXRNativeLoader_renderImage(JNIEnv *env, jclass clazz, jint colorAttachment, jint index) {
    glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, colorAttachment, 0, index);
}

JNIEXPORT JNICALL
extern "C" jlong
Java_net_sorenon_MCOpenXRNativeLoader_getApplicationActivityPtr(JNIEnv *env, jclass clazz) {
    return reinterpret_cast<jlong>(&app);
}

extern "C"
JNIEXPORT void JNICALL
Java_pojlib_util_VLoader_setActivity(JNIEnv *env, jclass clazz, jobject ctx) {
    app = reinterpret_cast<jobject*>(env->NewGlobalRef(ctx));
}