package replaysystem.ui;

import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import replaysystem.replay_player.ReplayPlayer;

public class ReplayControls {

    public Slider progressBar;
    public Table tbl;

    public void ReplayPlayer() {
        this.tbl = new Table();
    }


    public final static ReplayControls instance = new ReplayControls();

    public Table buildHud(ReplayPlayer replayPlayer) {
        var tbl = new Table();

        tbl.background(mindustry.ui.Styles.black6);

        tbl.center();

        tbl.add("panel").color(arc.graphics.Color.acid).padBottom(10f).row();

        // TODO
        this.progressBar = new Slider(0f, 1000f, 1f, false);

        progressBar.changed(() -> {
            replayPlayer.snapshotCursor = (int) progressBar.getValue();
        });

        tbl.add(progressBar).width(150f).row();
        return tbl;
    }

    public void updateProgressBase(ReplayPlayer replayPlayer) {
        progressBar.setValue(replayPlayer.snapshotCursor);
    }
}
