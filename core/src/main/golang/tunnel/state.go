package tunnel

import (
	"github.com/Dreamacro/clash/tunnel"
)

func QueryMode() string {
	return tunnel.Mode().String()
}
