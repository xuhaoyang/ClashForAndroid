package tun

import (
	"net"
	"time"

	"github.com/google/netstack/tcpip/adapters/gonet"
	"github.com/google/netstack/tcpip/buffer"
	"github.com/google/netstack/tcpip/stack"
	"github.com/google/netstack/tcpip/transport/udp"
	"github.com/google/netstack/waiter"
)

const defaultTimeout = 5
const defaultDNS = "8.8.8.8"

type DnsRedirectEndpoint struct {
	stack        *stack.Stack
	udpForwarder *udp.Forwarder
	targetAddr   *net.UDPAddr
}

func NewDnsRedirect(s *stack.Stack) *DnsRedirectEndpoint {
	result := &DnsRedirectEndpoint{
		stack:        s,
		udpForwarder: nil,
		targetAddr:   &net.UDPAddr{IP: net.ParseIP("8.8.8.8"), Port: 53},
	}

	result.udpForwarder = udp.NewForwarder(s, func(request *udp.ForwarderRequest) {
		var wq waiter.Queue
		ep, err := request.CreateEndpoint(&wq)
		if err != nil {
			return
		}

		conn := gonet.NewConn(&wq, ep)

		targetConn, udpErr := net.DialUDP("udp", &net.UDPAddr{IP: net.IPv4zero, Port: 0}, result.targetAddr)
		if udpErr != nil {
			conn.Close()
			return
		}

		// send
		go func() {
			var buffer [128]byte

			defer targetConn.Close()
			defer conn.Close()

			for {
				conn.SetDeadline(time.Now().Add(defaultTimeout * time.Second))

				n, err := conn.Read(buffer[:])
				if err != nil {
					return
				}

				_, err = targetConn.Write(buffer[:n])
				if err != nil {
					return
				}
			}
		}()

		// recive
		go func() {
			var buffer [128]byte

			defer targetConn.Close()
			defer conn.Close()

			for {
				conn.SetDeadline(time.Now().Add(defaultTimeout * time.Second))

				n, err := targetConn.Read(buffer[:])
				if err != nil {
					return
				}

				_, err = conn.Write(buffer[:n])
				if err != nil {
					return
				}
			}
		}()
	})

	return result
}

func (d *DnsRedirectEndpoint) SetTargetAddress(addr *net.UDPAddr) {
	d.targetAddr = addr
}

func (d *DnsRedirectEndpoint) UniqueID() uint64 {
	return 999
}

func (d *DnsRedirectEndpoint) HandlePacket(r *stack.Route, id stack.TransportEndpointID, pkt buffer.VectorisedView) {
	d.udpForwarder.HandlePacket(r, id, nil, pkt)
}

func (d *DnsRedirectEndpoint) HandleControlPacket(id stack.TransportEndpointID, typ stack.ControlType, extra uint32, pkt buffer.VectorisedView) {
	// Unsupported
}

func (d *DnsRedirectEndpoint) Close() {
	// Unsupported
}

func (d *DnsRedirectEndpoint) Wait() {
	// Unsupported
}
