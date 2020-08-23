package com.relay42;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class FileReader {
    public ArrayList<Object> getDeviceData(String traceFile) throws IOException {
        ArrayList<Object> requestList = new ArrayList<>();
        try (InputStream stream = FileReader.class.getClassLoader().getResourceAsStream(traceFile)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String request;
            while ((request = br.readLine()) != null) {
                requestList.add(binary(request));
            }
            br.close();
        }
        return requestList;
    }

    public ChannelBuffer binary(String... data) {
        return binary(ByteOrder.BIG_ENDIAN, data);
    }

    public ChannelBuffer binary(ByteOrder endianness, String... data) {
        return ChannelBuffers.wrappedBuffer(endianness, DatatypeConverter.parseHexBinary(concatenateStrings(data)));
    }

    private String concatenateStrings(String... strings) {
        return String.join("", strings);
    }
}
