package tunnel

import (
	C "github.com/Dreamacro/clash/constant"
	"github.com/Dreamacro/clash/tunnel/statistic"
)

func CloseAllConnections() {
	for _, c := range statistic.DefaultManager.Snapshot().Connections {
		_ = c.Close()
	}
}

func closeMatch(filter func(conn C.Conn) bool) {
	for _, c := range statistic.DefaultManager.Snapshot().Connections {
		if cc, ok := c.(C.Conn); ok {
			if filter(cc) {
				_ = cc.Close()
			}
		}
	}
}

func closeConnByGroup(name string) {
	closeMatch(func(conn C.Conn) bool {
		for _, c := range conn.Chains() {
			if c == name {
				return true
			}
		}

		return false
	})
}
