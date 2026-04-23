package replaysystem;

import arc.Core;
import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.PlayerSpawnCallPacket;
import mindustry.mod.Mod;
import mindustry.Vars;
import replaysystem.replay_player.ReplayPlayer;

// TODO оптимизировать хранение событий и их воспроизведение. возможно, переписать с json на sqlite

// TODO сделать опцию в настройках мода повзоляющую сохронять картку целиком раз в какое то время, что бы синхронизовать состояния для реплея.

public class Main extends Mod {

    public Main() {
        Log.info("ReplayMod loaded");

        Events.on(
                WorldLoadEvent.class, e -> {
                    if (ReplayConfig.isReplaying || ReplayConfig.isLoadingReplay) {
                        return;
                    }
                    ReplayRecorder.instance.start();
                }
        );

        Events.on(
                ResetEvent.class, e -> {
                    if (ReplayConfig.isLoadingReplay) {
                        return;
                    }

                    ReplayRecorder.instance.stop();
                    ReplayPlayer.instance.stop();
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
                e -> ReplayRecorder.instance.recordBlock(ReplayFrame.Block.fromEvent(e))
        );
        Events.on(
                BlockDestroyEvent.class,
                e -> ReplayRecorder.instance.recordBlock(ReplayFrame.Block.fromDestroy(e.tile.x, e.tile.y))
        );

        Events.run(
                Trigger.update, () -> {
                    ReplayRecorder.instance.onUpdate();
                    ReplayPlayer.instance.onUpdate();
                }
        );

        // ui
        Events.on(
                ClientLoadEvent.class, e -> Core.app.post(() -> {
                    Vars.ui.menufrag.addButton("@replay-mod.view-replays", Icon.play, () -> new ReplayViewerDialog().show());

                    Vars.ui.paused.buttons.row();
                    Vars.ui.paused.buttons.button(
                            "@replay-mod.view-replays", Icon.play, () -> {
                                Vars.ui.paused.hide();
                                new ReplayViewerDialog().show();
                            }
                    ).size(280f, 64f).padTop(12f).row();
                })
        );
    }

}
