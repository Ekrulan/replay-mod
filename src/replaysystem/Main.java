package replaysystem;

import arc.Core;
import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.PlayerSpawnCallPacket;
import mindustry.mod.Mod;
import mindustry.Vars;

public class Main extends Mod {

    public Main() {
        Log.info("ReplayMod loaded");

        Events.on(WorldLoadEvent.class, e -> {
            if (ReplayState.isReplaying || ReplayState.isLoadingReplay) {
                return;
            }
            ReplayRecorder.instance.start();
        });

        Events.on(ResetEvent.class, e -> {
            if (ReplayState.isLoadingReplay) {
                return;
            }

            ReplayRecorder.instance.stop();
            ReplayPlayer.instance.stop();
            ReplayState.isReplaying = false;
            ReplayState.isLoadingReplay = false;
        });

        Events.on(
                PlayerSpawnCallPacket.class, e -> {
            if (ReplayState.isReplaying) {
                e.player.clearUnit();
            }
        });

//        Events.on(BlockBuildEndEvent.class, e -> ReplayRecorder.instance.recordEvent("buildEnd", e));
//        Events.on(BlockDestroyEvent.class, e -> ReplayRecorder.instance.recordEvent("destroy", e));
//        Events.on(UnitDestroyEvent.class, e -> ReplayRecorder.instance.recordEvent("unitDestroy", e));
//        Events.on(UnitSpawnEvent.class, e -> ReplayRecorder.instance.recordEvent("unitSpawn", e));
//        Events.on(UnitChangeEvent.class, e -> ReplayRecorder.instance.recordEvent("unitChange", e));
//        Events.on(ConfigEvent.class, e -> ReplayRecorder.instance.recordEvent("config", e));
//        Events.on(TapEvent.class, e -> ReplayRecorder.instance.recordEvent("tap", e));

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
