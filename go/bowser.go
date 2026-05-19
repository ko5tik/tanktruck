package main

import (
	"context"
	"crypto/ecdsa"
	"encoding/hex"
	"fmt"
	"log"
	"math/big"
	"strings"

	"github.com/ethereum/go-ethereum"
	"github.com/ethereum/go-ethereum/accounts/abi"
	"github.com/ethereum/go-ethereum/accounts/abi/bind"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/crypto"
	"github.com/ethereum/go-ethereum/ethclient"
	"github.com/robfig/cron/v3"
)

// relevant part of ABI
const tanktruckABI = `[
    {
      "inputs": [],
      "name": "needResupply",
      "outputs": [
        {
          "internalType": "bool",
          "name": "",
          "type": "bool"
        }
      ],
      "stateMutability": "view",
      "type": "function"
    },
    {
      "inputs": [],
      "name": "resupply",
      "outputs": [],
      "stateMutability": "nonpayable",
      "type": "function"
    }
  ]`

type JsonError interface {
	Error() string
	ErrorCode() int
	ErrorData() interface{}
}
type Bowser struct {
	config          *Config
	client          *ethclient.Client
	privateKey      *ecdsa.PrivateKey
	publicKey       *ecdsa.PublicKey
	fromAddress     common.Address
	contractAddress common.Address
	abi             abi.ABI
}

func NewBowser(cfg *Config) (*Bowser, error) {
	parsedABI, err := abi.JSON(strings.NewReader(tanktruckABI))
	if err != nil {
		return nil, fmt.Errorf("failed to parse ABI: %w", err)
	}
	return &Bowser{
		config: cfg,
		abi:    parsedABI,
	}, nil
}

func (m *Bowser) Start() {

	log.Println("Connecting to Ethereum node...")

	// create client
	client, err := ethclient.Dial(m.config.Web3j.URL)
	m.client = client

	if err != nil {
		log.Fatalf("Error connection to  web3: %v", err)
		return
	}

	// private key to sign messages
	m.privateKey, err = crypto.HexToECDSA(m.config.Maintainer.Executor)
	if err != nil {
		log.Printf("Error loading private key: %v", err)
		return
	}

	m.publicKey = m.privateKey.Public().(*ecdsa.PublicKey)
	m.fromAddress = crypto.PubkeyToAddress(*m.publicKey)
	m.contractAddress = common.HexToAddress(m.config.Maintainer.Contract)

	// start periodic invocation
	c := cron.New()
	spec := m.config.Maintainer.PollingInterval

	log.Printf("Starting Tanktruck service with cron spec: %s", spec)

	_, err = c.AddFunc(spec, func() {
		m.Execute()
	})

	if err != nil {
		log.Fatalf("Error adding cron job: %v", err)
	}

	c.Start()
	// Keep the main goroutine running (or the goroutine that called Start)
	select {}
}

func (m *Bowser) Execute() error {
	// Check and cancel any stuck transactions before proceeding
	err := m.CancelStuckTransaction()
	if err != nil {
		log.Printf("Error during stuck transaction check/cancellation: %v", err)
	}

	log.Println("Checking if resupply is needed...")

	var needsResupply bool
	callData, err := m.abi.Pack("needResupply")
	if err != nil {
		return fmt.Errorf("failed to pack needResupply call: %w", err)
	}

	result, err := m.client.CallContract(context.Background(), ethereum.CallMsg{
		To:   &m.contractAddress,
		Data: callData,
	}, nil)
	if err != nil {
		return fmt.Errorf("failed to call needResupply: %w", err)
	}

	err = m.abi.UnpackIntoInterface(&needsResupply, "needResupply", result)
	if err != nil {
		return fmt.Errorf("failed to unpack needResupply result: %w", err)
	}

	if !needsResupply {
		log.Println("Resupply not needed. Skipping.")
		return nil
	}

	log.Println("Refueling...")
	nonce, err := m.client.PendingNonceAt(context.Background(), m.fromAddress)
	if err != nil {
		log.Printf("Error getting nonce: %v", err)
		return err
	}

	gasPrice, err := m.client.SuggestGasPrice(context.Background())
	if err != nil {
		log.Printf("Error suggesting gas price: %v", err)
		return err
	}

	// Apply bribe
	bribeMultiplier := big.NewInt(int64(100 + m.config.Maintainer.Bribe))
	gasPrice = new(big.Int).Div(new(big.Int).Mul(gasPrice, bribeMultiplier), big.NewInt(100))

	chainID := big.NewInt(int64(m.config.Web3j.ChainID))

	auth, err := bind.NewKeyedTransactorWithChainID(m.privateKey, chainID)
	if err != nil {
		log.Printf("Error creating transactor: %v", err)
		return err
	}
	auth.Nonce = big.NewInt(int64(nonce))
	auth.Value = big.NewInt(0) // in wei
	auth.GasPrice = gasPrice

	data, err := m.abi.Pack("resupply")
	if err != nil {
		return fmt.Errorf("failed to pack resupply call: %w", err)
	}

	// Estimate gas
	msg := ethereum.CallMsg{
		From:     m.fromAddress,
		To:       &m.contractAddress,
		GasPrice: gasPrice,
		Value:    auth.Value,
		Data:     data,
	}

	gasLimit, err := m.client.EstimateGas(context.Background(), msg)
	if err != nil {
		jsonErr, ok := err.(JsonError)

		if ok {
			var decodeString []byte
			if dataStr, isStr := jsonErr.ErrorData().(string); isStr {
				dataStr = strings.TrimPrefix(dataStr, "0x")
				decodeString, _ = hex.DecodeString(dataStr)
			}
			log.Printf("json error: %s, data: %v  %s", jsonErr.Error(), jsonErr.ErrorData(), decodeString)
		} else {
			log.Printf("non-json error: %s", err)
		}
		//log.Printf("Error estimating gas: %v", err)
		//  gas estimation failed, we return
		return err
	}

	// Apply safety margin - 50%
	gasLimit = gasLimit + (gasLimit / 2)
	auth.GasLimit = gasLimit

	tx := types.NewTx(&types.LegacyTx{
		Nonce:    nonce,
		To:       &m.contractAddress,
		Value:    auth.Value,
		Gas:      auth.GasLimit,
		GasPrice: auth.GasPrice,
		Data:     data,
	})

	signedTx, err := auth.Signer(auth.From, tx)
	if err != nil {
		log.Printf("Error signing transaction: %v", err)
		return err
	}

	err = m.client.SendTransaction(context.Background(), signedTx)
	if err != nil {
		log.Printf("Error sending transaction: %v", err)
		return err
	}

	log.Printf("Transaction sent: %s", signedTx.Hash().Hex())

	// Wait for receipt
	receipt, err := bind.WaitMined(context.Background(), m.client, signedTx)
	if err != nil {
		log.Printf("Error waiting for transaction mining: %v", err)
		return err
	}

	if receipt.Status == types.ReceiptStatusSuccessful {
		log.Printf("Transaction successful: %s", signedTx.Hash().Hex())
	} else {
		log.Printf("Transaction failed: %s status: %v", signedTx.Hash().Hex(), receipt.Status)
	}

	return nil
}

