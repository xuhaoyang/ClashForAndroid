package server

import (
	"bytes"
	"encoding/json"
	"net"
	"time"

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

			json.NewEncoder(buf).Encode(&Packet)

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

	for {
		select {
		case <-ticker.C:
			buf.Reset()

			var Packet struct {
				Total int64 `json:"total"`
			}

			sp := mgr.Snapshot()

			Packet.Total = sp.DownloadTotal + sp.UploadTotal

			json.NewEncoder(buf).Encode(&Packet)

			if writeCommandPacket(client, buf.Bytes()) != nil {
				return
			}
		}
	}
}
