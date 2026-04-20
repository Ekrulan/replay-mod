package replaysystem;

import mindustry.entities.units.UnitController;
import mindustry.gen.Unit;

public class ReplayController implements UnitController {

    public final static ReplayController instance = new ReplayController();

    @Override
    public void unit(Unit unit) {
    }

    @Override
    public Unit unit() {
        return null;
    }

}