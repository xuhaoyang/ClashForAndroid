package server

import (
	"encoding/binary"
	"errors"
	"io"
	"net"
)

func readCommandPacket(client *net.UnixConn) ([]byte, error) {
	var packageSize uint32

	if err := binary.Read(client, binary.BigEndian, &packageSize); err != nil {
		return nil, err
	}

	buffer := make([]byte, packageSize)

	if readSize, err := io.ReadFull(client, buffer); err != nil || uint32(readSize) != packageSize {
		return nil, err
	}

	return buffer, nil
}

func writeCommandPacket(client *net.UnixConn, buffer []byte) error {
	if err := binary.Write(client, binary.BigEndian, uint32(len(buffer))); err != nil {
		return err
	}

	n, err := client.Write(buffer)
	if err != nil {
		return err
	}

	if n != len(buffer) {
		return errors.New("Write invalid data size")
	}

	return nil
}
