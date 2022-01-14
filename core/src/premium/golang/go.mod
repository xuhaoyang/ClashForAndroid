module premium

go 1.17

require cfa v0.0.0

require (
	cfa/blob v0.0.0 // indirect
	github.com/Dreamacro/clash v1.7.1 // indirect
	github.com/Dreamacro/go-shadowsocks2 v0.1.7 // indirect
	github.com/Kr328/tun2socket v0.0.0-20211231120722-962f339492e8 // indirect
	github.com/avast/apkparser v0.0.0-20210223100516-186f320f9bfc // indirect
	github.com/avast/apkverifier v0.0.0-20210916093748-2146ff7c4b7f // indirect
	github.com/dlclark/regexp2 v1.4.0 // indirect
	github.com/gofrs/uuid v4.2.0+incompatible // indirect
	github.com/google/btree v1.0.1 // indirect
	github.com/gorilla/websocket v1.4.2 // indirect
	github.com/insomniacslk/dhcp v0.0.0-20211214070828-5297eed8f489 // indirect
	github.com/klauspost/compress v1.11.13 // indirect
	github.com/kr/text v0.1.0 // indirect
	github.com/miekg/dns v1.1.45 // indirect
	github.com/oschwald/geoip2-golang v1.5.0 // indirect
	github.com/oschwald/maxminddb-golang v1.8.0 // indirect
	github.com/sirupsen/logrus v1.8.1 // indirect
	github.com/u-root/uio v0.0.0-20210528114334-82958018845c // indirect
	go.etcd.io/bbolt v1.3.6 // indirect
	go.starlark.net v0.0.0-20211203141949-70c0e40ae128 // indirect
	go.uber.org/atomic v1.9.0 // indirect
	go4.org/intern v0.0.0-20211027215823-ae77deb06f29 // indirect
	go4.org/unsafe/assume-no-moving-gc v0.0.0-20211027215541-db492cf91b37 // indirect
	golang.org/x/crypto v0.0.0-20211215153901-e495a2d5b3d3 // indirect
	golang.org/x/mod v0.4.2 // indirect
	golang.org/x/net v0.0.0-20211216030914-fe4d6282115f // indirect
	golang.org/x/sync v0.0.0-20210220032951-036812b2e83c // indirect
	golang.org/x/sys v0.0.0-20220111092808-5a964db01320 // indirect
	golang.org/x/text v0.3.6 // indirect
	golang.org/x/time v0.0.0-20191024005414-555d28b269f0 // indirect
	golang.org/x/tools v0.1.6-0.20210726203631-07bc1bf47fb2 // indirect
	golang.org/x/xerrors v0.0.0-20200804184101-5ec99f83aff1 // indirect
	gopkg.in/check.v1 v1.0.0-20190902080502-41f04d3bba15 // indirect
	gopkg.in/yaml.v2 v2.4.0 // indirect
	gvisor.dev/gvisor v0.0.0-20220101053744-1d5a116c5b1d // indirect
	inet.af/netaddr v0.0.0-20211027220019-c74959edd3b6 // indirect
)

replace cfa => ../../main/golang

replace github.com/Dreamacro/clash => ./clash

replace cfa/blob => ../../../build/intermediates/golang_blob
