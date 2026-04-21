package replaysystem;

import arc.util.Nullable;
import arc.util.serialization.Jval;

public class Util {

    public static @Nullable Float safeFloat(Jval obj, String key) {
        if (obj == null) return null;

        Jval v = obj.get(key);
        if (v == null || v.isNull()) return null;

        try {
            String str = v.isString() ? v.asString().trim() : v.toString().trim();
            return Float.parseFloat(str);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Float safeFloat(Jval obj, String key, Float def) {
        var r = safeFloat(obj, key);
        return r != null ? r : def;
    }
}