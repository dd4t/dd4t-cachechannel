package org.dd4t.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tridion.cache.CacheEvent;

public class CacheEventSerializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String serialize(final CacheEvent cacheEvent) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(cacheEvent);
    }
}
