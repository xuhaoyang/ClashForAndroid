package tunnel

import "github.com/Dreamacro/clash/adapters/provider"

func Suspend(s bool) {
	provider.Suspend(s)
}
