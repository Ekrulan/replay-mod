package replaysystem.ui;

import arc.Events;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.Vars;
import mindustry.core.GameState;
import replaysystem.ReplayConfig;
import replaysystem.data.ReplayFile;
import replaysystem.replayplayer.ReplayPlayer;

public class ReplayViewerDialog extends BaseDialog {

    public ReplayViewerDialog() {
        super("@replay-mod.view-replays");
        this.rebuild();
        this.addCloseButton();
    }

    private void rebuild() {
        cont.clear();
        cont.top().defaults().pad(8);
        var replays = ReplayFile.replays();
        if (replays.isEmpty()) {
            cont.add("@replay-mod.no-replays").pad(20);
            return;
        }
        for (var replay : replays) {
            var info = replay.getInfoFile();
            var row = new Table();
            row.left();
            row.add(info.mapName).left().growX();
            row.button("@replay-mod.button.play", () -> playReplay(replay.createReader())).padLeft(12).width(100);
            row.button(
                    "@replay-mod.button.delete", () -> {
                        replay.delete();
                        rebuild();
                    }
            ).padLeft(8).width(100);
            cont.add(row).fillX().row();
        }
    }

    private void playReplay(ReplayFile.Reader replay) {
        hide();

        Log.info("ReplayViewer: load '" + replay + "' ...");

        Vars.ui.paused.hide();
        ReplayConfig.isLoadingReplay = true;
        ReplayConfig.isReplaying = false;

        var root = new GameWindow();


        arc.Core.app.post(() -> {
            try {
                Vars.ui.loadfrag.show("@replay-mod.loading-replay");

                Groups.unit.clear();
                Groups.build.clear();

                var ok = replay.loadNextMap();
                assert ok;

                Vars.state.set(GameState.State.playing);
                Events.fire(new EventType.WorldLoadEvent());


                ReplayConfig.isLoadingReplay = false;
                ReplayConfig.isReplaying = true;
                ReplayPlayer.instance.start(replay);
                var replayUi = ReplayControls.instance.buildHud(ReplayPlayer.instance);

                root.add(replayUi).expandY().bottom().padBottom(20f);
                ReplayPlayer.instance.listeners.add(ReplayControls.instance::updateProgressBase);

                Vars.ui.hudGroup.addChild(root);


                Vars.ui.loadfrag.hide();
                Log.info("ReplayViewer: replay playing");

            } catch (Exception e) {
                Log.err("ReplayViewer: error loading", e);
                Vars.ui.loadfrag.hide();
                ReplayConfig.isLoadingReplay = false;
                ReplayConfig.isReplaying = false;
            }
        });
    }

}