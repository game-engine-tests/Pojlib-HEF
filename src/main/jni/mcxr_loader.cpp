#include <jni.h>
#include <thread>

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
Java_net_sorenon_mcxr_play_MCXRNativeLoad_getJVMPtr(JNIEnv *env, jclass clazz) {
    return reinterpret_cast<jlong>(&jvm);
}

JNIEXPORT JNICALL
extern "C" jlong
Java_net_sorenon_mcxr_play_MCXRNativeLoad_getApplicationActivityPtr(JNIEnv *env, jclass clazz) {
    return reinterpret_cast<jlong>(&app);
}

extern "C"
JNIEXPORT void JNICALL
Java_pojlib_VLoader_setActivity(JNIEnv *env, jclass clazz, jobject ctx) {
    app = reinterpret_cast<jobject*>(env->NewGlobalRef(ctx));
}