import com.fasterxml.jackson.core.JsonProcessingException;
import com.tridion.cache.CacheEvent;
import org.dd4t.cache.CacheEventSerializer;
import org.dd4t.cache.CacheEventSerializerService;

public class Tester {
    public static void main(String[] args) {
        CacheEvent cacheEvent = new CacheEvent("regionpathX", "my:key", 1);
        try {
            String json = CacheEventSerializerService.serialize(cacheEvent);
            System.out.println(json);
            System.out.println("");
        } catch (JsonProcessingException e) {
            System.out.println("caught exception " + e.getMessage());
        }
    }
}
