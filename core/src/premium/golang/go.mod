module premium

go 1.17

require cfa v0.0.0

require (
	cfa/blob v0.0.0 // indirect
	github.com/Dreamacro/clash v1.7.1 // indirect
	github.com/Dreamacro/go-shadowsocks2 v0.1.7 // indirect
	github.com/avast/apkparser v0.0.0-20210223100516-186f320f9bfc // indirect
	github.com/avast/apkverifier v0.0.0-20210916093748-2146ff7c4b7f // indirect
	github.com/dlclark/regexp2 v1.4.0 // indirect
	github.com/gofrs/uuid v4.0.0+incompatible // indirect
	github.com/google/btree v1.0.1 // indirect
	github.com/gorilla/websocket v1.4.2 // indirect
	github.com/insomniacslk/dhcp v0.0.0-20210827173440-b95caade3eac // indirect
	github.com/klauspost/compress v1.11.13 // indirect
	github.com/kr328/tun2socket-lwip v0.0.0-20211015022349-94b5374d46e5 // indirect
	github.com/miekg/dns v1.1.43 // indirect
	github.com/oschwald/geoip2-golang v1.5.0 // indirect
	github.com/oschwald/maxminddb-golang v1.8.0 // indirect
	github.com/sirupsen/logrus v1.8.1 // indirect
	github.com/u-root/uio v0.0.0-20210528114334-82958018845c // indirect
	go.etcd.io/bbolt v1.3.5 // indirect
	go.starlark.net v0.0.0-20210901212718-87f333178d59 // indirect
	go.uber.org/atomic v1.9.0 // indirect
	go4.org/intern v0.0.0-20210108033219-3eb7198706b2 // indirect
	go4.org/unsafe/assume-no-moving-gc v0.0.0-20201222180813-1025295fd063 // indirect
	golang.org/x/crypto v0.0.0-20210817164053-32db794688a5 // indirect
	golang.org/x/net v0.0.0-20210903162142-ad29c8ab022f // indirect
	golang.org/x/sync v0.0.0-20210220032951-036812b2e83c // indirect
	golang.org/x/sys v0.0.0-20210906170528-6f6e22806c34 // indirect
	golang.org/x/text v0.3.6 // indirect
	golang.org/x/time v0.0.0-20191024005414-555d28b269f0 // indirect
	gopkg.in/yaml.v2 v2.4.0 // indirect
	gvisor.dev/gvisor v0.0.0-20210904021812-0d58674c658a // indirect
	inet.af/netaddr v0.0.0-20210903134321-85fa6c94624e // indirect
)

replace cfa => ../../main/golang

replace github.com/Dreamacro/clash => ./clash

replace cfa/blob => ../../../build/intermediates/golang_blob
