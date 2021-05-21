package tun

import (
	"encoding/binary"
	"io"
	"net"
	"time"

	"github.com/Dreamacro/clash/component/resolver"
	"github.com/kr328/tun2socket/bridge"

	D "github.com/miekg/dns"
)

const defaultDnsReadTimeout = time.Second * 30

func shouldHijackDns(dns net.IP, target net.IP, targetPort int) bool {
	if targetPort != 53 {
		return false
	}

	return net.IPv4zero.Equal(dns) || target.Equal(dns)
}

func hijackUDPDns(pkt []byte, lAddr, rAddr net.Addr, udp bridge.UDP) {
	go func() {
		answer, err := relayDnsPacket(pkt)

		if err != nil {
			return
		}

		_, _ = udp.WriteTo(answer, lAddr, rAddr)

		recycleUDP(pkt)
	}()
}

func hijackTCPDns(conn net.Conn) {
	go func() {
		defer conn.Close()

		for {
			if err := conn.SetReadDeadline(time.Now().Add(defaultDnsReadTimeout)); err != nil {
				return
			}

			var length uint16
			if binary.Read(conn, binary.BigEndian, &length) != nil {
				return
			}

			data := make([]byte, length)

			_, err := io.ReadFull(conn, data)
			if err != nil {
				return
			}

			rb, err := relayDnsPacket(data)
			if err != nil {
				continue
			}

			if binary.Write(conn, binary.BigEndian, uint16(len(rb))) != nil {
				return
			}

			if _, err := conn.Write(rb); err != nil {
				return
			}
		}
	}()
}

func relayDnsPacket(payload []byte) ([]byte, error) {
	msg := &D.Msg{}
	if err := msg.Unpack(payload); err != nil {
		return nil, err
	}

	r, err := resolver.ServeMsg(msg)
	if err != nil {
		return nil, err
	}

	for _, ans := range r.Answer {
		header := ans.Header()

		if header.Class == D.ClassINET && (header.Rrtype == D.TypeA || header.Rrtype == D.TypeAAAA) {
			header.Ttl = 1
		}
	}

	r.SetRcode(msg, r.Rcode)

	return r.Pack()
}
