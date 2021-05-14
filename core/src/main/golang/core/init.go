package core

import (
	"errors"
	"net"
	"syscall"

	"cfa/blob"

	"cfa/app"
	"cfa/platform"

	"github.com/Dreamacro/clash/component/process"
	"github.com/Dreamacro/clash/log"

	"github.com/Dreamacro/clash/component/dialer"
	"github.com/Dreamacro/clash/component/mmdb"
	"github.com/Dreamacro/clash/constant"
)

var errBlocked = errors.New("blocked")

func Init(home, versionName string, platformVersion int) {
	mmdb.LoadFromBytes(blob.GeoipDatabase)
	constant.SetHomeDir(home)
	app.ApplyVersionName(versionName)
	app.ApplyPlatformVersion(platformVersion)

	process.DefaultPackageNameResolver = func(metadata *constant.Metadata) (string, error) {
		src, dst := metadata.RawSrcAddr, metadata.RawDstAddr

		if src == nil || dst == nil {
			return "", process.ErrInvalidNetwork
		}

		uid := app.QuerySocketUid(metadata.RawSrcAddr, metadata.RawDstAddr)
		pkg := app.QueryAppByUid(uid)

		log.Debugln("[PKG] %s --> %s by %d[%s]", metadata.SourceAddress(), metadata.RemoteAddress(), uid, pkg)

		return pkg, nil
	}

	dialer.DialerHook = func(dialer *net.Dialer) error {
		dialer.Control = func(network, address string, c syscall.RawConn) error {
			return c.Control(func(fd uintptr) {
				app.MarkSocket(int(fd))
			})
		}

		return nil
	}
	dialer.ListenPacketHook = func(lc *net.ListenConfig, address string) (string, error) {
		lc.Control = func(network, address string, c syscall.RawConn) error {
			return c.Control(func(fd uintptr) {
				app.MarkSocket(int(fd))
			})
		}

		if platform.ShouldBlockConnection() {
			return "", errBlocked
		}

		return address, nil
	}

	dialer.DialHook = func(dialer *net.Dialer, network string, ip net.IP) error {
		if platform.ShouldBlockConnection() {
			return errBlocked
		}

		return nil
	}
}
