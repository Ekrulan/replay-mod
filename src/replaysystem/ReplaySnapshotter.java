package replaysystem;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.gen.Groups;
import replaysystem.replayfmt.types.Block;
import replaysystem.replayfmt.types.Tick;
import replaysystem.replayfmt.types.Unit;


public class ReplaySnapshotter {
    private final Seq<Object> content = new Seq<>();

    private int firstTick = -1;
    private int endTick = -1;
    private int lastTick = -1;


    public @Nullable Seq<Object> createSnapshot() {
        var currentTick = (int) Vars.state.tick;
//        if (currentTick % ReplayConfig.SNAPSHOT_INTERVAL != 0 || currentTick == this.lastTick) return null;
        if (currentTick == this.lastTick) return null;

        this.lastTick = currentTick;

        recordUnits();

        if (this.content.isEmpty()) {
            return null;
        } else {
            var c = this.content.copy(); // TODO избавится от копирования по возможности.
            c.add(new Tick(currentTick));
            this.tickUpdate(currentTick);
            this.content.clear();
            return c;
        }
    }

    private void tickUpdate(int tick) {
        if (this.firstTick == -1) {
            this.firstTick = tick;
        } else {
            this.endTick = tick;
        }
    }

    public int duration() {
        return this.endTick - this.firstTick;
    }

    private void recordUnits() {
        Groups.unit.each(unit -> {
            if (unit == null || !unit.isAdded()) return;
            this.content.add(Unit.fromUnit(unit));
        });
    }

    public void recordBlock(Block block) {
        this.content.add(block);
    }
}
