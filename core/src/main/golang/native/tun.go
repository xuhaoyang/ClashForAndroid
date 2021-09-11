package main

//#include "bridge.h"
import "C"

import (
	"context"
	"unsafe"

	"golang.org/x/sync/semaphore"

	"cfa/native/app"
	"cfa/native/tun"
)

type remoteTun struct {
	callback unsafe.Pointer

	closed bool
	limit  *semaphore.Weighted
}

func (t *remoteTun) markSocket(fd int) {
	_ = t.limit.Acquire(context.TODO(), 1)
	defer t.limit.Release(1)

	if t.closed {
		return
	}

	C.mark_socket(t.callback, C.int(fd))
}

func (t *remoteTun) querySocketUid(protocol int, source, target string) int {
	_ = t.limit.Acquire(context.TODO(), 1)
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

	app.ApplyTunContext(nil, nil)

	C.release_object(t.callback)
}

//export startTun
func startTun(fd, mtu C.int, gateway, dns C.c_string, callback unsafe.Pointer) C.int {
	f := int(fd)
	m := int(mtu)
	g := C.GoString(gateway)
	d := C.GoString(dns)

	remote := &remoteTun{callback: callback, closed: false, limit: semaphore.NewWeighted(4)}

	app.ApplyTunContext(remote.markSocket, remote.querySocketUid)

	if tun.Start(f, m, g, d, remote.stop) != nil {
		app.ApplyTunContext(nil, nil)

		return 1
	}

	return 0
}

//export stopTun
func stopTun() {
	tun.Stop()
}
