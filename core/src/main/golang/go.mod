module github.com/kr328/cfa

go 1.13

require (
	github.com/Dreamacro/clash v0.0.0 // local
	github.com/google/netstack v0.0.0-20191031000057-4787376a6744
	golang.org/x/sys v0.0.0-20190924154521-2837fb4f24fe
)

replace github.com/Dreamacro/clash v0.0.0 => ./clash
