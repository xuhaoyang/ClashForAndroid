package tun

import (
	"net"
	"strconv"

	"github.com/Dreamacro/clash/log"
	"github.com/Dreamacro/clash/proxy/tun"

	"github.com/google/netstack/tcpip"
	"github.com/google/netstack/tcpip/network/ipv4"
	"github.com/google/netstack/tcpip/stack"
	"github.com/google/netstack/tcpip/transport/udp"
)

type handler struct {
	tunAdapter  *tun.TunAdapter
	dnsRedirect *DnsRedirectEndpoint
}

var (
	instance        *handler
	dnsRedirectAddr *net.UDPAddr
)

// StartTunProxy - start
func StartTunProxy(fd, mtu int) error {
	StopTunProxy()

	adapter, err := tun.NewTunProxy("fd://" + strconv.Itoa(fd) + "?mtu=" + strconv.Itoa(mtu))
	if err != nil {
		return err
	}

	s := adapter.Stack()

	endpoint := NewDnsRedirect(s)

	id := &stack.TransportEndpointID{
		LocalAddress:  "",
		LocalPort:     53,
		RemotePort:    0,
		RemoteAddress: "",
	}

	if err := s.RegisterTransportEndpoint(1,
		[]tcpip.NetworkProtocolNumber{
			ipv4.ProtocolNumber,
		},
		udp.ProtocolNumber,
		*id,
		endpoint,
		true,
		1); err != nil {
		log.Errorln("Unable to set dns redirect" + err.String())
	}

	instance = &handler{
		tunAdapter:  &adapter,
		dnsRedirect: endpoint,
	}

	return nil
}

// StopTunProxy - stop
func StopTunProxy() {
	if instance != nil {
		(*instance.tunAdapter).Close()
		instance = nil
	}
}

// SetDNSRedirect - set dns
func SetDNSRedirect(addr *net.UDPAddr) {
	if instance == nil {
		return
	}

	if addr != nil {
		instance.dnsRedirect.SetTargetAddress(addr)
	} else {
		instance.dnsRedirect.SetDefaultTargetAddress()
	}
}
