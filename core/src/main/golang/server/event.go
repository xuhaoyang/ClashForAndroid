package server

import (
	"encoding/binary"
	"github.com/kr328/clash/event"
	"net"
)

func handlePollEvent(client *net.UnixConn) {
	var control chan []byte = make(chan []byte, 10)

	defer close(control)
	defer event.ClearHandlers()

	var send func([]byte) = func(buf []byte) {
		control <- buf
	}

	var c func() = func() {
		close(control)
	}

	defer binary.Write(client, binary.BigEndian, event.EventClose)

	if err := event.SetHandlers(send, c); err != nil {
		return
	}

	for buf := range control {
		if buf == nil {
			return
		}

		writeCommandPacket(client, buf)
	}
}

func handleSetEventEnabled(client *net.UnixConn) {
	var eventType int
	var enabled int
	var err error

	err = binary.Read(client, binary.BigEndian, &eventType)
	if err != nil {
		return
	}

	err = binary.Read(client, binary.BigEndian, &enabled)
	if err != nil {
		return
	}

	switch eventType {
	case event.EventLog:
		event.StartLogEvent()
	}
}
