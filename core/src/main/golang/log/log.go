package log

/*
#cgo LDFLAGS: -llog
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>

#define TAG "ClashForAndroid"

static void log_info(_GoString_ msg) {
    __android_log_print(ANDROID_LOG_INFO, "ClashForAndroid", "%s", _GoStringPtr(msg));
}

static void log_error(_GoString_ msg) {
	__android_log_print(ANDROID_LOG_ERROR, "ClashForAndroid", "%s", _GoStringPtr(msg));
}

static void log_warn(_GoString_ msg) {
	__android_log_print(ANDROID_LOG_WARN, "ClashForAndroid", "%s", _GoStringPtr(msg));
}
*/
import "C"

func Info(msg string) {
	C.log_info(msg)
}

func Error(msg string) {
	C.log_error(msg)
}

func Warn(msg string) {
	C.log_warn(msg)
}
