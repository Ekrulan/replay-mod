package replaysystem.replayfmt;

import arc.struct.ObjectMap;
import arc.struct.Seq;

public class ReplayFmtMap extends ObjectMap<Integer, Seq<Object>> {

    public ReplayFmtMap() {
        super(3);
    }

    public void add(int key, Object value) {
        var seq = this.get(key);

        if (seq == null) {
            seq = new Seq<>();
            put(key, seq);
        }
        seq.add(value);
    }
}
