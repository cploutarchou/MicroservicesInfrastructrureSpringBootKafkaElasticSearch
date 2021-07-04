package com.microservices.twitter.to.kafka.service.runner.imp;

import com.microservices.twitter.to.kafka.service.config.TwitterToKafkaServiceConfigData;
import com.microservices.twitter.to.kafka.service.listener.TwitterKafkaStatusListener;
import com.microservices.twitter.to.kafka.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import javax.annotation.PreDestroy;
import java.util.Arrays;

@Component
public class TwitterKafkaStreamRunner implements StreamRunner {

    private final TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData;
    private final TwitterKafkaStatusListener twitterKafkaStatusListener;
    private TwitterStream twitterStream;
    private static final Logger Log = LoggerFactory.getLogger(TwitterKafkaStatusListener.class);

    public TwitterKafkaStreamRunner(TwitterToKafkaServiceConfigData configData, TwitterKafkaStatusListener statusListener) {
        this.twitterToKafkaServiceConfigData = configData;
        this.twitterKafkaStatusListener = statusListener;
    }


    @Override
    public void start() throws TwitterException {
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(twitterKafkaStatusListener);
        addFilter();
    }

    public void shutdown() throws TwitterException {
        if (twitterStream != null) {
            Log.info("Closing twitter stream");
            twitterStream.shutdown();
        }
    }

    @PreDestroy
    public void addFilter() {
        String[] keywords = twitterToKafkaServiceConfigData.getTwitterKeywords().toArray(new String[0]);
        FilterQuery filterQuery = new FilterQuery(keywords);
        twitterStream.filter(filterQuery);
        Log.info("Started Filtering stream for keywords {}", Arrays.toString(keywords));
    }
}
