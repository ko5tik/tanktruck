package de.pribluda.eth.tanktruck.service;


import de.pribluda.eth.tanktruck.web3j.TxCanceller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.DefaultFunctionEncoder;
import org.web3j.crypto.Credentials;
import org.web3j.generated.contracts.ITanker;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

import java.math.BigInteger;
import java.util.*;

import static org.web3j.abi.Utils.typeMap;

/**
 *  wrapper around backend contract execution.   Handles gas estimation,  execution and cancellation if request becomes stale
 *
 */
@Service
public class EtherSender {

    public static final String TIMEOUT_CAUSE = "Transaction receipt was not generated after";
    private final RawTransactionManager txManager;
    private final long chainId;
    private final static Logger logger = LoggerFactory.getLogger(EtherSender.class);
    private final Web3j web3j;
    private final static DefaultFunctionEncoder defaultFunctionEncoder = new DefaultFunctionEncoder();
    private final TxCanceller canceller;
    private final String encodedResullplyCall;
    private final String tankerContract;
    private final BigInteger bribe;
    private final Credentials credentials;

    public EtherSender(@Value("${tanktruck.contract}") String tankerContract,
                       Web3j web3j,
                       @Value("${web3j.node.chainId}") long chainId,
                       @Value("${tanktruck.bribe}") long bribe,
                       Credentials credentials,
                       TxCanceller canceller) {
        this.web3j = web3j;
        this.canceller = canceller;
        this.credentials = credentials;

        this.chainId = chainId;
        this.txManager = new RawTransactionManager(web3j, credentials, this.chainId);

        // since transaction call doew not have any params, we may as well cache encodd value
        ITanker iTanker = ITanker.load(tankerContract, web3j, credentials, new DefaultGasProvider());
        encodedResullplyCall = iTanker.resupply().encodeFunctionCall();
        this.tankerContract = tankerContract;
        this.bribe = BigInteger.valueOf(bribe);
    }


    public void sendFunds() {
        try {
            try {

                //  get current gas price
                EthBlock block = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
                BigInteger baseFeePerGas = block.getBlock().getBaseFeePerGas();
                BigInteger premiumGasPrice = baseFeePerGas.multiply(BigInteger.valueOf(100).add(bribe)).divide(BigInteger.valueOf(100));
                BigInteger totalGasPrice = baseFeePerGas.add(premiumGasPrice);


                //  estimate gas consumption
                EthEstimateGas gas = web3j.ethEstimateGas(createTx(totalGasPrice)).send();

                // error means we cannot proceed.
                if (gas.hasError()) {
                    logger.error("failed to estimate gas: {}", gas.getError().getMessage());
                    return;
                }

                //  add 30% for safety
                BigInteger maxGas = gas.getAmountUsed().multiply(BigInteger.valueOf(130)).divide(BigInteger.valueOf(100));


                // send transaction
                EthSendTransaction sentTransaction = txManager.sendEIP1559Transaction(
                        chainId,
                        premiumGasPrice,
                        totalGasPrice,
                        maxGas,
                        tankerContract,
                        encodedResullplyCall,
                        BigInteger.ZERO);


                String transactionHash = sentTransaction.getTransactionHash();

                logger.info("TX: send: {} {} {}", sentTransaction.getResult(), transactionHash, sentTransaction.getError());

                if (null != sentTransaction.getError()) {
                    logger.error("TX failed: {} {}", transactionHash, sentTransaction.getError().getMessage());
                    canceller.sendCancellationTx();
                    return;
                }

                TransactionReceipt receipt = new PollingTransactionReceiptProcessor(web3j, 1000, 50).waitForTransactionReceipt(transactionHash);
                logger.debug("TX receipt: {}", receipt);
                if (receipt.getStatus().equals("0x1")) {
                    logger.info("TX: successful: {} {}", transactionHash, receipt.getStatus());
                } else {
                    logger.info("TX: fail: {} {}", transactionHash, receipt.getStatus());
                    canceller.sendCancellationTx();
                }

            } catch (TransactionException e) {
                //  we consider this transaction stale now, cancel it
                if (e.getMessage().startsWith(TIMEOUT_CAUSE)) {
                    logger.error("TX stale, cancelling it {}", e.getMessage());
                    // Get the pending transaction's nonce
                    canceller.sendCancellationTx();
                }
            }
        } catch (Exception e) {
            logger.error("failed to send funds: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    private Transaction createTx(BigInteger totalGasPrice) {
        return Transaction.createFunctionCallTransaction(credentials.getAddress(),
                null,
                totalGasPrice,
                null,
                tankerContract,
                encodedResullplyCall
        );
    }

}
