package tun

import (
	"net"
	"os"
	"sync"

	"github.com/kr328/tun2socket"
)

type context struct {
	stack tun2socket.Stack
	device *os.File
}

var lock sync.Mutex
var tun *context

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

	go func() {
		// device -> lwip

		defer device.Close()
		defer stack.Close()

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

		defer device.Close()
		defer stack.Close()

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

	tun = &context{
		stack:  stack,
		device: device,
	}

	return nil
}

func Stop() {
	lock.Lock()
	defer lock.Unlock()

	stopLocked()
}

func stopLocked() {
	if tun != nil {
		tun.device.Close()
		tun.stack.Close()
	}

	tun = nil
}
