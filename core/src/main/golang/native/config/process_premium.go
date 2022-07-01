//go:build premium

package config

import "github.com/Dreamacro/clash/config"

func patchTun(cfg *config.RawConfig, _ string) error {
	cfg.Tun.Enable = false

	return nil
}

func patchSniff(cfg *config.RawConfig, _ string) error {
	cfg.Experimental.SniffTLSSNI = true

	return nil
}