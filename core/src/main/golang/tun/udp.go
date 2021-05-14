package tun

import (
	"io"
	"net"

	"github.com/Dreamacro/clash/transport/socks5"
	"github.com/kr328/tun2socket/binding"
	"github.com/kr328/tun2socket/redirect"

	adapters "github.com/Dreamacro/clash/adapters/inbound"
	"github.com/Dreamacro/clash/common/pool"
	C "github.com/Dreamacro/clash/constant"
	"github.com/Dreamacro/clash/tunnel"
)

type udpPacket struct {
	metadata *C.Metadata
	source   binding.Address
	data     []byte
	send     redirect.UDPSender
}

func (u *udpPacket) Data() []byte {
	return u.data
}

func (u *udpPacket) WriteBack(b []byte, addr net.Addr) (n int, err error) {
	uAddr, ok := addr.(*net.UDPAddr)
	if !ok {
		return 0, io.ErrClosedPipe
	}

	return len(b), u.send(b, &binding.Endpoint{
		Source: binding.Address{IP: uAddr.IP, Port: uint16(uAddr.Port)},
		Target: u.source,
	})
}

func (u *udpPacket) Drop() {
	recycleUDP(u.data)
}

func (u *udpPacket) LocalAddr() net.Addr {
	return &net.UDPAddr{
		IP:   u.source.IP,
		Port: int(u.source.Port),
		Zone: "",
	}
}

func handleUDP(payload []byte, endpoint *binding.Endpoint, sender redirect.UDPSender) {
	pkt := &udpPacket{
		source: endpoint.Source,
		data:   payload,
		send:   sender,
	}

	rAddr := &net.UDPAddr{
		IP:   endpoint.Target.IP,
		Port: int(endpoint.Target.Port),
		Zone: "",
	}

	adapter := adapters.NewPacket(socks5.ParseAddrToSocksAddr(rAddr), pkt, C.SOCKS)

	tunnel.AddPacket(adapter)
}

func allocUDP(size int) []byte {
	return pool.Get(size)
}

func recycleUDP(payload []byte) {
	_ = pool.Put(payload)
}
