package app

import "strings"

var systemDns []string

func NotifyDnsChanged(dnsList string) {
	dns := strings.Split(dnsList, ",")

	systemDns = dns
}

func SystemDns() []string {
	return systemDns
}
