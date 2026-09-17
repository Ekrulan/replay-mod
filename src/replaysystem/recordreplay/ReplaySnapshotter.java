package replaysystem.recordreplay;

import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.gen.Groups;
import replaysystem.replayfmt.types.Block;
import replaysystem.replayfmt.types.Tick;
import replaysystem.replayfmt.types.Unit;


public class ReplaySnapshotter {
    private Seq<Object> content = newSeq();

    private int firstTick = -1;
    private int endTick = -1;
    private int lastTick = -1;


    private Seq<Object> newSeq() {
        return new Seq<>(10);
    }

    public @Nullable Seq<Object> createSnapshot() {
        var currentTick = (int) Vars.state.tick;
//        if (currentTick % ReplayConfig.SNAPSHOT_INTERVAL != 0 || currentTick == this.lastTick) return null;
        if (currentTick == this.lastTick) return null;

        this.lastTick = currentTick;

        recordUnits();

        if (this.content.isEmpty()) {
            return null;
        }

        this.content.add(new Tick(currentTick));

        var result = this.content;

        this.content = newSeq();
        this.tickUpdate(currentTick);

        return result;
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
