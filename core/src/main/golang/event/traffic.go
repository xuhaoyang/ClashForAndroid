package event

import (
	"bytes"
	"time"

	"encoding/binary"
	"encoding/json"

	"github.com/Dreamacro/clash/tunnel"
)

var trafficExit chan int = nil

func StartTrafficEvent() {
	if trafficExit != nil {
		return
	}

	go func() {
		trafficExit = make(chan int)
		ticker := time.NewTicker(time.Second)
		buf := &bytes.Buffer{}
		traffic := tunnel.DefaultManager
		var prefix [4]byte

		binary.BigEndian.PutUint32(prefix[:], EventTraffic)

		for {
			select {
			case <-ticker.C:
				buf.Reset()

				binary.Write(buf, binary.BigEndian, EventTraffic)
				binary.Write(buf, binary.BigEndian, uint32(0))

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

				data := buf.Bytes()

				binary.BigEndian.PutUint32(data[4:], uint32(len(data)-8))

				handleSend(data)
			case <-trafficExit:
				ticker.Stop()

				close(trafficExit)
				trafficExit = nil

				return
			}
		}

	}()
}

func StopTrafficEvent() {
	if trafficExit == nil {
		return
	}

	trafficExit <- 0
}
