module cfa

go 1.16

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

replace github.com/Dreamacro/clash => ./core/foss

replace cfa/blob => ../../../build/intermediates/golang_blob
