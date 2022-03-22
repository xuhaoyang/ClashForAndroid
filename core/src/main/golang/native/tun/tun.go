package tun

import (
	"encoding/binary"
	"io"
	"net"
	"os"
	"time"

	"github.com/Kr328/tun2socket"

	"github.com/Dreamacro/clash/adapter/inbound"
	"github.com/Dreamacro/clash/common/pool"
	C "github.com/Dreamacro/clash/constant"
	"github.com/Dreamacro/clash/context"
	"github.com/Dreamacro/clash/log"
	"github.com/Dreamacro/clash/transport/socks5"
	"github.com/Dreamacro/clash/tunnel"
)

var _, ipv4LoopBack, _ = net.ParseCIDR("127.0.0.0/8")

func Start(fd int, gateway, portal, dns string) (io.Closer, error) {
	log.Debugln("TUN: fd = %d, gateway = %s, portal = %s, dns = %s", fd, gateway, portal, dns)

	device := os.NewFile(uintptr(fd), "/dev/tun")

	ip, network, err := net.ParseCIDR(gateway)
	if err != nil {
		panic(err.Error())
	} else {
		network.IP = ip
	}

	stack, err := tun2socket.StartTun2Socket(device, network, net.ParseIP(portal))
	if err != nil {
		_ = device.Close()

		return nil, err
	}

	dnsAddr := net.ParseIP(dns)

	tcp := func() {
		defer stack.TCP().Close()
		defer log.Debugln("TCP: closed")

		for stack.TCP().SetDeadline(time.Time{}) == nil {
			conn, err := stack.TCP().Accept()
			if err != nil {
				log.Debugln("Accept connection: %v", err)

				continue
			}

			lAddr := conn.LocalAddr().(*net.TCPAddr)
			rAddr := conn.RemoteAddr().(*net.TCPAddr)

			if ipv4LoopBack.Contains(rAddr.IP) {
				conn.Close()

				continue
			}

			if shouldHijackDns(dnsAddr, rAddr.IP, rAddr.Port) {
				go func() {
					defer conn.Close()

					buf := pool.Get(pool.UDPBufferSize)
					defer pool.Put(buf)

					for {
						conn.SetReadDeadline(time.Now().Add(C.DefaultTCPTimeout))

						length := uint16(0)
						if err := binary.Read(conn, binary.BigEndian, &length); err != nil {
							return
						}

						if int(length) > len(buf) {
							return
						}

						n, err := conn.Read(buf[:length])
						if err != nil {
							return
						}

						msg, err := relayDns(buf[:n])
						if err != nil {
							return
						}

						_, _ = conn.Write(msg)
					}
				}()

				continue
			}

			tunnel.TCPIn() <- context.NewConnContext(conn, createMetadata(lAddr, rAddr))
		}
	}

	udp := func() {
		defer stack.UDP().Close()
		defer log.Debugln("UDP: closed")

		for {
			buf := pool.Get(pool.UDPBufferSize)

			n, lRAddr, rRAddr, err := stack.UDP().ReadFrom(buf)
			if err != nil {
				return
			}

			raw := buf[:n]
			lAddr := lRAddr.(*net.UDPAddr)
			rAddr := rRAddr.(*net.UDPAddr)

			if ipv4LoopBack.Contains(rAddr.IP) {
				pool.Put(buf)

				continue
			}

			if shouldHijackDns(dnsAddr, rAddr.IP, rAddr.Port) {
				go func() {
					defer pool.Put(buf)

					msg, err := relayDns(raw)
					if err != nil {
						return
					}

					_, _ = stack.UDP().WriteTo(msg, rAddr, lAddr)
				}()

				continue
			}

			pkt := &packet{
				local: lAddr,
				data:  raw,
				writeBack: func(b []byte, addr net.Addr) (int, error) {
					return stack.UDP().WriteTo(b, addr, lAddr)
				},
				drop: func() {
					pool.Put(buf)
				},
			}

			tunnel.UDPIn() <- inbound.NewPacket(socks5.ParseAddrToSocksAddr(rAddr), pkt, C.SOCKS5)
		}
	}

	go tcp()
	go udp()
	go udp()

	return stack, nil
}
