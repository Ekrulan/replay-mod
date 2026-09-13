package replaysystem.replayplayer.replayers;

import arc.math.Mathf;
import arc.struct.IntSet;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.entities.units.UnitController;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import replaysystem.ReplayFrame;
import replaysystem.replayplayer.ReplayPlayer;

import static replaysystem.helpers.Util.safeFloat;

public class ReplayUnit implements ReplayPlayer.SnapshotApplier {

    @Override
    public void applySnapshot(Jval snapshot) {
        var unitsArray = snapshot.get(ReplayFrame.UNITS);
        if (unitsArray == null || !unitsArray.isArray()) return;

        for (var u : unitsArray.asArray()) {

            var unitDt = ReplayFrame.Unit.fromJson(u);

            if (unitDt == null) {
                Log.warn("ReplayPlayer: invalid unit part: " + u);
                continue;
            }

            var unit = Groups.unit.find(unit2 -> unit2.id == unitDt.id);

            if (unit == null) {
                var unitType = Vars.content.unit(unitDt.type);
                if (unitType == null) {
                    continue;
                }

                unit = unitType.create(Team.get(unitDt.team));
                unit.id = unitDt.id;
                unit.add();
            }
            unit.move(unitDt.x, unitDt.y);
            unit.rotation = unitDt.rot;
            unit.health = unitDt.health;
        }
    }

    @Override
    public void interpolate(Jval prevSnapshot, Jval curSnapshot) {

        var prevUnits = prevSnapshot.get(ReplayFrame.UNITS);
        var currUnits = curSnapshot.get(ReplayFrame.UNITS);
        if (prevUnits == null || currUnits == null) return;

        var prevIds = new IntSet();
        for (var pu : prevUnits.asArray()) {
            int id = pu.asArray().get(ReplayFrame.Unit.ID).asInt();
            prevIds.add(id);
        }


        for (var cu : currUnits.asArray()) {
            var ut = ReplayFrame.Unit.fromJson(cu);

            assert ut != null;

            prevIds.remove(ut.id);
//            var unit = Groups.unit.find(u -> u.id == ut.id);
            var unit = Groups.unit.getByID(ut.id);
            if (unit == null || unit.dead()) continue;

            unit.controller(new UnitController() {
                @Override
                public void unit(Unit unit) {

                }

                @Override
                public Unit unit() {
                    return null;
                }
            });

            var pu = findUnitById(prevUnits, ut.id);
            if (pu == null) continue;

            var arr = pu.asArray();

            var prevX = safeFloat(arr.get(ReplayFrame.Unit.X));
            assert prevX != null;
            var prevY = safeFloat(arr.get(ReplayFrame.Unit.Y));
            assert prevY != null;
            var prevRot = safeFloat(arr.get(ReplayFrame.Unit.ROT));
            assert prevRot != null;

            unit.set(Mathf.lerp(prevX, ut.x, 1f), Mathf.lerp(prevY, ut.y, 1f));
            unit.rotation = lerpAngle(prevRot, ut.rot);

            if (ut.target != null) {
                unit.aim(ut.target.x, ut.target.y);
                unit.isShooting = true;

                for (var mount : unit.mounts) {
                    mount.aimX = ut.target.x;
                    mount.aimY = ut.target.y;
                    mount.shoot = true;
                    mount.rotate = true;
                }
            } else {
                unit.isShooting = false;
            }

        }

        for (var it = prevIds.iterator(); it.hasNext; ) {
            var missingId = it.next();
            var unit = Groups.unit.getByID(missingId);
            if (unit != null && !unit.dead()) {
                unit.kill();
            }
        }
    }


    private static float lerpAngle(float from, float to) {
        float delta = to - from;

        if (delta > 180f) delta -= 360f;
        else if (delta < -180f) delta += 360f;

        return from + delta;
    }

    private Jval findUnitById(Jval unitsArray, int id) {
        if (!unitsArray.isArray()) return null;
        for (var u : unitsArray.asArray()) {
            if (u.asArray().get(ReplayFrame.Unit.ID).asInt() == id) return u;
        }
        return null;
    }

}