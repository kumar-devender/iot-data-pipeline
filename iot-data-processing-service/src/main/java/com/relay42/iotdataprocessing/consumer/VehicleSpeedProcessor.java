package com.relay42.iotdataprocessing.consumer;

import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.relay42.dto.ConnectedDevicesView;
import com.relay42.protocol.h02.H02DTO;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaMapWithStateDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.springframework.stereotype.Component;
import scala.Tuple2;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.datastax.spark.connector.japi.CassandraStreamingJavaUtil.javaFunctions;

@Component
public class VehicleSpeedProcessor {
    private Map<String, String> columnNameMappings = new HashMap<>();

    @PostConstruct
    private void init() {
        columnNameMappings.put("protocol", "protocol");
        columnNameMappings.put("total", "total");
    }


    public void processTotalConnectedDevices(JavaDStream<H02DTO> stream) {
        /*stream.foreachRDD(x -> {
            x.collect().stream().forEach(n -> System.out.println("Consuming total devices : " + n));
        });*/

        JavaPairDStream<String, Long> protocolPairDStream = stream.mapToPair(item -> new Tuple2<>(item.getProtocol(), 1L))
                .reduceByKey((v1, v2) -> v1 + v2);

        StateSpec<String, Long, Long, Tuple2<String, Long>> stateSpec =
                StateSpec.function(totalDevicesOfTypeFunc).timeout(Durations.seconds(3600));

        JavaMapWithStateDStream<String, Long, Long, Tuple2<String, Long>> countDStreamWithStatePair =
                protocolPairDStream.mapWithState(stateSpec);//maintain state for one hour

        JavaDStream<Tuple2<String, Long>> countDStream = countDStreamWithStatePair.map(tuple2 -> tuple2);
        JavaDStream<ConnectedDevicesView> connectedDevicesViewStream = countDStream.map(totalConnectedDevicesViewFunc);

        // call CassandraStreamingJavaUtil function to save in DB
        javaFunctions(connectedDevicesViewStream)
                .writerBuilder(
                        "iot_data_store",
                        "connected_devices",
                        CassandraJavaUtil.mapToRow(ConnectedDevicesView.class, columnNameMappings)
                )
                .withConstantTTL(120)//keeping data for 2 minutes
                .saveToCassandra();
    }

    private static Function<Tuple2<String, Long>, ConnectedDevicesView> totalConnectedDevicesViewFunc = (tuple -> {
        ConnectedDevicesView connectedDevicesView = new ConnectedDevicesView();
        connectedDevicesView.setProtocol(tuple._1);
        connectedDevicesView.setTotal(tuple._2);
        //connectedDevicesView.setTimeStamp(LocalDateTime.now());
        return connectedDevicesView;
    });

    private static final Function3<String, Optional<Long>, State<Long>, Tuple2<String, Long>> totalDevicesOfTypeFunc = (key, currentAvg, state) -> {
        Long objectOption = currentAvg.get();
        objectOption = objectOption == null ? 0l : objectOption;
        long totalSum = objectOption + (state.exists() ? state.get() : 0);
        Tuple2<String, Long> total = new Tuple2<>(key, totalSum);
        state.update(totalSum);
        return total;
    };
}
