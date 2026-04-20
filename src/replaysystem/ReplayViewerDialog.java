package replaysystem;

import arc.Events;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.io.SaveIO;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.Vars;
import mindustry.core.GameState;

public class ReplayViewerDialog extends BaseDialog {

    public ReplayViewerDialog() {
        super("@replay-mod.view-replays");
        rebuild();
        this.addCloseButton();
    }

    private void rebuild() {
        cont.clear();
        cont.top().defaults().pad(8);
        var replays = ReplayManager.instance.getAllReplays();
        if (replays.isEmpty()) {
            cont.add("@replay-mod.no-replays").pad(20);
            return;
        }
        for (var replay : replays) {
            var row = new Table();
            row.left();
            row.add(replay.name).left().growX();
            row.button("@replay-mod.button.play", () -> playReplay(replay)).padLeft(12).width(100);
            row.button(
                    "@replay-mod.button.delete", () -> {
                        ReplayManager.instance.delete(replay);
                        rebuild();
                    }
            ).padLeft(8).width(100);
            cont.add(row).fillX().row();
        }
    }

    private void playReplay(Replay replay) {
        hide();
        var initial = replay.folder.child("initial.msav");
        if (!initial.exists()) {
            Log.err("ReplayViewer: initial.msav not found!");
            return;
        }

        Log.info("ReplayViewer: load '" + replay.name + "' ...");

        Vars.ui.paused.hide();
        ReplayState.isLoadingReplay = true;
        ReplayState.isReplaying = false;

        arc.Core.app.post(() -> {
            try {
                Vars.ui.loadfrag.show("@replay-mod.loading-replay");

                Groups.unit.clear();
                Groups.build.clear();

                SaveIO.load(initial);

                disablePlayerControl();

                Vars.state.set(GameState.State.playing);
                Events.fire(new EventType.WorldLoadEvent());


                ReplayState.isLoadingReplay = false;
                ReplayState.isReplaying = true;
                ReplayPlayer.instance.start(replay);

                Vars.ui.loadfrag.hide();
                Log.info("ReplayViewer: replay playing");

            } catch (Exception e) {
                Log.err("ReplayViewer: error loading", e);
                Vars.ui.loadfrag.hide();
                ReplayState.isLoadingReplay = false;
                ReplayState.isReplaying = false;
            }
        });
    }

    private void disablePlayerControl() {
        if (Vars.player.unit() != null) {
            Vars.player.unit().kill();
            Vars.player.unit().controller(ReplayController.instance);
        }
        Vars.player.clearUnit();

    }
}