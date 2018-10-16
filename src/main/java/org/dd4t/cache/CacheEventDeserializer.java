package org.dd4t.cache;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.tridion.cache.CacheEvent;

import java.io.IOException;

public class CacheEventDeserializer extends StdDeserializer<CacheEvent> {
    public CacheEventDeserializer() {
        this(null);
    }

    public CacheEventDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CacheEvent deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        try {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String regionPath = node.get("regionPath").asText();
            String key = node.get("key").asText();
            int eventType = (Integer) node.get("type").numberValue();
            return new CacheEvent(regionPath, key, eventType);
        } catch (IOException ex) {
            return null;
        }
    }
}
