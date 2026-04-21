package replaysystem;

import arc.Core;
import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.PlayerSpawnCallPacket;
import mindustry.mod.Mod;
import mindustry.Vars;

// TODO оптимизировать хранение событий и их воспроизведение.


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
                e -> Log.info("BuildEnd: team: " + e.tile.team().id + "; x=" + e.tile.x + "; y=" + e.tile.y + "; build: " + (e.tile.build != null ?
                        e.tile.build.id : "null"))
        );
        Events.on(
                BlockDestroyEvent.class,
                e -> Log.info("Destroy: x=" + e.tile.x + "; y=" + e.tile.y)
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
                    Vars.ui.custom.buttons.button(
                            "@replay-mod.view-replays", Icon.play, () -> {
                                Vars.ui.paused.hide();
                                new ReplayViewerDialog().show();
                            }
                    ).size(280f, 64f).padTop(12f).row();
                })
        );
    }

}
