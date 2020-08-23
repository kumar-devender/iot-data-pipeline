package com.relay42.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
@UtilityClass
public class ResourceUtil {
    public String readResource(String name) throws IOException {
        return IOUtils.toString(ClassLoader.getSystemResourceAsStream(name), Charset.defaultCharset());
    }
}
