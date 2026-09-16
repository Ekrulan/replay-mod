package replaysystem.replayplayer.replayers;

import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import replaysystem.replayfmt.ReplayFmtMap;
import replaysystem.replayfmt.Types;
import replaysystem.replayfmt.types.Block;
import replaysystem.replayplayer.ReplayPlayer;


public class ReplayBlock implements ReplayPlayer.SnapshotApplier {
    @Override
    public void applySnapshot(ReplayFmtMap snapshot) {
        var blocks = snapshot.get(Types.BLOCK);
        if (blocks == null) {
            return;
        }

//        Log.info("ReplayBlock: " + blocks);

        blocks.each((b) -> {
            if (!placeBlock((Block) b)) {
                Log.warn("ReplayBlock: failed to placed " + b);
            }
        });
    }

    // TODO deletion of large blocks is displayed incorrectly
    private static boolean placeBlock(Block block) {

        var tile = Vars.world.tile(block.x, block.y);
        if (tile == null) return false;

        if (block.build == null) {
            tile.setAir();
            return true;
        }

        var blc = Vars.content.block(block.build.blockId);

        if (blc == null) return false;

        tile.setBlock(blc);

        if (blc.id == Blocks.air.id) return true;

        var build = tile.build;
        if (build == null) return false;

        build.team = Team.get(block.build.team);
        build.rotation = block.build.rot;
        build.health = block.build.health;

        build.add();
        return true;
    }

}
