package config

var (
	defaultNameServers = []string{
		"223.5.5.5",
		"119.29.29.29",
		"8.8.8.8",
		"1.1.1.1",
	}
	defaultFakeIPFilter = []string{
		// Stun Services
		"+.stun.*.*",
		"+.stun.*.*.*",
		"+.stun.*.*.*.*",
		"+.stun.*.*.*.*.*",

		// Google Voices
		"lens.l.google.com",

		// Nintendo Switch
		"*.n.n.srv.nintendo.net",

		// PlayStation
		"+.stun.playstation.net",

		// XBox
		"xbox.*.*.microsoft.com",
		"*.*.xboxlive.com",

		// Microsoft
		"*.msftncsi.com",
		"*.msftconnecttest.com",

		// Bilibili CDN
		"*.mcdn.bilivideo.cn",
	}
	defaultFakeIPRange = "28.0.0.0/8"
)
