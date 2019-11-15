package main

/*
#ifndef JNI_COMPILE
#define JNI_COMPILE
#include <jni.h>
#include <string.h>

#define CLASH_EXCEPTION_CLASS "com/github/kr328/clash/core/ClashException"

extern const char *_GoStringPtr(_GoString_ s);

static void jni_throw_clash_exception(JNIEnv *env, _GoString_ message) {
	(*env)->ThrowNew(env, CLASH_EXCEPTION_CLASS, _GoStringPtr(message));
}

static char *jni_get_string(JNIEnv *env, jstring string) {
	const char *jni_string = (*env)->GetStringUTFChars(env, string, NULL);
	char *result = strdup(jni_string);
	(*env)->ReleaseStringUTFChars(env, string, jni_string);
	return result;
}
#endif //JNI_COMPILE
*/
import "C"
import "unsafe"

import (
	"github.com/kr328/clash-for-android/log"
	"github.com/kr328/clash-for-android/profile"
)

//export Java_com_github_kr328_clash_core_Clash_loadDefault
func Java_com_github_kr328_clash_core_Clash_loadDefault(env *C.JNIEnv, class C.jclass) {
	log.Info("Clash load default")

	profile.LoadDefault()
}

//export Java_com_github_kr328_clash_core_Clash_loadProfileFromPath
func Java_com_github_kr328_clash_core_Clash_loadProfileFromPath(env *C.JNIEnv, class C.jclass, path C.jstring) {
	cs := C.jni_get_string(env, path)
	if cs == nil {
		C.jni_throw_clash_exception(env, "Unable to get path")
		return
	}

	p := C.GoString(cs)

	C.free(unsafe.Pointer(cs))

	if err := profile.LoadFromFile(p); err != nil {
		C.jni_throw_clash_exception(env, err.Error())
	}
}
