module premium

go 1.17

require cfa v0.0.0

require (
	cfa/blob v0.0.0 // indirect
	github.com/Dreamacro/clash v1.7.1 // indirect
	github.com/Dreamacro/go-shadowsocks2 v0.1.7 // indirect
	github.com/Kr328/tun2socket v0.0.0-20211116115317-bc437243ffbb // indirect
	github.com/avast/apkparser v0.0.0-20210223100516-186f320f9bfc // indirect
	github.com/avast/apkverifier v0.0.0-20210916093748-2146ff7c4b7f // indirect
	github.com/dlclark/regexp2 v1.4.0 // indirect
	github.com/gofrs/uuid v4.1.0+incompatible // indirect
	github.com/google/btree v1.0.1 // indirect
	github.com/gorilla/websocket v1.4.2 // indirect
	github.com/insomniacslk/dhcp v0.0.0-20211026125128-ad197bcd36fd // indirect
	github.com/klauspost/compress v1.11.13 // indirect
	github.com/kr/text v0.1.0 // indirect
	github.com/miekg/dns v1.1.43 // indirect
	github.com/oschwald/geoip2-golang v1.5.0 // indirect
	github.com/oschwald/maxminddb-golang v1.8.0 // indirect
	github.com/sirupsen/logrus v1.8.1 // indirect
	github.com/u-root/uio v0.0.0-20210528114334-82958018845c // indirect
	go.etcd.io/bbolt v1.3.6 // indirect
	go.starlark.net v0.0.0-20211013185944-b0039bd2cfe3 // indirect
	go.uber.org/atomic v1.9.0 // indirect
	go4.org/intern v0.0.0-20211027215823-ae77deb06f29 // indirect
	go4.org/unsafe/assume-no-moving-gc v0.0.0-20211027215541-db492cf91b37 // indirect
	golang.org/x/crypto v0.0.0-20210921155107-089bfa567519 // indirect
	golang.org/x/net v0.0.0-20211105192438-b53810dc28af // indirect
	golang.org/x/sync v0.0.0-20210220032951-036812b2e83c // indirect
	golang.org/x/sys v0.0.0-20211116061358-0a5406a5449c // indirect
	golang.org/x/text v0.3.6 // indirect
	golang.org/x/time v0.0.0-20191024005414-555d28b269f0 // indirect
	gopkg.in/check.v1 v1.0.0-20190902080502-41f04d3bba15 // indirect
	gopkg.in/yaml.v2 v2.4.0 // indirect
	gvisor.dev/gvisor v0.0.0-20211104052249-2de3450f76d6 // indirect
	inet.af/netaddr v0.0.0-20211027220019-c74959edd3b6 // indirect
)

replace cfa => ../../main/golang

replace github.com/Dreamacro/clash => ./clash

replace cfa/blob => ../../../build/intermediates/golang_blob
