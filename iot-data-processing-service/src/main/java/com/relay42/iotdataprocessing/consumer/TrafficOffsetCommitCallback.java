package com.relay42.iotdataprocessing.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;

import java.io.Serializable;
import java.util.Map;

@Slf4j
public class TrafficOffsetCommitCallback implements OffsetCommitCallback, Serializable {
    @Override
    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
        log.info("---------------------------------------------------");
        log.info(String.format("{0} | {1}", new Object[]{offsets, exception}));
        log.info("---------------------------------------------------");
    }
}