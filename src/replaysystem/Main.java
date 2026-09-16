package replaysystem;

import arc.Core;
import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.PlayerSpawnCallPacket;
import mindustry.mod.Mod;
import mindustry.Vars;
import replaysystem.replayfmt.types.Block;
import replaysystem.replayplayer.ReplayPlayer;
import replaysystem.ui.ReplayViewerDialog;


public class Main extends Mod {

    public Main() {
        Log.info("ReplayMod loaded");

        var replayRecorder = new ReplayRecorder(); // TODO сдлеать умную загрузку. так как одновременно нужен тольк один из них.
        var replayPlayer = ReplayPlayer.withDefaultHandlers();

        Events.on(
                WorldLoadEvent.class, e -> {
                    if (ReplayConfig.isReplaying || ReplayConfig.isLoadingReplay) {
                        return;
                    }
                    replayRecorder.start();
                }
        );

        Events.on(
                ResetEvent.class, e -> {
                    if (ReplayConfig.isLoadingReplay) {
                        return;
                    }

                    replayRecorder.stop();
                    replayPlayer.stop();
                    ReplayConfig.isReplaying = false;
                    ReplayConfig.isLoadingReplay = false;
                }
        );

        Events.on(
                PlayerSpawnCallPacket.class, e -> {
                    if (ReplayConfig.isReplaying) {
                        e.player.clearUnit();
                    }
                }
        );

        Events.on(
                BlockBuildEndEvent.class,
                e -> {
                    if (replayRecorder.isRecording()) {
                        replayRecorder.recordBlock(Block.fromEvent(e));
                    }
                }
        );
        Events.on(
                BlockDestroyEvent.class,
                e ->
                {
                    if (replayRecorder.isRecording()) {
                        replayRecorder.recordBlock(Block.destroyBlock(e.tile.x, e.tile.y));
                    }
                }
        );

        Events.run(
                Trigger.update, () -> {
                    if (!ReplayConfig.shouldTakeSnapshot()) {
                        return;
                    }
                    replayRecorder.onUpdate();
                    replayPlayer.onUpdate();
                }
        );


        // ui
        Events.on(
                ClientLoadEvent.class, e -> Core.app.post(() -> {
                    Vars.ui.menufrag.addButton("@replay-mod.view-replays", Icon.play, () -> new ReplayViewerDialog(replayPlayer).show());

                    Vars.ui.paused.buttons.row();
                    Vars.ui.paused.buttons.button(
                            "@replay-mod.view-replays", Icon.play, () -> {
                                Vars.ui.paused.hide();
                                new ReplayViewerDialog(replayPlayer).show();
                            }
                    ).size(280f, 64f).padTop(12f).row();
                })
        );
    }

}
