package replaysystem;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.core.GameState;
import mindustry.io.SaveIO;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.Vars;

public class ReplayViewerDialog extends BaseDialog {

    public ReplayViewerDialog() {
        super("@replay-mod.view-replays");
        rebuild();
        this.addCloseButton();
    }

    private void rebuild() {
        cont.clear();
        cont.top().defaults().pad(8);

        Seq<Replay> replays = ReplayManager.instance.getAllReplays();

        if (replays.isEmpty()) {
            cont.add("@replay-mod.no-replays").pad(20);
            return;
        }

        for (Replay replay : replays) {
            Table row = new Table();
            row.left();

            row.add(replay.name).left().growX();
            row.button("@replay-mod.button.play", () -> playReplay(replay)).padLeft(12).width(100);
            row.button("@replay-mod.button.delete", () -> {
                ReplayManager.instance.delete(replay);
                rebuild();
            }).padLeft(8).width(100);

            cont.add(row).fillX().row();
        }

    }

    private void playReplay(Replay replay) {
        hide();

        var initial = replay.folder.child("initial.msav");
        if (!initial.exists()) {
            Log.err("ReplayViewer: initial.msav not found");
            return;
        }

        Vars.ui.paused.hide();

        Core.app.post(() -> {
            try {
                Vars.ui.loadfrag.show("@replay-mod.loading-replay");

                SaveIO.load(initial);

                Vars.state.set(GameState.State.playing);

                Vars.ui.loadfrag.hide();

                Log.info("ReplayViewer: replay loaded");
            } catch (Exception e) {
                Log.err("ReplayViewer: error loading replay", e);
                Vars.ui.loadfrag.hide();
            }
        });
    }
}