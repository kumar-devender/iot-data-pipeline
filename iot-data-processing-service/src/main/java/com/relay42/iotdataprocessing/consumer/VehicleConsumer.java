package com.relay42.iotdataprocessing.consumer;

import com.relay42.iotdataprocessing.config.ConfigProperties;
import com.relay42.iotdataprocessing.config.KafkaConfig;
import com.relay42.protocol.h02.H02DTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.sql.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class VehicleConsumer implements Serializable {

    @Autowired
    private transient ConfigProperties properties;
    @Autowired
    private transient JavaStreamingContext jsc;
    @Autowired
    private transient SparkSession sparkSession;
    @Autowired
    private transient KafkaConfig kafkaConfig;
    @Autowired
    private transient VehicleSpeedProcessor connectedDeviceProcessor;

    public void startStreamProcessing() throws InterruptedException {
        Map<TopicPartition, Long> lastOffSet = buildLatestOffSet();
        JavaInputDStream<ConsumerRecord<String, H02DTO>> directKafkaStream = buildKafkaStream(lastOffSet);
        //JavaDStream<H02DTO> values = directKafkaStream.map(ConsumerRecord::value);
        JavaDStream<H02DTO> transformedStream = directKafkaStream.transform(VehicleConsumer::getEnhancedObjWithKafkaInfo);
        //startStreamProcessing(transformedStream);
        commitOffset(directKafkaStream);
        startStreamProcessing(transformedStream);
        jsc.start();
        jsc.awaitTermination();
    }

    private void commitOffset(JavaInputDStream<ConsumerRecord<String, H02DTO>> directKafkaStream) {
        directKafkaStream.foreachRDD((JavaRDD<ConsumerRecord<String, H02DTO>> trafficRdd) -> {
            if (!trafficRdd.isEmpty()) {
                OffsetRange[] offsetRanges = ((HasOffsetRanges) trafficRdd.rdd()).offsetRanges();
                CanCommitOffsets canCommitOffsets = (CanCommitOffsets) directKafkaStream.inputDStream();
                canCommitOffsets.commitAsync(offsetRanges, new TrafficOffsetCommitCallback());
            }
        });
    }

    private Map<TopicPartition, Long> buildLatestOffSet() {
        Map<TopicPartition, Long> collect = Collections.emptyMap();
        try {
            Dataset<Row> parquet = sparkSession.read()
                    .parquet(properties.get("relay42.iotdataprocessing.config.hdfs.parquetLocation"));
            //.parquet("hdfs://0.0.0.0:8020/iot-pipeline/iot-data-parque");
            //.parquet(environment.getProperty("relay42.iotdataprocessing.config.hdfs.parquetLocation"));

            parquet.createTempView("vehicle-tracking");
            Dataset<Row> sql = parquet.sqlContext()
                    .sql("select max(untilOffset) as untilOffset, topic, kafkaPartition from traffic group by topic, kafkaPartition");

            collect = sql.javaRDD()
                    .collect()
                    .stream()
                    .map(row -> {
                        TopicPartition topicPartition = new TopicPartition(row.getString(row.fieldIndex("topic")), row.getInt(row.fieldIndex("kafkaPartition")));
                        Tuple2<TopicPartition, Long> key = new Tuple2<>(
                                topicPartition,
                                Long.valueOf(row.getString(row.fieldIndex("untilOffset")))
                        );
                        return key;
                    })
                    .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
        } catch (Exception e) {
            return collect;
        }
        return collect;
    }

    private JavaInputDStream<ConsumerRecord<String, H02DTO>> buildKafkaStream(
            Map<TopicPartition, Long> fromOffsets
    ) {
        Map<String, Object> props = kafkaConfig.getProps();
        List<String> topicSet = Arrays.asList(properties.get("relay42.iotdataprocessing.config.kafka.topic").split(","));
        System.out.println("TOPICS >>>>>>>  " + topicSet);

        ConsumerStrategy<String, H02DTO> subscribe;
        if (fromOffsets.isEmpty()) {
            subscribe = ConsumerStrategies.Subscribe(topicSet, props);
        } else {
            subscribe = ConsumerStrategies.Subscribe(topicSet, props, fromOffsets);
        }

        //subscribe = ConsumerStrategies.Subscribe(topicSet, props);
        return KafkaUtils.createDirectStream(
                jsc,
                LocationStrategies.PreferConsistent(),
                subscribe
        );
    }

    private void appendDataToHDFS(SparkSession sql, JavaDStream<H02DTO> nonFilteredIotDataStream) {
        nonFilteredIotDataStream.foreachRDD(rdd -> {
            if (!rdd.isEmpty()) {
                Dataset<Row> dataFrame = sql.createDataFrame(rdd, H02DTO.class);
                Dataset<Row> dfStore = dataFrame.selectExpr(
                        "lat", "lon", "fuel", "speed",
                        "metaData.fromOffset as fromOffset",
                        "metaData.untilOffset as untilOffset",
                        "metaData.kafkaPartition as kafkaPartition",
                        "metaData.topic as topic"
                );
                dfStore.write()
                        .partitionBy("topic", "kafkaPartition")
                        .mode(SaveMode.Append)
                        .parquet(properties.get("relay42.iotdataprocessing.config.hdfs.parquetLocation"));
            }
        });
    }

    private void startStreamProcessing(JavaDStream<H02DTO> nonFilteredIotDataStream) {
        //appendDataToHDFS(sparkSession, nonFilteredIotDataStream);
        //JavaDStream<H02DTO> filteredIotDataStream = getVehicleNotProcessed(nonFilteredIotDataStream);
        //cache stream as it is used in many computation
        //filteredIotDataStream.cache();
        //connectedDeviceProcessor.processTotalConnectedDevices(filteredIotDataStream);
        connectedDeviceProcessor.processTotalConnectedDevices(nonFilteredIotDataStream);
    }

    private JavaDStream<H02DTO> getVehicleNotProcessed(JavaDStream<H02DTO> nonFilteredIotDataStream) {
        //We need filtered stream for total and traffic data calculation
        JavaPairDStream<Integer, H02DTO> iotDataPairStream = nonFilteredIotDataStream
                .mapToPair(iot -> new Tuple2<>(iot.getId(), iot))
                .reduceByKey((a, b) -> a);

        // Check vehicle Id is already processed
        JavaMapWithStateDStream<Integer, H02DTO, Boolean, Tuple2<H02DTO, Boolean>> iotDStreamWithStatePairs =
                iotDataPairStream
                        .mapWithState(
                                StateSpec.function(processedVehicleFunc).timeout(Durations.seconds(3600))
                        );//maintain state for one hour

        // Filter processed vehicle ids and keep un-processed
        JavaDStream<Tuple2<H02DTO, Boolean>> filteredIotDStreams = iotDStreamWithStatePairs
                .filter(tuple -> tuple._2.equals(Boolean.FALSE));

        // Get stream of IoTdata
        return filteredIotDStreams.map(tuple -> tuple._1);
    }

    private final static Function3<Integer, Optional<H02DTO>, State<Boolean>, Tuple2<H02DTO, Boolean>> processedVehicleFunc = (String, iot, state) -> {
        Tuple2<H02DTO, Boolean> vehicle = new Tuple2<>(iot.get(), false);
        if (state.exists()) {
            vehicle = new Tuple2<>(iot.get(), true);
        } else {
            state.update(Boolean.TRUE);
        }
        return vehicle;
    };

    private static JavaRDD<H02DTO> getEnhancedObjWithKafkaInfo(JavaRDD<ConsumerRecord<String, H02DTO>> item) {
        OffsetRange[] offsetRanges = ((HasOffsetRanges) item.rdd()).offsetRanges();

        return item.mapPartitionsWithIndex((index, items) -> {
            Map<String, String> meta = new HashMap<String, String>() {{
                int partition = offsetRanges[index].partition();
                long from = offsetRanges[index].fromOffset();
                long until = offsetRanges[index].untilOffset();

                put("topic", offsetRanges[index].topic());
                put("fromOffset", "" + from);
                put("kafkaPartition", "" + partition);
                put("untilOffset", "" + until);
            }};
            List<H02DTO> list = new ArrayList<>();
            while (items.hasNext()) {
                ConsumerRecord<String, H02DTO> next = items.next();
                H02DTO dataItem = next.value();
                //dataItem.setMetaData(meta);
                list.add(dataItem);
            }
            return list.iterator();
        }, true);
    }
}
