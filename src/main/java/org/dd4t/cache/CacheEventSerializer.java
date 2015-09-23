package org.dd4t.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tridion.cache.CacheEvent;

public class CacheEventSerializer {
    private static ObjectMapper objectMapper = null;
    public static String serialize(CacheEvent cacheEvent) throws JsonProcessingException {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper.writeValueAsString(cacheEvent);
    }
}
