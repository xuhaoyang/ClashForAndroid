package server

import (
	"bytes"
	"encoding/json"
	"net"
	"time"

	"github.com/Dreamacro/clash/log"
	"github.com/Dreamacro/clash/tunnel"
)

func handlePullTrafficEvent(client *net.UnixConn) {
	trafficExit := make(chan int)
	ticker := time.NewTicker(time.Second)

	traffic := tunnel.DefaultManager
	buf := &bytes.Buffer{}

	defer ticker.Stop()

	go func() {
		for {
			var buf [4]byte

			_, err := client.Read(buf[:])

			if err != nil {
				close(trafficExit)
				return
			}
		}
	}()

	for {
		select {
		case <-ticker.C:
			buf.Reset()

			var Packet struct {
				Up   int64 `json:"up"`
				Down int64 `json:"down"`
			}

			up, down := traffic.Now()

			Packet.Up = up
			Packet.Down = down

			if json.NewEncoder(buf).Encode(&Packet) != nil {
				return
			}

			if writeCommandPacket(client, buf.Bytes()) != nil {
				return
			}
		case <-trafficExit:
			return
		}
	}
}

func handlePullBandwidthEvent(client *net.UnixConn) {
	bandWidthExit := make(chan int)
	ticker := time.NewTicker(time.Second)

	mgr := tunnel.DefaultManager
	buf := &bytes.Buffer{}

	defer ticker.Stop()

	go func() {
		for {
			var buf [4]byte

			_, err := client.Read(buf[:])

			if err != nil {
				close(bandWidthExit)
				return
			}
		}
	}()

	tick := func() error {
		buf.Reset()

		var Packet struct {
			Total int64 `json:"total"`
		}

		sp := mgr.Snapshot()

		Packet.Total = sp.DownloadTotal + sp.UploadTotal

		json.NewEncoder(buf).Encode(&Packet)

		return writeCommandPacket(client, buf.Bytes())
	}

	tick()

	for {
		select {
		case <-ticker.C:
			if tick() != nil {
				return
			}
		case <-bandWidthExit:
			return
		}
	}
}

func handlePullLogEvent(client *net.UnixConn) {
	logExit := make(chan int)

	subseribe := log.Subscribe()
	buf := &bytes.Buffer{}

	go func() {
		var buf [4]byte

		for {
			_, err := client.Read(buf[:])

			if err != nil {
				close(logExit)
				return
			}
		}
	}()

	for {
		select {
		case elm := <-subseribe:
			buf.Reset()
			msg := elm.(*log.Event)

			var payload struct {
				Level   int    `json:"level"`
				Messgae string `json:"message"`
			}

			switch msg.LogLevel {
			case log.DEBUG:
				payload.Level = 1
				break
			case log.INFO:
				payload.Level = 2
				break
			case log.WARNING:
				payload.Level = 3
				break
			case log.ERROR:
				payload.Level = 4
			}

			payload.Messgae = msg.Payload

			if err := json.NewEncoder(buf).Encode(&payload); err != nil {
				return
			}

			if writeCommandPacket(client, buf.Bytes()) != nil {
				return
			}
		case <-logExit:
			return
		}
	}
}
