package tun

import (
	"fmt"
	"strconv"

	"github.com/Dreamacro/clash/dns"
	"github.com/Dreamacro/clash/log"
	T "github.com/Dreamacro/clash/proxy/tun"
)

type handler struct {
	tunAdapter *T.TunAdapter
}

const dnsRedirectAddr = "0.0.0.0"

var (
	instance *handler
)

// StartTunProxy - start
func StartTunProxy(fd, mtu int) error {
	StopTunProxy()

	adapter, err := T.NewTunProxy("fd://" + strconv.Itoa(fd) + "?mtu=" + strconv.Itoa(mtu))
	if err != nil {
		return err
	}

	instance = &handler{
		tunAdapter: &adapter,
	}

	ResetDnsRedirect()

	fmt.Println("Android tun started")

	return nil
}

// StopTunProxy - stop
func StopTunProxy() {
	if instance != nil {
		(*instance.tunAdapter).Close()
		instance = nil
	}
}

func ResetDnsRedirect() {
	if instance == nil {
		return
	}

	resolver := dns.DefaultResolver
	if resolver != nil {
		err := (*instance.tunAdapter).ReCreateDNSServer(resolver, dnsRedirectAddr+":53")
		if err != nil {
			log.Errorln("Can't create DNSServer on tun: %v", err)
		}
	} else {
		(*instance.tunAdapter).ReCreateDNSServer(nil, "")
	}
}
