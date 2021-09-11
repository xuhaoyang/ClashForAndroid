package app

import (
	"strings"

	"github.com/Dreamacro/clash/dns"
)

func NotifyDnsChanged(dnsList string) {
	dL := strings.Split(dnsList, ",")

	ns := make([]dns.NameServer, 0, len(dnsList))
	for _, d := range dL {
		ns = append(ns, dns.NameServer{Addr: d})
	}

	dns.UpdateSystemDNS(dL)
	dns.FlushCacheWithDefaultResolver()
}

