package tunnel

import (
	"github.com/Dreamacro/clash/tunnel/statistic"
)

func ResetStatistic() {
	statistic.DefaultManager.ResetStatistic()
}

func Now() (up int64, down int64) {
	return statistic.DefaultManager.Now()
}

func Total() (up int64, down int64) {
	return statistic.DefaultManager.Total()
}
