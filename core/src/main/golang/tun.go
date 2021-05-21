package main

//#include "bridge.h"
import "C"

import (
	"context"
	"unsafe"

	"cfa/app"
	"cfa/tun"

	"golang.org/x/sync/semaphore"

	"github.com/Dreamacro/clash/log"
)

type remoteTun struct {
	callback unsafe.Pointer

	closed bool
	limit  *semaphore.Weighted
}

func (t *remoteTun) markSocket(fd int) {
	_ = t.limit.Acquire(context.Background(), 1)
	defer t.limit.Release(1)

	if t.closed {
		return
	}

	C.mark_socket(t.callback, C.int(fd))
}

func (t *remoteTun) querySocketUid(protocol int, source, target string) int {
	_ = t.limit.Acquire(context.Background(), 1)
	defer t.limit.Release(1)

	if t.closed {
		return -1
	}

	return int(C.query_socket_uid(t.callback, C.int(protocol), C.CString(source), C.CString(target)))
}

func (t *remoteTun) stop() {
	_ = t.limit.Acquire(context.Background(), 4)
	defer t.limit.Release(4)

	t.closed = true

	C.release_object(t.callback)

	log.Infoln("Android tun device destroyed")
}

//export startTun
func startTun(fd, mtu C.int, dns C.c_string, callback unsafe.Pointer) C.int {
	f := int(fd)
	m := int(mtu)
	d := C.GoString(dns)

	remote := &remoteTun{callback: callback, closed: false, limit: semaphore.NewWeighted(4)}

	if tun.Start(f, m, d) != nil {
		return 1
	}

	app.ApplyTunContext(remote.markSocket, remote.querySocketUid)

	log.Infoln("Android tun device created")

	return 0
}

//export stopTun
func stopTun() {
	tun.Stop()
}
