package server

import (
	"net"
	"encoding/binary"
)

const (
	pingReply = 233
)

func handlePing(client *net.UnixConn) {
	binary.Write(client, binary.BigEndian, uint32(pingReply))
}