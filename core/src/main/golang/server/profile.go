package server

import (
	"encoding/binary"
	"encoding/json"
	"net"

	"github.com/Dreamacro/clash/log"
	"github.com/kr328/clash/profile"
)

func handleProfileDefault(client *net.UnixConn) {
	profile.LoadDefault()

	log.Infoln("Profile default loaded")
}

func handleProfileReload(client *net.UnixConn) {
	packet, err := readCommandPacket(client)
	if err != nil {
		log.Errorln("Read profile payload failure, %s", err.Error())
		return
	}

	var payload struct {
		Path string `json:"path"`
	}

	err = json.Unmarshal(packet, &payload)
	if err != nil {
		log.Errorln("Parse profile payload failure, %s", err.Error())
		return
	}

	err = profile.LoadFromFile(payload.Path)

	if err != nil {
		var reply struct {
			Err string `json:"error"`
		}

		reply.Err = err.Error()

		buffer, _ := json.Marshal(&reply)

		writeCommandPacket(client, buffer)
	} else {
		binary.Write(client, binary.BigEndian, uint32(0))
		return
	}

	log.Infoln("Profile " + payload.Path + " loaded")
}
