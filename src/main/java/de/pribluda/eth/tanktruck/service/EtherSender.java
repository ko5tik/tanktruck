package de.pribluda.eth.tanktruck.service;


import de.pribluda.eth.tanktruck.web3j.ExtendedAdmin;
import de.pribluda.eth.tanktruck.web3j.GasTx;
import de.pribluda.eth.tanktruck.web3j.TxCanceller;
import de.pribluda.eth.tanktruck.web3j.TxPoolContentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.JsonRpc2_0Admin;
import org.web3j.protocol.admin.methods.response.TxPoolContent;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.web3j.abi.Utils.typeMap;

/**
 * executes transaction, defined by the exchange path. this executor is tightly tied to  backend contract
 * ATTENTION!  in case of abi changes, be sure that you have  checked:
 * -  EXECUTE_ID matches actual ABI
 * - parameters are encoded correctly
 */
@Service
public class EtherSender {

    public static final String TIMEOUT_CAUSE = "Transaction receipt was not generated after";
    private final RawTransactionManager txManager;
    private final long chainId;
    private final static Logger logger = LoggerFactory.getLogger(EtherSender.class);

    private final Web3j web3j;
    private final ExtendedAdmin admin;
    private final Credentials credentials;

    private final TxCanceller canceller;

    public EtherSender(Web3j web3j,
                       Web3jService servce,
                       @Value("${web3j.node.chainId}") long chainId,
                       Credentials credentials,
                       TxCanceller canceller) {
        this.web3j = web3j;
        this.admin = new ExtendedAdmin(servce);
        this.credentials = credentials;
        this.canceller = canceller;
        logger.info("executor address: {}", credentials.getAddress());
        this.chainId = chainId;
        this.txManager = new RawTransactionManager(web3j, credentials, this.chainId);
    }


    public void sendFunds(String address, BigInteger maxValue) {
        try {
            try {

                Transfer transfer = new Transfer(web3j, txManager);

                TransactionReceipt transactionReceipt = transfer.sendFunds(address, new BigDecimal(maxValue), Convert.Unit.WEI).send();
                logger.info("TX: sent: {} {}", transactionReceipt.getTransactionHash(), transactionReceipt.getStatus());

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
}
