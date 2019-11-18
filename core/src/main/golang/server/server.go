package server

import (
	"encoding/binary"
	"net"

	"github.com/Dreamacro/clash/log"
)

const (
	commandTunStart = 1
	commandTunStop  = 2
)

var handlers map[int]func(*net.UnixConn) = make(map[int]func(*net.UnixConn))

func init() {
	handlers[commandTunStart] = handleTunStart // tun.go
	handlers[commandTunStop] = handleTunStop   // tun.go
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
	var command int32

	if err := binary.Read(client, binary.BigEndian, &command); err != nil {
		log.Warnln("Unix read command failure %s", err.Error())
	}

	handler := handlers[int(command)]
	if handler == nil {
		log.Warnln("Invalid command failure %d", command)
	}

	handler(client)
}
