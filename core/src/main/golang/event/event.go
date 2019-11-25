package event

import "errors"

var (
	handleSend  func([]byte)
	handleClose func()
)

const (
	EventClose        uint32 = 0
	EventLog          uint32 = 1
	EventProxyChanged uint32 = 2
	EventTraffic      uint32 = 3
)

func SetHandlers(send func([]byte), close func()) error {
	if handleSend != nil || handleClose != nil {
		return errors.New("Already registered")
	}

	handleSend = send
	handleClose = close

	return nil
}

func ClearHandlers() {
	handleSend = nil
	handleClose = nil
}

func send(buf []byte) {
	if handleSend != nil {
		handleSend(buf)
	}
}
