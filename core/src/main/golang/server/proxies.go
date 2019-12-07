package server

import (
	"context"
	"encoding/json"
	"errors"
	"net"
	"strconv"
	"time"

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

	var order int = 0

	for k, p := range proxies {
		inner, err := p.MarshalJSON()

		if err != nil {
			log.Errorln("MarshalJSON failure %s", err.Error())
			continue
		}

		mapping := map[string]interface{}{}
		json.Unmarshal(inner, &mapping)

		mapping["order"] = order

		order += 1

		root.Proxies[k] = mapping
	}

	data, err := json.Marshal(&root)
	if err != nil {
		log.Errorln("MarshJSON failure %s", err.Error())
	}

	writeCommandPacket(client, data)
}

func handleSetProxy(client *net.UnixConn) {
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

	if err := setProxySelect(Request.Key, Request.Value); err != nil {
		Response.Error = err.Error()
	}
}

func handleUrlTest(client *net.UnixConn) {
	var request struct {
		URL     string   `json:"url"`
		Timeout int      `json:"timeout"`
		Proxies []string `json:"proxies"`
	}

	request.Proxies = make([]string, 0)

	buf, err := readCommandPacket(client)
	if err != nil {
		return
	}

	if json.Unmarshal(buf, &request) != nil {
		return
	}

	type Response struct {
		Name  string `json:"name"`
		Delay int    `json:"delay"`
	}

	proxies := tunnel.Instance().Proxies()
	channel := make(chan *Response, len(request.Proxies))

	for _, p := range request.Proxies {
		go func(p string) {
			proxy := proxies[p]
			if proxy == nil {
				channel <- nil
				return
			}

			ctx, cancel := context.WithTimeout(context.Background(), time.Duration(request.Timeout)*time.Millisecond)

			defer cancel()

			delay, err := proxies[p].URLTest(ctx, request.URL)
			if err != nil {
				channel <- nil
				return
			}

			channel <- &Response{
				Name:  p,
				Delay: int(delay),
			}
		}(p)
	}

	for range request.Proxies {
		response := <-channel

		if response != nil {
			buf, _ := json.Marshal(&response)

			writeCommandPacket(client, buf)
		}
	}

	writeCommandPacket(client, nil)

	log.Infoln("URLTest exited")
}

func setProxySelect(name, selected string) error {
	proxies := tunnel.Instance().Proxies()

	p := proxies[name]

	if p == nil {
		return errors.New("Unknown proxy " + name)
	}

	proxy, ok := p.(*A.Proxy)
	if !ok {
		return errors.New("Invalid proxy " + name)
	}

	selector, ok := proxy.ProxyAdapter.(*A.Selector)
	if !ok {
		return errors.New("Not selector")
	}

	log.Infoln("Set selector " + name + " -> " + selected)

	if err := selector.Set(selected); err != nil {
		return err
	}

	return nil
}
