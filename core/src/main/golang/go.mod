module cfa

go 1.17

require (
	cfa/blob v0.0.0 // local generated
	github.com/Dreamacro/clash v0.0.0 // local
	github.com/dlclark/regexp2 v1.4.0
	github.com/kr328/tun2socket-lwip v0.0.0-20210911023118-0b4947e2a9c1
	github.com/miekg/dns v1.1.43
	github.com/oschwald/geoip2-golang v1.5.0
	golang.org/x/sync v0.0.0-20210220032951-036812b2e83c
	gopkg.in/yaml.v2 v2.4.0
)

require (
	github.com/Dreamacro/go-shadowsocks2 v0.1.7 // indirect
	github.com/gofrs/uuid v4.0.0+incompatible // indirect
	github.com/gorilla/websocket v1.4.2 // indirect
	github.com/insomniacslk/dhcp v0.0.0-20210827173440-b95caade3eac // indirect
	github.com/oschwald/maxminddb-golang v1.8.0 // indirect
	github.com/sirupsen/logrus v1.8.1 // indirect
	github.com/u-root/uio v0.0.0-20210528114334-82958018845c // indirect
	go.uber.org/atomic v1.9.0 // indirect
	golang.org/x/crypto v0.0.0-20210817164053-32db794688a5 // indirect
	golang.org/x/net v0.0.0-20210903162142-ad29c8ab022f // indirect
	golang.org/x/sys v0.0.0-20210906170528-6f6e22806c34 // indirect
	golang.org/x/text v0.3.6 // indirect
)

replace github.com/Dreamacro/clash => ./core/foss

replace cfa/blob => ../../../build/intermediates/golang_blob
