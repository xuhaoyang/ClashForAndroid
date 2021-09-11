package app

import (
	"net"
	"strings"
	"syscall"

	"cfa/native/platform"
)

var markSocketImpl func(fd int)
var querySocketUidImpl func(protocol int, source, target string) int

func MarkSocket(fd int) {
	markSocketImpl(fd)
}

func QuerySocketUid(source, target net.Addr) int {
	protocol := syscall.IPPROTO_TCP

	if strings.HasPrefix(source.String(), "udp") {
		protocol = syscall.IPPROTO_UDP
	}

	if PlatformVersion() < 29 {
		return platform.QuerySocketUidFromProcFs(source, target)
	}

	return querySocketUidImpl(protocol, source.String(), target.String())
}

func ApplyTunContext(markSocket func(fd int), querySocketUid func(int, string, string) int) {
	if markSocket == nil {
		markSocket = func(fd int) {}
	}

	if querySocketUid == nil {
		querySocketUid = func(int, string, string) int { return -1 }
	}

	markSocketImpl = markSocket
	querySocketUidImpl = querySocketUid
}

func init() {
	ApplyTunContext(nil, nil)
}
