package log

/*
#cgo LDFLAGS: -llog
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>

#define TAG "ClashForAndroid"

static void log_info(const char *msg) {
    __android_log_print(ANDROID_LOG_INFO, "ClashForAndroid", "%s", msg);
}

static void log_error(const char *msg) {
	__android_log_print(ANDROID_LOG_ERROR, "ClashForAndroid", "%s", msg);
}

static void log_warn(const char *msg) {
	__android_log_print(ANDROID_LOG_WARN, "ClashForAndroid", "%s", msg);
}
*/
import "C"
import "unsafe"

func Info(msg string) {
	log := C.CString(msg)
	C.log_info(log)
	C.free(unsafe.Pointer(log))
}

func Error(msg string) {
	log := C.CString(msg)
	C.log_error(log)
	C.free(unsafe.Pointer(log))
}

func Warn(msg string) {
	log := C.CString(msg)
	C.log_warn(log)
	C.free(unsafe.Pointer(log))
}