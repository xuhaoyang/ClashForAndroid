module premium

go 1.18

require cfa v0.0.0

require (
	cfa/blob v0.0.0 // indirect
	github.com/Dreamacro/clash v1.7.1 // indirect
	github.com/Dreamacro/go-shadowsocks2 v0.1.8 // indirect
	github.com/Kr328/tun2socket v0.0.0-20220414050025-d07c78d06d34 // indirect
	github.com/avast/apkparser v0.0.0-20210223100516-186f320f9bfc // indirect
	github.com/avast/apkverifier v0.0.0-20210916093748-2146ff7c4b7f // indirect
	github.com/cilium/ebpf v0.8.1 // indirect
	github.com/dlclark/regexp2 v1.4.0 // indirect
	github.com/florianl/go-tc v0.4.0 // indirect
	github.com/gofrs/uuid v4.2.0+incompatible // indirect
	github.com/google/btree v1.0.1 // indirect
	github.com/google/go-cmp v0.5.7 // indirect
	github.com/gorilla/websocket v1.5.0 // indirect
	github.com/insomniacslk/dhcp v0.0.0-20220405050111-12fbdcb11b41 // indirect
	github.com/josharian/native v1.0.0 // indirect
	github.com/klauspost/compress v1.11.13 // indirect
	github.com/mdlayher/netlink v1.6.0 // indirect
	github.com/mdlayher/socket v0.1.1 // indirect
	github.com/miekg/dns v1.1.48 // indirect
	github.com/openacid/low v0.1.21 // indirect
	github.com/oschwald/geoip2-golang v1.7.0 // indirect
	github.com/oschwald/maxminddb-golang v1.9.0 // indirect
	github.com/samber/lo v1.11.0 // indirect
	github.com/sirupsen/logrus v1.8.1 // indirect
	github.com/u-root/uio v0.0.0-20210528114334-82958018845c // indirect
	go.etcd.io/bbolt v1.3.6 // indirect
	go.starlark.net v0.0.0-20220328144851-d1966c6b9fcd // indirect
	go.uber.org/atomic v1.9.0 // indirect
	go4.org/intern v0.0.0-20211027215823-ae77deb06f29 // indirect
	go4.org/unsafe/assume-no-moving-gc v0.0.0-20211027215541-db492cf91b37 // indirect
	golang.org/x/crypto v0.0.0-20220408190544-5352b0902921 // indirect
	golang.org/x/exp v0.0.0-20220303212507-bbda1eaf7a17 // indirect
	golang.org/x/mod v0.6.0-dev.0.20211013180041-c96bc1413d57 // indirect
	golang.org/x/net v0.0.0-20220407224826-aac1ed45d8e3 // indirect
	golang.org/x/sync v0.0.0-20210220032951-036812b2e83c // indirect
	golang.org/x/sys v0.0.0-20220408201424-a24fb2fb8a0f // indirect
	golang.org/x/text v0.3.7 // indirect
	golang.org/x/time v0.0.0-20191024005414-555d28b269f0 // indirect
	golang.org/x/tools v0.1.9 // indirect
	golang.org/x/xerrors v0.0.0-20200804184101-5ec99f83aff1 // indirect
	gopkg.in/yaml.v2 v2.4.0 // indirect
	gvisor.dev/gvisor v0.0.0-20220311014831-b314d81fbac7 // indirect
	inet.af/netaddr v0.0.0-20211027220019-c74959edd3b6 // indirect
)

replace cfa => ../../main/golang

replace github.com/Dreamacro/clash => ./clash

replace cfa/blob => ../../../build/intermediates/golang_blob
