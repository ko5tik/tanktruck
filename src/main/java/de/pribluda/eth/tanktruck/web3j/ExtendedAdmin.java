package de.pribluda.eth.tanktruck.web3j;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;

import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

public class ExtendedAdmin extends org.web3j.protocol.admin.JsonRpc2_0Admin{
    public ExtendedAdmin(Web3jService web3jService) {
        super(web3jService);
    }

    public ExtendedAdmin(Web3jService web3jService, long pollingInterval, ScheduledExecutorService scheduledExecutorService) {
        super(web3jService, pollingInterval, scheduledExecutorService);
    }


    public Request<?, TxPoolContentBase> txPoolContentBase() {
        return new Request<>(
                "txpool_content",
                Collections.<String>emptyList(),
                web3jService,
                TxPoolContentBase.class);
    }

    public Request<?, TxPoolGas> gasPoolContent() {
        return new Request<>(
                "txpool_content",
                Collections.<String>emptyList(),
                web3jService,
                TxPoolGas.class);
    }
}
