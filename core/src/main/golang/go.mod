module cfa

go 1.16

require (
	cfa/blob v0.0.0 // local generated
	github.com/Dreamacro/clash v0.0.0 // local
	github.com/dlclark/regexp2 v1.4.0
	github.com/kr328/tun2socket v0.0.0-20210412191540-3d56c47e2d99
	github.com/miekg/dns v1.1.42
	github.com/oschwald/geoip2-golang v1.5.0
	golang.org/x/sync v0.0.0-20210220032951-036812b2e83c
	gopkg.in/yaml.v2 v2.4.0
)

replace github.com/Dreamacro/clash => ./clash

replace cfa/blob => ../../../build/intermediates/golang_blob
