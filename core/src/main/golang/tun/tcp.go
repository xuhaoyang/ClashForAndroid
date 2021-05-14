package tun

import (
	"net"
	"strconv"

	"github.com/kr328/tun2socket/binding"

	C "github.com/Dreamacro/clash/constant"
	"github.com/Dreamacro/clash/context"
	"github.com/Dreamacro/clash/tunnel"
)

func handleTCP(conn net.Conn, endpoint *binding.Endpoint) {
	src := &net.TCPAddr{
		IP:   endpoint.Source.IP,
		Port: int(endpoint.Source.Port),
		Zone: "",
	}
	dst := &net.TCPAddr{
		IP:   endpoint.Target.IP,
		Port: int(endpoint.Target.Port),
		Zone: "",
	}

	metadata := &C.Metadata{
		NetWork:    C.TCP,
		Type:       C.SOCKS,
		SrcIP:      src.IP,
		DstIP:      dst.IP,
		SrcPort:    strconv.Itoa(src.Port),
		DstPort:    strconv.Itoa(dst.Port),
		AddrType:   C.AtypIPv4,
		Host:       "",
		RawSrcAddr: src,
		RawDstAddr: dst,
	}

	tunnel.Add(context.NewConnContext(conn, metadata))
}
