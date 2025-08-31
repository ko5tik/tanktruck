package de.pribluda.eth.tanktruck.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Async;

@Configuration
public class Web3jConfiguration {
    Logger logger = LoggerFactory.getLogger(Web3jConfiguration.class);

    @Value("${web3j.node.url}")
    private String nodeUrl;
    @Value("${web3j.node.pollingInterval}")
    private long pollingInterval;

    @Value("${tanktruck.executorKey}")
    private String executorKey;


    @Bean
    public Web3j web3j(HttpService webSocketService) {
        logger.info("connecting to url: {}", nodeUrl);
        return Web3j.build(webSocketService, pollingInterval, Async.defaultExecutorService());
    }



    @Bean
    public HttpService getHttpService() {
        return new HttpService(nodeUrl);
    }

    /**
     * credentials to read from the contracts. needs to be a valid key,
     * NEVER user secure and valuable key here!!!!!
     *
     * @return
     */
    @Bean
    public Credentials credentials() {
        return Credentials.create(executorKey);
    }

}
