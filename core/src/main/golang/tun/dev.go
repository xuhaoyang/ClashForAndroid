package tun

import (
	"errors"
	"syscall"
	"unsafe"

	"github.com/google/netstack/tcpip/link/fdbased"
	"github.com/google/netstack/tcpip/stack"

	"github.com/Dreamacro/clash/log"
)

type tunDevice struct {
	mtu       int
	fd        int
	linkCache *stack.LinkEndpoint
}

func openTunDevice(fd int, mtu int) (*tunDevice, error) {
	var ifr struct {
		name  [16]byte
		flags uint16
		_     [22]byte
	}

	_, _, errno := syscall.Syscall(syscall.SYS_IOCTL, uintptr(fd), syscall.TUNGETIFF, uintptr(unsafe.Pointer(&ifr)))
	if errno != 0 {
		return nil, errno
	}

	if ifr.flags&syscall.IFF_TUN == 0 || ifr.flags&syscall.IFF_NO_PI == 0 {
		return nil, errors.New("Only tun device and no pi mode supported")
	}

	log.Infoln("Open tun fd = %d mtu = %d", fd, mtu)

	return &tunDevice{
		fd:  fd,
		mtu: mtu,
	}, nil
}

func (t tunDevice) asLinkEndpoint() (result stack.LinkEndpoint, err error) {
	if t.linkCache != nil {
		return *t.linkCache, nil
	}

	if err != nil {
		return nil, errors.New("Unable to get device mtu")
	}

	result, err = fdbased.New(&fdbased.Options{
		FDs:            []int{t.fd},
		MTU:            uint32(t.mtu),
		EthernetHeader: false,
	})

	t.linkCache = &result

	return result, nil
}

func (t tunDevice) close() {
	syscall.Close(t.fd)
}
