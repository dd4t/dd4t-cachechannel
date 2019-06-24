package org.dd4t.cache;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tridion.cache.CacheEvent;

import java.io.IOException;

public class CacheEventCustomSerializer extends StdSerializer<CacheEvent> {

    public CacheEventCustomSerializer() {
        this(null);
    }

    public CacheEventCustomSerializer(Class<CacheEvent> vc) {
        super(vc);
    }


    @Override
    public void serialize(CacheEvent cacheEvent, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
        String keyPattern = "^(1:).*:.*";
        String key = (String) cacheEvent.getKey();

        if (key.matches(keyPattern)) {
            key = key.replaceFirst("^1:", "");
        }

        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("type", cacheEvent.getEventType());
        jsonGenerator.writeStringField("regionPath", cacheEvent.getRegionPath());
        jsonGenerator.writeStringField("key", key);
        jsonGenerator.writeEndObject();
    }
}
