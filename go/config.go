package main

import (
	"fmt"
	"os"

	"github.com/pelletier/go-toml/v2"
)

type Config struct {
	Web3j      Web3jConfig      `toml:"web3j"`
	Maintainer MaintainerConfig `toml:"maintainer"`
}

type Web3jConfig struct {
	URL       string `toml:"url"`
	ChainID   int    `toml:"chainId"`
	ReaderKey string `toml:"readerKey"`
}

type MaintainerConfig struct {
	Contract        string `toml:"contract"`
	Executor        string `toml:"executor"`
	Bribe           int    `toml:"bribe"`
	PollingInterval string `toml:"pollingInterval"`
}

func LoadConfig(path string) (*Config, error) {
	file, err := os.Open(path)
	if err != nil {
		return nil, fmt.Errorf("failed to open config file: %w", err)
	}

	defer func(file *os.File) {
		err := file.Close()
		if err != nil {
			fmt.Println("failed to close config file:", err)
		}
	}(file)

	var cfg Config
	decoder := toml.NewDecoder(file)
	if err := decoder.Decode(&cfg); err != nil {
		return nil, fmt.Errorf("failed to decode config file: %w", err)
	}

	return &cfg, nil
}
