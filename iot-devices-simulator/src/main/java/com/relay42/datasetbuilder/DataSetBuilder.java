package com.relay42.datasetbuilder;

import java.util.List;

public interface DataSetBuilder {
    boolean isSupported(String device);

    List<Object> buildData(int deviceId);
}
