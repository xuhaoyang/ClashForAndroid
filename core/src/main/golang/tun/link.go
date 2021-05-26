package tun

import "github.com/Dreamacro/clash/log"

func (a *adapter) rx() {
	log.Infoln("[ATUN] Device rx started")
	defer log.Infoln("[ATUN] Device rx exited")
	defer a.once.Do(a.stop)
	defer a.close()

	buf := make([]byte, a.mtu)

	for {
		n, err := a.device.Read(buf)
		if err != nil {
			return
		}

		_, _ = a.stack.Link().Write(buf[:n])
	}
}

func (a *adapter) tx() {
	log.Infoln("[ATUN] Device tx started")
	defer log.Infoln("[ATUN] Device tx exited")
	defer a.once.Do(a.stop)
	defer a.close()

	buf := make([]byte, a.mtu)

	for {
		n, err := a.stack.Link().Read(buf)
		if err != nil {
			return
		}

		_, _ = a.device.Write(buf[:n])
	}
}
