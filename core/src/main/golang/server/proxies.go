package server

import (
	"encoding/json"
	"net"

	"github.com/Dreamacro/clash/log"
	"github.com/Dreamacro/clash/tunnel"
)

func handleQueryProxies(client *net.UnixConn) {
	proxies := tunnel.Instance().Proxies()

	var root struct {
		proxies []map[string]interface{} `json:"proxies"`
	}

	root.proxies = make([]map[string]interface{}, len(proxies))

	for _, p := range proxies {
		inner, err := p.MarshalJSON()

		if err != nil {
			log.Warnln("MarshalJSON failure %s", err.Error())
			continue
		}

		mapping := map[string]interface{}{}
		json.Unmarshal(inner, &mapping)

		root.proxies = append(root.proxies, mapping)
	}

}
