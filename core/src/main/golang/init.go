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

import (
	"github.com/Dreamacro/clash/constant"
)

//export Java_com_github_kr328_clash_core_Clash_init
func Java_com_github_kr328_clash_core_Clash_init(env *C.JNIEnv, class C.jclass, home C.jstring) {
	cs := C.jni_get_string(env, home)
	if cs == nil {
		C.jni_throw_clash_exception(env, "Unable to get path")
		return
	}

	p := C.GoString(cs)

	C.free(unsafe.Pointer(cs))

	constant.SetHomeDir(p)
}