func (m *Bowser) CancelStuckTransaction() error {
	log.Println("Checking for stuck transactions...")
	ctx := context.Background()

	pendingNonce, err := m.client.PendingNonceAt(ctx, m.fromAddress)
	if err != nil {
		return fmt.Errorf("failed to get pending nonce: %w", err)
	}

	nonce, err := m.client.NonceAt(ctx, m.fromAddress, nil)
	if err != nil {
		return fmt.Errorf("failed to get confirmed nonce: %w", err)
	}

	if pendingNonce > nonce {
		log.Printf("Found stuck transaction(s). Confirmed nonce: %d, Pending nonce: %d. Cancelling transaction %d...", nonce, pendingNonce, nonce)

		gasPrice, err := m.client.SuggestGasPrice(ctx)
		if err != nil {
			return fmt.Errorf("failed to suggest gas price: %w", err)
		}

		// Apply a significant boost to ensure cancellation (150% + bribe)
		bribeMultiplier := big.NewInt(int64(150 + m.config.Maintainer.Bribe))
		gasPrice = new(big.Int).Div(new(big.Int).Mul(gasPrice, bribeMultiplier), big.NewInt(100))

		chainID := big.NewInt(int64(m.config.Web3j.ChainID))

		auth, err := bind.NewKeyedTransactorWithChainID(m.privateKey, chainID)
		if err != nil {
			return fmt.Errorf("error creating transactor: %w", err)
		}
		auth.Nonce = big.NewInt(int64(nonce))
		auth.Value = big.NewInt(0)
		auth.GasLimit = 21000 // standard gas limit for a simple ETH transfer
		auth.GasPrice = gasPrice

		tx := types.NewTx(&types.LegacyTx{
			Nonce:    nonce,
			To:       &m.fromAddress,
			Value:    auth.Value,
			Gas:      auth.GasLimit,
			GasPrice: auth.GasPrice,
			Data:     nil,
		})

		signedTx, err := auth.Signer(auth.From, tx)
		if err != nil {
			return fmt.Errorf("error signing cancellation transaction: %w", err)
		}

		err = m.client.SendTransaction(ctx, signedTx)
		if err != nil {
			return fmt.Errorf("error sending cancellation transaction: %w", err)
		}

		log.Printf("Cancellation transaction sent: %s", signedTx.Hash().Hex())

		// Wait for receipt
		receipt, err := bind.WaitMined(ctx, m.client, signedTx)
		if err != nil {
			return fmt.Errorf("error waiting for cancellation transaction mining: %w", err)
		}

		if receipt.Status == types.ReceiptStatusSuccessful {
			log.Printf("Cancellation transaction successful: %s", signedTx.Hash().Hex())
		} else {
			log.Printf("Cancellation transaction failed: %s status: %v", signedTx.Hash().Hex(), receipt.Status)
		}
	} else {
		log.Println("No stuck transactions found.")
	}

	return nil
}
