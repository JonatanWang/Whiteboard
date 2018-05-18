import io.vertx.core.eventbus.MessageConsumer;

import java.util.HashMap;
import java.util.Map;

public class PaintClient {

    private Map<Integer, MessageConsumer<String>> consumers = new HashMap<>();


    public boolean addConsumer(int key, MessageConsumer<String> consumer){
        if(!consumers.containsKey(key)){
            consumers.put(key, consumer);
            return true;
        }else{
            return false;
        }
    }

    public boolean isInvited(int key){
        return consumers.containsKey(key);
    }

    public void removeConsumer(int key){
        consumers.remove(key).unregister();
    }

    public void removeAll(){
        consumers.values().forEach(MessageConsumer::unregister);
        consumers.clear();
    }
}
