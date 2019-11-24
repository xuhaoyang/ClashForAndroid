package event

import "errors"

var (
	handleSend  func([]byte)
	handleClose func()
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
