package main

/*
#cgo LDFLAGS: -llog
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>

static void log_print(const char *msg) {
    __android_log_print(ANDROID_LOG_INFO, "ClashForAndroid", "%s", msg);
}
*/
import "C"
import "unsafe"

//export Java_com_github_kr328_clash_core_Clash_loadDefault
func Java_com_github_kr328_clash_core_Clash_loadDefault(env *C.JNIEnv, class C.jclass) {
    log := C.CString("Hello World form go")
    C.log_print(log)
    C.free(unsafe.Pointer(log))
}

func main() {

}
