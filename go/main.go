package main

import (
	"flag"
	"fmt"
	"log"
)

func main() {
	configPath := flag.String("config", "cfg/config.toml", "path to the configuration file")
	flag.Parse()

	fmt.Println("Tanktruck - provide gas for ETH accounts")
	fmt.Printf("Loading configuration from: %s\n", *configPath)

	cfg, err := LoadConfig(*configPath)
	if err != nil {
		log.Fatalf("Error loading config: %v", err)
	}

	fmt.Printf("Loaded config: %+v\n", cfg.Web3j)
	fmt.Printf("Backend contract: %+v\n", cfg.Maintainer.Contract)

	maintainer, err := NewBowser(cfg)
	if err != nil {
		log.Fatalf("Error starting maintainer: %v", err)
	}

	maintainer.Start()
}
