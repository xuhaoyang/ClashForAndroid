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

	// Redirect stderr to stdout
	os.Stderr = os.Stdout

	if cwd, err := os.Getwd(); err == nil {
		constant.SetHomeDir(cwd)
	} else {
		return
	}

	if err := server.Start(os.Args[1]); err != nil {
		fmt.Println("[CONTROLLER] ERROR={" + err.Error() + "}")
		return
	}

	fmt.Println("[PID]", os.Getpid())
	fmt.Println("[CONTROLLER] STARTED")

	profile.LoadDefault()

	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, syscall.SIGINT, syscall.SIGTERM)
	<-sigCh

	return
}
