package tun

import (
	"net"
	"os"
	"sync"

	"github.com/kr328/tun2socket"
)

type context struct {
	device *os.File
	stack tun2socket.Stack
}

var lock sync.Mutex
var tun *context

func (ctx *context) close() {
	_ = ctx.stack.Close()
	_ = ctx.device.Close()
}

func Start(fd, mtu int, dns string) error {
	lock.Lock()
	defer lock.Unlock()

	stopLocked()

	dnsIP := net.ParseIP(dns)

	device := os.NewFile(uintptr(fd), "/dev/tun")

	stack, err := tun2socket.NewStack(mtu)
	if err != nil {
		_ = device.Close()

		return err
	}

	ctx := &context{
		device: device,
		stack:  stack,
	}

	go func() {
		// device -> lwip

		defer ctx.close()

		buf := make([]byte, mtu)

		for {
			n, err := device.Read(buf)
			if err != nil {
				return
			}

			_, _ = stack.Link().Write(buf[:n])
		}
	}()

	go func() {
		// lwip -> device

		defer ctx.close()

		buf := make([]byte, mtu)

		for {
			n, err := stack.Link().Read(buf)
			if err != nil {
				return
			}

			_, _ = device.Write(buf[:n])
		}
	}()

	go func() {
		// lwip tcp

		defer ctx.close()

		for {
			conn, err := stack.TCP().Accept()
			if err != nil {
				return
			}

			source := conn.LocalAddr().(*net.TCPAddr)
			target := conn.RemoteAddr().(*net.TCPAddr)

			if shouldHijackDns(dnsIP, target.IP, target.Port) {
				hijackTCPDns(conn)

				continue
			}

			handleTCP(conn, source, target)
		}
	}()

	go func() {
		// lwip udp

		defer ctx.close()

		for {
			buf := allocUDP(mtu)

			n, lAddr, rAddr, err := stack.UDP().ReadFrom(buf)
			if err != nil {
				return
			}

			source := lAddr.(*net.UDPAddr)
			target := rAddr.(*net.UDPAddr)

			if n == 0 {
				continue
			}

			if shouldHijackDns(dnsIP, target.IP, target.Port) {
				hijackUDPDns(buf[:n], source, target, stack.UDP())

				continue
			}

			handleUDP(buf[:n], source, target, stack.UDP())
		}
	}()

	tun = ctx

	return nil
}

func Stop() {
	lock.Lock()
	defer lock.Unlock()

	stopLocked()
}

func stopLocked() {
	if tun != nil {
		tun.close()
	}

	tun = nil
}
