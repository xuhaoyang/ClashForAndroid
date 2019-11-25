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
		Proxies map[string]interface{} `json:"proxies"`
	}

	root.Proxies = make(map[string]interface{})

	for k, p := range proxies {
		inner, err := p.MarshalJSON()

		if err != nil {
			log.Errorln("MarshalJSON failure %s", err.Error())
			continue
		}

		mapping := map[string]interface{}{}
		json.Unmarshal(inner, &mapping)
		root.Proxies[k] = mapping
	}

	data, err := json.Marshal(&root)
	if err != nil {
		log.Errorln("MarshJSON failure %s", err.Error())
	}

	writeCommandPacket(client, data)
}
