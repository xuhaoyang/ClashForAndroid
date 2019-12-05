package server

import (
	"encoding/json"
	"net"
	"strconv"

	"github.com/Dreamacro/clash/log"
	"github.com/Dreamacro/clash/proxy"
	"github.com/Dreamacro/clash/tunnel"

	A "github.com/Dreamacro/clash/adapters/outbound"
)

const (
	modeDirect = 1
	modeGlobal = 2
	modeRule   = 3
)

func handleQueryGeneral(client *net.UnixConn) {
	var payload struct {
		Ports struct {
			Http           int `json:"http"`
			Socks          int `json:"socks"`
			Redirect       int `json:"redirect"`
			RandomHttpPort int `json:"randomHttp"`
		} `json:"ports"`
		Mode int `json:"mode"`
	}

	mode := tunnel.Instance().Mode()
	ports := proxy.GetPorts()

	payload.Ports.Http = ports.Port
	payload.Ports.Socks = ports.SocksPort
	payload.Ports.Redirect = ports.RedirPort

	if httpListener != nil {
		addr := httpListener.Addr().String()

		_, port, _ := net.SplitHostPort(addr)
		p, _ := strconv.Atoi(port)

		payload.Ports.RandomHttpPort = p
	}

	switch mode {
	case tunnel.Direct:
		payload.Mode = modeDirect
		break
	case tunnel.Global:
		payload.Mode = modeGlobal
		break
	case tunnel.Rule:
		payload.Mode = modeRule
		break
	}

	buf, _ := json.Marshal(&payload)

	writeCommandPacket(client, buf)
}

func handleQueryProxies(client *net.UnixConn) {
	proxies := tunnel.Instance().Proxies()

	var root struct {
		Mode    string                 `json:"mode"`
		Proxies map[string]interface{} `json:"proxies"`
	}

	root.Mode = tunnel.Instance().Mode().String()
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

func handleSetProxy(client *net.UnixConn) {
	proxies := tunnel.Instance().Proxies()

	var Request struct {
		Key   string `json:"key"`
		Value string `json:"value"`
	}

	var Response struct {
		Error string `json:"error"`
	}

	defer func() {
		buf, _ := json.Marshal(&Response)

		writeCommandPacket(client, buf)
	}()

	buf, err := readCommandPacket(client)
	if err != nil {
		return
	}

	json.Unmarshal(buf, &Request)

	p := proxies[Request.Key]

	if p == nil {
		Response.Error = "Unknown proxy " + Request.Key
		return
	}

	proxy, ok := p.(*A.Proxy)
	if !ok {
		Response.Error = "Invalid proxy " + Request.Key
		return
	}

	selector, ok := proxy.ProxyAdapter.(*A.Selector)
	if !ok {
		Response.Error = "Not selector"
		return
	}

	log.Infoln("Set selector " + Request.Key + " -> " + Request.Value)

	if err := selector.Set(Request.Value); err != nil {
		Response.Error = err.Error()
	}
}
