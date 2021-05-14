package tun

import "github.com/Dreamacro/clash/log"

type logger struct{}

func (l *logger) D(format string, args ...interface{}) {
	log.Debugln(format, args...)
}

func (l *logger) I(format string, args ...interface{}) {
	log.Infoln(format, args...)
}

func (l *logger) W(format string, args ...interface{}) {
	log.Warnln(format, args...)
}

func (l *logger) E(format string, args ...interface{}) {
	log.Errorln(format, args...)
}
