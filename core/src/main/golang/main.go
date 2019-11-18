package main

import (
	"fmt"
	"os"
	"os/signal"
	"syscall"

	"github.com/Dreamacro/clash/constant"

	"github.com/kr328/clash/profile"
)

func main() int {
	if len(os.Args) != 1 {
		fmt.Println("Invalid argument")
		return -1
	}

	if cwd, err := os.Getwd(); err == nil {
		constant.SetHomeDir(cwd)
	} else {
		return -1
	}

	profile.LoadDefault()

	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, syscall.SIGINT, syscall.SIGTERM)
	<-sigCh

	return 0
}
