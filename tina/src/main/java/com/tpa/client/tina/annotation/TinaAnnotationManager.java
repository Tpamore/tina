package com.tpa.client.tina.annotation;

import java.util.HashMap;
import java.util.Map;

public class TinaAnnotationManager {

    private static TinaAnnotationManager instance = new TinaAnnotationManager();

    private Map<String, TinaAnnotationHandler> map = new HashMap<>();

    public static TinaAnnotationManager getInstance() {
        return instance;
    }

    public synchronized void register(Class annotation , TinaAnnotationHandler handler) {
        if (!map.containsKey(annotation.getCanonicalName())) {
            map.put(annotation.getCanonicalName() , handler);
        }
    }

    public synchronized void unRegister(Class annotation) {
        map.remove(annotation.getCanonicalName());
    }

    public TinaAnnotationHandler getHanlder(Class annotation){
        return map.get(annotation.getCanonicalName());
    }
}
