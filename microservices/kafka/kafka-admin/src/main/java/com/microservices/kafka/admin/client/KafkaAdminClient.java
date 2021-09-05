package com.microservices.kafka.admin.client;

import com.microservices.config.KafkaConfigData;
import com.microservices.config.RetryConfigData;
import com.microservices.kafka.admin.exception.KafkaClientException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
public class KafkaAdminClient {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaAdminClient.class);

    private final KafkaConfigData kafkaConfigData;

    private final RetryConfigData retryConfigData;

    private final AdminClient adminClient;

    private final RetryTemplate retryTemplate;

    private final WebClient webClient;


    private KafkaAdminClient(KafkaConfigData config, RetryConfigData retryConfigData, AdminClient client, RetryTemplate template, WebClient webClient) {
        this.kafkaConfigData = config;
        this.retryConfigData = retryConfigData;
        this.adminClient = client;
        this.retryTemplate = template;
        this.webClient = webClient;
    }


    public void createTopic() {
        CreateTopicsResult createTopicsResult;
        try {
            createTopicsResult = retryTemplate.execute(this::doCreateTopics);
        } catch (Throwable e) {
            throw new RuntimeException("Reached max number of retries for creating kafka topic(s)!");
        }
        checkTopicCreated();

    }


    private CreateTopicsResult doCreateTopics(RetryContext context) {

        List<String> topics = kafkaConfigData.getTopicNamesToCreate();
        LOG.info("Start creating {} topic(s), attempt {}", topics.size(), context.getRetryCount());
        List<NewTopic> kafkaTopics = topics.stream().map(topic -> new NewTopic(topic.trim(),
                kafkaConfigData.getNumOfPartitions(), kafkaConfigData.getReplicationFactor())).collect(Collectors.toList());

        return adminClient.createTopics(kafkaTopics);

    }

    public void checkTopicCreated() {
        Collection<TopicListing> topics = getTopics();
        int retryCount = 1;
        Integer maxRetries = retryConfigData.getMaxAttempts();
        int multiplier = retryConfigData.getMultiplier().intValue();
        Long sleepTimeMS = retryConfigData.getSleepTimeMs();
        for (String topic : kafkaConfigData.getTopicNamesToCreate()) {
            while (!isTopicCreated(topics, topic)) {
                checkMaxRetries(retryCount++, maxRetries);
                sleep(sleepTimeMS);
                sleepTimeMS *= multiplier;
                topics = getTopics();
            }

        }

    }

    public void checkSchemaRegistry() {
        int retryCount = 1;
        Integer maxRetries = retryConfigData.getMaxAttempts();
        int multiplier = retryConfigData.getMultiplier().intValue();
        Long sleepTimeMS = retryConfigData.getSleepTimeMs();
        while (!getSchemaRegistryStatus().is2xxSuccessful()) {
            checkMaxRetries(retryCount++, maxRetries);
            sleep(sleepTimeMS);
            sleepTimeMS *= multiplier;
        }
    }


    private HttpStatus getSchemaRegistryStatus() {
        try {
            return webClient
                    .method(HttpMethod.GET)
                    .uri(kafkaConfigData.getSchemaRegistryUrl())
                    .exchange()
                    .map(ClientResponse::statusCode)
                    .block();
        } catch (Exception e) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
    }

    private void sleep(Long sleepTimeMS) {
        try {
            Thread.sleep(sleepTimeMS);
        } catch (InterruptedException e) {
            throw new KafkaClientException("Error while sleeping during the waiting for new topics creation.");
        }
    }

    private void checkMaxRetries(int retry, Integer maxRetries) {
        if (retry > maxRetries) {
            throw new KafkaClientException("Reached max number of retries for reading kafka topic(s)");
        }
    }

    private Boolean isTopicCreated(Collection<TopicListing> topics, String topicName) {
        if (topicName == null) {
            return false;
        }
        return topics.stream().anyMatch(topic -> topic.name().trim().equals(topicName));
    }

    private Collection<TopicListing> getTopics() {
        Collection<TopicListing> topics;

        try {
            topics = retryTemplate.execute(this::executeGetTopics);
        } catch (Throwable e) {
            throw new KafkaClientException("Reached max number of retries for reading kafka topic(s)", e);
        }
        return topics;
    }


    private Collection<TopicListing> executeGetTopics(RetryContext retryContext) throws ExecutionException, InterruptedException {
        LOG.info("Start reading kafka topic {} , attempt {}", kafkaConfigData.getTopicNamesToCreate().toArray(), retryContext.getRetryCount());
        Collection<TopicListing> topics = adminClient.listTopics().listings().get();
        if (topics != null) {
            topics.forEach(topic -> LOG.debug("Topic name {} .", topic.name()));
        }
        return topics;
    }

}

