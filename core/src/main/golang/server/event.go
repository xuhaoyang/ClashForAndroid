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

	defer close(trafficExit)
	defer ticker.Stop()

	go func() {
		for {
			var buf [4]byte

			_, err := client.Read(buf[:])

			if err != nil {
				trafficExit <- 0
				return
			}
		}
	}()

	for {
		select {
		case <-ticker.C:
			buf.Reset()

			var Packet struct {
				Up    int64 `json:"up"`
				Down  int64 `json:"down"`
				Total int64 `json:"total"`
			}

			up, down := traffic.Now()
			snapshot := traffic.Snapshot()

			Packet.Up = up
			Packet.Down = down
			Packet.Total = snapshot.DownloadTotal + snapshot.UploadTotal

			json.NewEncoder(buf).Encode(&Packet)

			writeCommandPacket(client, buf.Bytes())
		case <-trafficExit:
			return
		}
	}
}
