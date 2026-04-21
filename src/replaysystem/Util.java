package replaysystem;

import arc.util.Nullable;
import arc.util.serialization.Jval;

public class Util {

    public static @Nullable Float safeFloat(Jval v) {
        try {
            String str = v.isString() ? v.asString().trim() : v.toString().trim();
            return Float.parseFloat(str);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Float safeFloat(Jval obj, Float def) {
        var r = safeFloat(obj);
        return r != null ? r : def;
    }
}