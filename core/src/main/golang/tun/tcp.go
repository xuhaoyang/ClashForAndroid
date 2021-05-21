package tun

import (
	"net"
	"strconv"

	C "github.com/Dreamacro/clash/constant"
	CTX "github.com/Dreamacro/clash/context"
	"github.com/Dreamacro/clash/tunnel"
)

func handleTCP(conn net.Conn, source *net.TCPAddr, target *net.TCPAddr) {
	metadata := &C.Metadata{
		NetWork:    C.TCP,
		Type:       C.SOCKS,
		SrcIP:      source.IP,
		DstIP:      target.IP,
		SrcPort:    strconv.Itoa(source.Port),
		DstPort:    strconv.Itoa(target.Port),
		AddrType:   C.AtypIPv4,
		Host:       "",
		RawSrcAddr: source,
		RawDstAddr: target,
	}

	tunnel.Add(CTX.NewConnContext(conn, metadata))
}
