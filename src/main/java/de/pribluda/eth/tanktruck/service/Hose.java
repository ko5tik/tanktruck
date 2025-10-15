package de.pribluda.eth.tanktruck.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.generated.contracts.ITanker;
import org.web3j.protocol.Web3j;

import org.web3j.tx.gas.DefaultGasProvider;

/**
 *  Hose - called periodically to determine whether refueling is necessary
 */
@Service
public class Hose {

    Logger logger = LoggerFactory.getLogger(Hose.class);


    private final Web3j web3j;
    private final EtherSender sender;
    private DefaultGasProvider defaultGasProvider;
    private final ITanker tanker;

    public Hose(@Value("${tanktruck.contract}") String tankerContract,
                Web3j web3j,
                Credentials credentials,
                EtherSender sender) {


        this.web3j = web3j;
        this.sender = sender;
        defaultGasProvider = new DefaultGasProvider();
        this.tanker = ITanker.load(tankerContract, web3j, credentials, defaultGasProvider);
    }

    @Scheduled(fixedRateString = "${tanktruck.checkInterval}")
    public void pump() {
        logger.info("scheduled status check....");

        // do we need to send?
        try {
            Boolean needsResupply = tanker.needResupply().send();
            if (needsResupply) {
                sender.sendFunds();
            }
        } catch (Exception e) {
            logger.error("failed to check tanker status: {}", e.getMessage());
        }
        logger.info("... done");
    }
}
