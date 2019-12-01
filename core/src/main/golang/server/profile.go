package server

import (
	"encoding/json"
	"net"

	A "github.com/Dreamacro/clash/adapters/outbound"
	"github.com/Dreamacro/clash/log"
	"github.com/Dreamacro/clash/tunnel"
	"github.com/kr328/cfa/profile"
)

func handleProfileDefault(client *net.UnixConn) {
	profile.LoadDefault()

	log.Infoln("Profile default loaded")
}

func handleProfileReload(client *net.UnixConn) {
	packet, err := readCommandPacket(client)
	if err != nil {
		log.Errorln("Read profile payload failure, %s", err.Error())
		return
	}

	var payload struct {
		Path     string            `json:"path"`
		Selected map[string]string `json:"selected"`
	}

	var response struct {
		Error           string   `json:"error"`
		InvalidSelected []string `json:"invalidSelected"`
	}

	response.InvalidSelected = make([]string, 0)

	defer func() {
		buf, _ := json.Marshal(&response)

		writeCommandPacket(client, buf)
	}()

	err = json.Unmarshal(packet, &payload)
	if err != nil {
		log.Errorln("Parse profile payload failure, %s", err.Error())
		return
	}

	err = profile.LoadFromFile(payload.Path)

	if err != nil {
		response.Error = err.Error()
		return
	}

	proxies := tunnel.Instance().Proxies()

	for k, v := range payload.Selected {
		p := proxies[k]

		if p == nil {
			response.InvalidSelected = append(response.InvalidSelected, k)
			continue
		}

		proxy, ok := p.(*A.Proxy)
		if !ok {
			response.InvalidSelected = append(response.InvalidSelected, k)
			continue
		}

		selector, ok := proxy.ProxyAdapter.(*A.Selector)
		if !ok {
			response.InvalidSelected = append(response.InvalidSelected, k)
			continue
		}

		log.Infoln("Set selector " + k + " -> " + v)

		selector.Set(v)
	}

	log.Infoln("Profile " + payload.Path + " loaded")
}
