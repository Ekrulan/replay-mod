package replaysystem.helpers;

import arc.util.Nullable;
import arc.util.serialization.Jval;

import static replaysystem.helpers.Util.safeFloat;

public class Coords2D {

    public float x;
    public float y;


    public Coords2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static @Nullable Coords2D fromJval(Jval vl) {
        if (!vl.isArray()) return null;

        var arr = vl.asArray();
        var ax = safeFloat(arr.get(0));
        if (ax == null) {
            return null;
        }
        var ay = safeFloat(arr.get(1));
        if (ay == null) {
            return null;
        }
        return new Coords2D(ax, ay);
    }

    public Jval toJval() {
        var targetArray = Jval.newArray();
        targetArray.add(this.x);
        targetArray.add(this.y);
        return targetArray;
    }


    @Override
    public String toString() {
        return String.format("[%s]: x=%f, y=%f", this.getClass().getName(), this.x, this.y);
    }
}
