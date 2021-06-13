package proxy

import (
	"bufio"
	"net"
	"net/http"
	"sync"

	"github.com/Dreamacro/clash/adapter/inbound"
	"github.com/Dreamacro/clash/log"
	"github.com/Dreamacro/clash/tunnel"
)

var listener *httpListener
var lock sync.Mutex

type httpListener struct {
	net.Listener

	closed bool
}

func Start(listen string) (listenAt string, err error) {
	lock.Lock()
	defer lock.Unlock()

	stopLocked()

	l, err := net.Listen("tcp", listen)
	if err != nil {
		log.Errorln("Listen HTTP proxy at: %s: %s", listen, err.Error())

		return
	}

	h := &httpListener{
		Listener: l,
		closed:   false,
	}

	listener = h

	go func() {
		for !h.closed {
			conn, err := h.Accept()
			if err != nil {
				log.Warnln("Accept connection: %s", err.Error())
				continue
			}

			_ = conn.(*net.TCPConn).SetKeepAlive(false)

			go h.handleConn(conn)
		}
	}()

	return h.Addr().String(), nil
}

func Stop() {
	lock.Lock()
	defer lock.Unlock()

	stopLocked()
}

func stopLocked() {
	if listener != nil {
		listener.closed = true
		_ = listener.Close()
	}

	listener = nil
}

func (l *httpListener) handleConn(conn net.Conn) {
	br := bufio.NewReader(conn)
	request, err := http.ReadRequest(br)

	if err != nil || request.URL.Host == "" {
		if err != nil {
			log.Warnln("[HTTP] Connection closed: %s", err.Error())
		} else {
			log.Warnln("[HTTP] Connection closed: unknown host")
		}

		_ = conn.Close()
		return
	}

	if request.Method == http.MethodConnect {
		_, err := conn.Write([]byte("HTTP/1.1 200 Connection established\r\n\r\n"))
		if err != nil {
			return
		}
		tunnel.Add(inbound.NewHTTPS(request, conn))
		return
	}

	tunnel.Add(inbound.NewHTTP(request, conn))
}
