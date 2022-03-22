//go:build !premium

package tunnel

import "net"

var loopback = net.ParseIP("127.0.0.1")
