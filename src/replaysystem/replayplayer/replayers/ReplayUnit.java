package replaysystem.replayplayer.replayers;

import arc.math.Mathf;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.entities.units.UnitController;
import mindustry.game.Team;
import mindustry.gen.Groups;
import replaysystem.replayfmt.ReplayFmtMap;
import replaysystem.replayfmt.helpers.Types;
import replaysystem.replayfmt.types.Unit;
import replaysystem.replayplayer.ReplayPlayer;


public class ReplayUnit implements ReplayPlayer.SnapshotApplier {

    @Override
    public void applySnapshot(ReplayFmtMap snapshot) {
        var unitsArray = snapshot.get(Types.UNIT);
        if (unitsArray == null) return;

        for (var obj : unitsArray) {

            var un = (Unit) obj;

            var unit = Groups.unit.getByID(un.id);

            if (unit == null) {
                var unitType = Vars.content.unit(un.type);
                if (unitType == null) {
                    continue;
                }

                unit = unitType.create(Team.get(un.team));
                unit.id = un.id;
                unit.add();
            }
            unit.move(un.x, un.y);
            unit.rotation = un.rot;
            unit.health = un.health;
        }
    }

    @Override
    public void interpolate(ReplayFmtMap prevSnapshot, ReplayFmtMap curSnapshot) {

        var prevUnits = prevSnapshot.get(Types.UNIT);
        var currUnits = curSnapshot.get(Types.UNIT);
        if (prevUnits == null || currUnits == null) return;

        var prevIds = new IntSet();
        for (var pu : prevUnits) {
            prevIds.add(((Unit) pu).id);
        }


        for (var cu : currUnits) {
            var ut = (Unit) cu;

            prevIds.remove(ut.id);
//            var unit = Groups.unit.find(u -> u.id == ut.id);
            var unit = Groups.unit.getByID(ut.id);
            if (unit == null || unit.dead()) continue;

            unit.controller(new UnitController() {
                @Override
                public void unit(mindustry.gen.Unit unit) {

                }

                @Override
                public mindustry.gen.Unit unit() {
                    return null;
                }
            });

            var pu = findUnitById(prevUnits, ut.id);
            if (pu == null) continue;


//            var prevX = safeFloat(arr.get(ReplayFrame.Unit.X));
//            assert prevX != null;
//            var prevY = safeFloat(arr.get(ReplayFrame.Unit.Y));
//            assert prevY != null;
//            var prevRot = safeFloat(arr.get(ReplayFrame.Unit.ROT));
//            assert prevRot != null;

            unit.set(Mathf.lerp(pu.x, ut.x, 1f), Mathf.lerp(pu.y, ut.y, 1f));
            unit.rotation = lerpAngle(pu.rot, ut.rot);

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

    private @Nullable Unit findUnitById(Seq<Object> unitsArray, int id) {
        for (var u : unitsArray) {
            if (((Unit) u).id == id) return (Unit) u;
        }
        return null;
    }

}