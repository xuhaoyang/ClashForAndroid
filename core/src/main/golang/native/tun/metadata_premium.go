//go:build premium

package tun

import (
	"net"
	"net/netip"
	"strconv"

	C "github.com/Dreamacro/clash/constant"
)

func createMetadata(lAddr, rAddr *net.TCPAddr) *C.Metadata {
	srcAddr, _ := netip.AddrFromSlice(lAddr.IP)
	dstAddr, _ := netip.AddrFromSlice(rAddr.IP)

	return &C.Metadata{
		NetWork:    C.TCP,
		Type:       C.SOCKS5,
		SrcIP:      srcAddr,
		DstIP:      dstAddr,
		SrcPort:    strconv.Itoa(lAddr.Port),
		DstPort:    strconv.Itoa(rAddr.Port),
		AddrType:   C.AtypIPv4,
		Host:       "",
		RawSrcAddr: lAddr,
		RawDstAddr: rAddr,
	}
}
