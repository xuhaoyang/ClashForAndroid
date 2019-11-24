package event

import "github.com/Dreamacro/clash/log"

import "encoding/json"

import "encoding/binary"

var logExit chan int = nil

func StartLogEvent() {
	if logExit != nil {
		return
	}

	go func() {
		logExit = make(chan int)
		sub := log.Subscribe()

		defer log.UnSubscribe(sub)
		defer func() {
			close(logExit)
			logExit = nil
		}()

		for {
			select {
			case data := <-sub:
				log := data.(*log.Event)

				if log == nil {
					continue
				}

				var message struct {
					Type    int    `json:"type"`
					Message string `json:"message"`
				}

				message.Type = levelToInt(log.LogLevel)
				message.Message = log.Payload

				s, _ := json.Marshal(&message)

				var prefix [4]byte

				binary.BigEndian.PutUint32(prefix[:], EventLog)

				send(append(prefix[:], s[:]...))

			case <-logExit:
				return
			}
		}
	}()
}

func StopLogEvent() {
	if logExit == nil {
		return
	}

	logExit <- 0
}

func levelToInt(level log.LogLevel) int {
	switch level {
	case log.DEBUG:
		return 1
	case log.INFO:
		return 2
	case log.WARNING:
		return 3
	case log.ERROR:
		return 4
	}

	return 1
}
