package org.dd4t.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tridion.cache.CacheEvent;

import java.io.IOException;

public class CacheEventSerializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String serialize(final CacheEvent cacheEvent) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(cacheEvent);
    }
    public static CacheEvent deserialize(final String s) throws JsonProcessingException, IOException {
        return OBJECT_MAPPER.readValue(s, CacheEvent.class);
    }
}
