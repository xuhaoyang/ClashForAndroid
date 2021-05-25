package tun

import (
	"net"

	"github.com/Dreamacro/clash/transport/socks5"
	"github.com/kr328/tun2socket"

	adapters "github.com/Dreamacro/clash/adapters/inbound"
	"github.com/Dreamacro/clash/common/pool"
	C "github.com/Dreamacro/clash/constant"
	"github.com/Dreamacro/clash/tunnel"
)

type udpPacket struct {
	source   *net.UDPAddr
	data     []byte
	udp      tun2socket.UDP
}

func (u *udpPacket) Data() []byte {
	return u.data
}

func (u *udpPacket) WriteBack(b []byte, addr net.Addr) (n int, err error) {
	return u.udp.WriteTo(b, u.source, addr)
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

func handleUDP(payload []byte, source *net.UDPAddr, target *net.UDPAddr, udp tun2socket.UDP) {
	pkt := &udpPacket{
		source:   source,
		data:     payload,
		udp:      udp,
	}

	adapter := adapters.NewPacket(socks5.ParseAddrToSocksAddr(target), pkt, C.SOCKS)

	tunnel.AddPacket(adapter)
}

func allocUDP(size int) []byte {
	return pool.Get(size)
}

func recycleUDP(payload []byte) {
	_ = pool.Put(payload)
}
