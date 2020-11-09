package tv.danmaku.ijk.media.exo2;

import android.util.LruCache;

import java.util.HashMap;

public class RangeManagerFactory {
    private static Object mutex = new Object();
    private static RangeManagerFactory instance;
    private LruCache<String, RangeManager> mangerCollection = new LruCache<>(10);

    private RangeManagerFactory() {
    }

    public static RangeManagerFactory getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) {
                    instance = new RangeManagerFactory();
                }
            }
        }
        return instance;
    }

    public RangeManager getRangeManger(String fileTag) {
        RangeManager manager = mangerCollection.get(fileTag);
        if (manager == null) {
            manager = new RangeManager(fileTag);
            mangerCollection.put(fileTag,manager);
        }
        return manager;
    }

    public boolean hasWroteRange(String fileTag, long start, int len) {
        return mangerCollection.get(fileTag) != null && mangerCollection.get(fileTag).hasWroteRange(start, len);
    }
}