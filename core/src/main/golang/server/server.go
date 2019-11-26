package server

import (
	"encoding/binary"
	"net"

	"github.com/Dreamacro/clash/log"
)

const (
	commandPing           uint32 = 0
	commandTunStart       uint32 = 1
	commandTunStop        uint32 = 2
	commandProfileDefault uint32 = 3
	commandProfileReload  uint32 = 4
	commandQueryProxies   uint32 = 5
	commandPullTraffic    uint32 = 6
	commandPullLog        uint32 = 7
)

var handlers map[uint32]func(*net.UnixConn) = make(map[uint32]func(*net.UnixConn))

func init() {
	handlers[commandPing] = handlePing                     // ping.go
	handlers[commandTunStart] = handleTunStart             // tun.go
	handlers[commandTunStop] = handleTunStop               // tun.go
	handlers[commandProfileDefault] = handleProfileDefault // profile.go
	handlers[commandProfileReload] = handleProfileReload   // profile.go
	handlers[commandQueryProxies] = handleQueryProxies     // proxies.go
	handlers[commandPullTraffic] = handlePullTrafficEvent  // event.go
	handlers[commandPullLog] = nil                         // event.go
}

// Start local control server
func Start(path string) error {
	address, _ := net.ResolveUnixAddr("unix", path)

	listener, err := net.ListenUnix("unix", address)
	if err != nil {
		return err
	}

	go func() {
		for {
			client, err := listener.AcceptUnix()
			if err != nil {
				log.Fatalln("Unix socket accept failure: %s", err.Error())
				listener.Close()
				client.Close()
				return
			}

			go handleConnection(client)
		}
	}()

	return nil
}

func handleConnection(client *net.UnixConn) {
	var command uint32

	if err := binary.Read(client, binary.BigEndian, &command); err != nil {
		log.Errorln("Unix read command failure %s", err.Error())
	}

	handler := handlers[command]
	if handler == nil {
		log.Errorln("Invalid command failure %d", command)
		client.Close()
	} else {
		handler(client)
	}
}
