package org.dd4t.cache;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tridion.cache.CacheEvent;

import java.io.IOException;

public class CacheEventSerializer extends StdSerializer<CacheEvent> {

    public CacheEventSerializer() {
        this(null);
    }

    public CacheEventSerializer(Class<CacheEvent> vc) {
        super(vc);
    }


    @Override
    public void serialize(CacheEvent cacheEvent, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
        jsonGenerator.writeStartObject();
// TODO: make this work with SDL Web 8/8.5 and later versions
// SDLWeb85:
        jsonGenerator.writeNumberField("type", cacheEvent.getType());
// Tridion9:        jsonGenerator.writeNumberField("type", cacheEvent.getEventType());
        jsonGenerator.writeStringField("regionPath", cacheEvent.getRegionPath());
        jsonGenerator.writeStringField("key", (String) cacheEvent.getKey());
        jsonGenerator.writeEndObject();
    }
}
