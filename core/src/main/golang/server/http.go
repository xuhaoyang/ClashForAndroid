package server

import "github.com/Dreamacro/clash/proxy/http"

var httpListener *http.HttpListener

func startRandomHttpPort() {
	listener, _ := http.NewHttpProxy("127.0.0.1:0")

	httpListener = listener
}
