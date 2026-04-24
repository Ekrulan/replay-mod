package replaysystem;

import arc.util.Nullable;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.gen.Groups;

public class ReplaySnapshotter {
    private Jval snapshot = Jval.newObject();
    private Jval blocks = Jval.newArray();

    public @Nullable Jval createSnapshot() {
        var currentTick = (int) Vars.state.tick;
        if (currentTick % ReplayConfig.SNAPSHOT_INTERVAL != 0) return null;

        snapshot.put(ReplayFrame.TICK, currentTick);
        recordUnits();

        if (!blocks.asArray().isEmpty()) {
            snapshot.put(ReplayFrame.BLOCKS, blocks);
            blocks = Jval.newArray();
        }
        var result = snapshot;
        snapshot = Jval.newObject();

        return result;
    }

    private void recordUnits() {
        var unitsArray = Jval.newArray();
        Groups.unit.each(unit -> {
            if (unit == null || !unit.isAdded()) return;
            unitsArray.add(ReplayFrame.Unit.fromUnit(unit).toJson());
        });

        if (!unitsArray.asArray().isEmpty()) {
            snapshot.put(ReplayFrame.UNITS, unitsArray);
        }
    }

    public void recordBlock(ReplayFrame.Block block) {
        blocks.asArray().add(block.toJson());
    }
}
