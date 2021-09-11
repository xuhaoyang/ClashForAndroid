package delegate

import (
	"errors"
	"syscall"

	"cfa/blob"

	app2 "cfa/native/app"
	"cfa/native/platform"
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
	app2.ApplyVersionName(versionName)
	app2.ApplyPlatformVersion(platformVersion)

	process.DefaultPackageNameResolver = func(metadata *constant.Metadata) (string, error) {
		src, dst := metadata.RawSrcAddr, metadata.RawDstAddr

		if src == nil || dst == nil {
			return "", process.ErrInvalidNetwork
		}

		uid := app2.QuerySocketUid(metadata.RawSrcAddr, metadata.RawDstAddr)
		pkg := app2.QueryAppByUid(uid)

		log.Debugln("[PKG] %s --> %s by %d[%s]", metadata.SourceAddress(), metadata.RemoteAddress(), uid, pkg)

		return pkg, nil
	}

	dialer.DefaultSocketHook = func(network, address string, conn syscall.RawConn) error {
		if platform.ShouldBlockConnection() {
			return errBlocked
		}

		return conn.Control(func(fd uintptr) {
			app2.MarkSocket(int(fd))
		})
	}
}
