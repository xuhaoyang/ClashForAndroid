package server

import (
	"net"

	"golang.org/x/sys/unix"

	"github.com/Dreamacro/clash/log"

	"github.com/kr328/clash/tun"
)

func handleTunStart(client *net.UnixConn) {
	buffer, err := readCommandPackage(client)
	if err != nil {
		log.Warnln("Read packet from unix socket failure %s", err.Error())
		return
	}

	msg, err := unix.ParseSocketControlMessage(buffer)
	if err != nil || len(msg) != 1 {
		log.Warnln("Parse control package failure %s", err.Error())
		return
	}

	fds, err := unix.ParseUnixRights(&msg[0])
	if err != nil {
		log.Warnln("Parse control package failure %s", err.Error())
	}

	tun.NewTunProxy(fds[0])
}

func handleTunStop(client *net.UnixConn) {

}
