package main

import (
	"fmt"
	"os"
	"os/signal"
	"syscall"

	"github.com/Dreamacro/clash/constant"

	"github.com/kr328/clash/profile"
	"github.com/kr328/clash/server"
)

func main() {
	if len(os.Args) != 2 {
		fmt.Println("Invalid argument")
		return
	}

	fmt.Println("[CLASH] PID =", int(os.Getpid()))

	if cwd, err := os.Getwd(); err == nil {
		constant.SetHomeDir(cwd)
	} else {
		return
	}

	profile.LoadDefault()

	server.Start(os.Args[1])

	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, syscall.SIGINT, syscall.SIGTERM)
	<-sigCh

	return
}
