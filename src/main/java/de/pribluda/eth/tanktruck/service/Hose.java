package de.pribluda.eth.tanktruck.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@Service
public class Hose {

    private final Credentials credentials;
    Logger logger = LoggerFactory.getLogger(Hose.class);

    private final List<String> addresses;
    private final BigInteger minValue;
    private final BigInteger sendValue;
    private final Web3j web3j;
    private final EtherSender sender;

    public Hose(@Value("${tanktruck.addressList}") List<String> addresses,
                @Value("${tanktruck.minAmount}") BigInteger minValue,
                @Value("${tanktruck.sendAmount}") BigInteger sendValue,
                Web3j web3j,
                Credentials credentials,
                EtherSender sender) {
        this.addresses = addresses;
        this.minValue = minValue;
        this.sendValue = sendValue;
        this.web3j = web3j;
        this.credentials = credentials;

        this.sender = sender;
    }

    @Scheduled(fixedRateString = "${tanktruck.checkInterval}")
    public void pump() {
        logger.info("scheduled status check");

        for (String address : addresses) {
            logger.info("checking status of {}", address);
            balanceCheck(address);
        }
    }

    private boolean balanceCheck(String address) {

        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            String result = ethGetBalance.getResult();
            logger.info("balance of {} is {}", address, result);

            BigInteger balanceAmount = Numeric.decodeQuantity(result);
            if (balanceAmount.compareTo(minValue) < 0) {
                logger.info("filling  {}  up", address);

                sender.sendFunds(address, sendValue);
                return true;
            }
        } catch (IOException e) {
            logger.error("failed to poll block receipts: {}", e.getMessage());
        }
        return false;
    }
}
