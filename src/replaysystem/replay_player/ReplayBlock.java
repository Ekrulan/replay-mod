package replaysystem.replay_player;

import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import replaysystem.ReplayFrame;


public class ReplayBlock implements ReplayPlayer.SnapshotApplier {
    @Override
    public void applySnapshot(Jval snapshot) {
        var blocks = snapshot.get(ReplayFrame.BLOCKS);
        if (blocks == null) {
            return;
        }

        blocks.asArray().each((b) -> {
            if (!placeBlock(b)) {
                Log.warn("ReplayBlock: failed to placed " + b);
            }
        });
    }

    private static boolean placeBlock(Jval b) {
        var block = ReplayFrame.Block.fromJson(b);
        if (block == null) return false;

        var tile = Vars.world.tile(block.x, block.y);
        if (tile == null) return false;

        var blc = Vars.content.block(block.blockId);

        if (blc == null) return false;

        tile.setBlock(blc);

        if (blc.id == Blocks.air.id) return true;

        var build = tile.build;
        if (build == null) return false;

        build.team = Team.get(block.team);
        build.rotation = block.rot;
        build.health = block.health;

        build.add();
        return true;
    }

}
