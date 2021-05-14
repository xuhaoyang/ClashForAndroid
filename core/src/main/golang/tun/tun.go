package tun

import (
	"net"
	"os"
	"strconv"
	"sync"

	"github.com/kr328/tun2socket/binding"
	"github.com/kr328/tun2socket/redirect"

	"github.com/kr328/tun2socket"
)

var lock sync.Mutex
var tun *tun2socket.Tun2Socket

func Start(fd, mtu int, gateway, mirror, dns string, onStop func()) error {
	lock.Lock()
	defer lock.Unlock()

	stopLocked()

	dnsHost, dnsPort, err := net.SplitHostPort(dns)
	if err != nil {
		return err
	}

	dnsP, err := strconv.Atoi(dnsPort)
	if err != nil {
		return err
	}

	dnsAddr := binding.Address{
		IP:   net.ParseIP(dnsHost),
		Port: uint16(dnsP),
	}

	t := tun2socket.NewTun2Socket(os.NewFile(uintptr(fd), "/dev/tun"), mtu, net.ParseIP(gateway), net.ParseIP(mirror))

	t.SetAllocator(allocUDP)
	t.SetClosedHandler(onStop)
	t.SetLogger(&logger{})

	t.SetTCPHandler(func(conn net.Conn, endpoint *binding.Endpoint) {
		if shouldHijackDns(dnsAddr, endpoint.Target) {
			hijackTCPDns(conn)

			return
		}

		handleTCP(conn, endpoint)
	})
	t.SetUDPHandler(func(payload []byte, endpoint *binding.Endpoint, sender redirect.UDPSender) {
		if shouldHijackDns(dnsAddr, endpoint.Target) {
			hijackUDPDns(payload, endpoint, sender)

			return
		}

		handleUDP(payload, endpoint, sender)
	})

	t.Start()

	tun = t

	return nil
}

func Stop() {
	lock.Lock()
	defer lock.Unlock()

	stopLocked()
}

func stopLocked() {
	if tun != nil {
		tun.Close()
	}

	tun = nil
}
