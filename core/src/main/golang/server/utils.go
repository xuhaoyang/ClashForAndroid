package server

import (
	"encoding/binary"
	"io"
	"net"
)

func readCommandPackage(client *net.UnixConn) ([]byte, error) {
	var packageSize int32

	if err := binary.Read(client, binary.BigEndian, &packageSize); err != nil {
		return nil, err
	}

	buffer := make([]byte, packageSize)

	if readSize, err := io.ReadFull(client, buffer); err != nil || int32(readSize) != packageSize {
		return nil, err
	}

	return buffer, nil
}
