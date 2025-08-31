package de.pribluda.eth.tanktruck.web3j;


import de.pribluda.eth.tanktruck.service.EtherSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * service class to cancel pending transactions
 */
@Service
public class TxCanceller {

    private final RawTransactionManager txManager;
    private final long chainId;
    private final static Logger logger = LoggerFactory.getLogger(EtherSender.class);

    private final Web3j web3j;
    private final ExtendedAdmin admin;
    private final Credentials credentials;

    public TxCanceller(Web3jService servce,
                       @Value("${web3j.node.chainId}") long chainId,
                       Web3j web3j,
                       Credentials credentials) {
        this.chainId = chainId;
        this.web3j = web3j;
        this.credentials = credentials;
        this.admin = new ExtendedAdmin(servce);
        this.txManager = new RawTransactionManager(web3j, credentials, this.chainId);

    }

    /**
     * prepare and send cancelling transaction
     *
     * @throws IOException can produce IO exception
     */
    public void sendCancellationTx() throws IOException {
        // peek in txpool.
        TxPoolContentBase poolContent = admin.txPoolContentBase().send();


        //  extract transactions
        List<GasTx> allBase =
                poolContent.getResult().getBaseFee()
                        .values().stream().map(Map::values).flatMap(Collection::stream)
                        .toList();

        List<GasTx> baseFee = allBase.stream().filter(tx -> tx.getFrom().equalsIgnoreCase(credentials.getAddress()))
                .toList();
        logger.debug("pending tx count: {}", baseFee.size());

        //  extract transactions
        List<GasTx> allPending =
                poolContent.getResult().getPending()
                        .values().stream().map(Map::values).flatMap(Collection::stream)
                        .toList();

        List<GasTx> pending =
                allPending.stream().filter(tx -> tx.getFrom().equalsIgnoreCase(credentials.getAddress()))
                        .toList();

        logger.debug("pending tx count: {}", pending.size());

        List<GasTx> allQueued = poolContent.getResult().getQueued()
                .values().stream().map(Map::values).flatMap(Collection::stream)
                .toList();

        List<GasTx> queued = allQueued.stream()
                .filter(tx -> tx.getFrom().equalsIgnoreCase(credentials.getAddress()))
                .toList();

        logger.debug("queued tx count: {}", pending.size());


        ArrayList<GasTx> allTx = new ArrayList<>(pending);
        allTx.addAll(queued);
        allTx.addAll(baseFee);

        allTx.sort(Comparator.comparing(GasTx::getNonce));


        for (GasTx tx : allTx) {
            logger.debug("stuck  tx: nonce: {}  gas: {}   max fee: {} max prio fee {} ", tx.getNonce(), tx.getGas(), tx.getMaxFeePerGas(), tx.getMaxPriorityFeePerGas());
            BigInteger adjustedMaxPrio = tx.getMaxPriorityFeePerGas().multiply(BigInteger.valueOf(130)).divide(BigInteger.valueOf(100));
            BigInteger adjustedMaxFee = tx.getMaxFeePerGas().multiply(BigInteger.valueOf(130)).divide(BigInteger.valueOf(100));
            RawTransaction rawTransaction =
                    RawTransaction.createTransaction(
                            chainId,
                            tx.getNonce(),
                            tx.getGas(),
                            credentials.getAddress(),
                            BigInteger.ZERO,
                            "",
                            adjustedMaxFee,
                            adjustedMaxFee
                    );

            logger.debug("cancelling tx: {} {} {} {} ", rawTransaction.getNonce(), rawTransaction.getGasLimit(), adjustedMaxFee, adjustedMaxPrio);
            EthSendTransaction ethSendTransaction = txManager.signAndSend(rawTransaction);
            logger.info("TX: cancelled: {} {}", rawTransaction.getNonce(), ethSendTransaction.getResult());
            Response.Error error = ethSendTransaction.getError();
            if (null != error) {
                logger.warn("produced error: {} {}", error.getCode(), error.getMessage());
            }
        }
    }


}
