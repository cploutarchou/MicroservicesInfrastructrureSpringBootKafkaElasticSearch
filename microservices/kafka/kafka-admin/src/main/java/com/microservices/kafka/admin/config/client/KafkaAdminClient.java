package com.microservices.kafka.admin.config.client;

import com.microservices.config.KafkaConfigData;
import com.microservices.config.RetryConfigData;
import org.apache.kafka.clients.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaAdminClient {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaAdminClient.class);

    private final KafkaConfigData kafkaConfigData;

    private final RetryConfigData retryConfigData;

    private final AdminClient adminClient;

    private final RetryTemplate retryTemplate;


    private KafkaAdminClient(KafkaConfigData config,
                             RetryConfigData retryConfigData,
                             AdminClient client,
                             RetryTemplate template) {

        this.kafkaConfigData = config;
        this.retryConfigData = retryConfigData;
        this.adminClient = client;
        this.retryTemplate = template;
    }


}
