//go:build !premium

package tun

import (
	"net"
	"strconv"

	C "github.com/Dreamacro/clash/constant"
)

func createMetadata(lAddr, rAddr *net.TCPAddr) *C.Metadata {
	return &C.Metadata{
		NetWork:    C.TCP,
		Type:       C.SOCKS5,
		SrcIP:      lAddr.IP,
		DstIP:      rAddr.IP,
		SrcPort:    strconv.Itoa(lAddr.Port),
		DstPort:    strconv.Itoa(rAddr.Port),
		AddrType:   C.AtypIPv4,
		Host:       "",
		RawSrcAddr: lAddr,
		RawDstAddr: rAddr,
	}
}
