package data.scripts.everyFrames.combat;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import data.scripts.campaign.misc.niko_MPC_satelliteHandler;
import data.utilities.*;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static data.utilities.niko_MPC_ids.niko_MPC_isSatelliteHullId;

public class niko_MPC_newSatelliteDeployerScript extends BaseEveryFrameCombatPlugin {

    private float framesToWait = 2;
    public boolean escapeBattle = false;

    private static class satelliteDeploymentParams {
        public float mapWidth;
        public float mapHeight;
        public float initialXCoord;
        public float xOffset;
        public float xThresholdForWrapping;
        public float initialYCoord;
        public float yOffset;
        public float facing;
        public float yCoord;
        public float xCoord;

        private void invertCoordinates() {
            initialYCoord *= -1f;
            yOffset *= -1f;
            if (facing == 90f) {
                facing = 270f;
            }
            else facing = 90f;
        }

    }

    @Override
    public void init(CombatEngineAPI engine) {
        super.init(engine);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (Global.getCurrentState() != GameState.COMBAT) return;

        super.advance(amount, events);

        if (framesToWait > 0) {
            framesToWait--;
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();


        FleetGoal playerGoal = engine.getContext().getPlayerGoal();
        FleetGoal otherGoal = engine.getContext().getOtherGoal();

        if (otherGoal == FleetGoal.ESCAPE || playerGoal == FleetGoal.ESCAPE) {
            escapeBattle = true;
        }

        deploySatellitesForSide(FleetSide.PLAYER, engine);
        deploySatellitesForSide(FleetSide.ENEMY, engine);

        prepareForGarbageCollection();
        engine.removePlugin(this);
    }

    private void prepareForGarbageCollection() {
        return;
    }

    private void deploySatellitesForSide(FleetSide side, CombatEngineAPI engine) {
        List<FleetMemberAPI> satellites = new ArrayList<>();

        int intOwner = (side == FleetSide.PLAYER ? 0 : 1);

        CombatFleetManagerAPI fleetManager = engine.getFleetManager(side);
        List<FleetMemberAPI> reserves = fleetManager.getReservesCopy();
        List<ShipAPI> ships = engine.getShips();
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        niko_MPC_satelliteBattleTracker tracker = niko_MPC_satelliteUtils.getSatelliteBattleTracker();

        if (!engine.isMission()) {
            BattleAPI thisBattle = playerFleet.getBattle();
            boolean usePlayerSide = (side == FleetSide.PLAYER);
            // WHY IS THIS SO FUCKING OBTUSE TO DO
            List<CampaignFleetAPI> fleetsOnSide = (usePlayerSide ? thisBattle.getPlayerSide() : thisBattle.getNonPlayerSide());

            for (CampaignFleetAPI potentialSatelliteFleet : fleetsOnSide) {
                niko_MPC_satelliteHandler handler = niko_MPC_satelliteUtils.getEntitySatelliteHandler(potentialSatelliteFleet);
                if (handler == null) continue;
                // else, they have satellites
                boolean hasSatelliteShips = false;
                for (FleetMemberAPI member : potentialSatelliteFleet.getFleetData().getMembersListCopy()) {
                    if (member.getHullSpec().hasTag(niko_MPC_ids.niko_MPC_isSatelliteHullId)) {
                        satellites.add(member);
                        hasSatelliteShips = true;
                    }
                }
                if (hasSatelliteShips)
                    if (!tracker.areSatellitesInvolvedInBattle(thisBattle, handler)) {
                        tracker.associateSatellitesWithBattle(thisBattle, handler, thisBattle.pickSide(potentialSatelliteFleet));
                    }
            }
        }
        else {
            for (FleetMemberAPI ship : reserves) {
                if (ship.getHullSpec().hasTag(niko_MPC_ids.niko_MPC_isSatelliteHullId)) {
                    if (satellites.contains(ship)) {
                        continue;
                    }
                    satellites.add(ship);
                }
            }
        }

        for (ShipAPI ship : ships) {
            if (ship.getHullSpec().hasTag(niko_MPC_ids.niko_MPC_isSatelliteHullId) && (ship.getOwner() == intOwner)) {
                FleetMemberAPI shipFleetMember = ship.getFleetMember();
                if (satellites.contains(shipFleetMember)) {
                    continue;
                }
                satellites.add(shipFleetMember);
            }
        }

        forceDeploySatellites(engine, fleetManager, side, satellites);
    }

    private void forceDeploySatellites(CombatEngineAPI engine, CombatFleetManagerAPI fleetManager, FleetSide side, List<FleetMemberAPI> satellites) {

        satelliteDeploymentParams params = new satelliteDeploymentParams();

        int size = satellites.size();
        if (size == 0) return;

        params.mapWidth = engine.getMapWidth();
        params.mapHeight = engine.getMapHeight();

        params.initialXCoord = 0;
        if (size > 1) {
            params.initialXCoord = (float) (params.mapWidth*-0.39); // if we have more than one, we set it to be at the very edge of the map
        }
        params.xOffset = 0;
        if (size > 1) {
            params.xOffset = (params.initialXCoord * -2) / (size - 1); //ex. if we start at 400, 2 satellites causes it to be 200, then 100, 50, etc
        }
        params.xThresholdForWrapping = params.mapWidth;

        params.initialYCoord = (float) (params.mapHeight*-0.27);
        params.yOffset = 200f;

        params.facing = 90f;


        if (side == FleetSide.ENEMY) {
            params.invertCoordinates();
        }
        if (escapeBattle) {
            params.invertCoordinates();
        }

        params.yCoord = params.initialYCoord;
        params.xCoord = params.initialXCoord;

        for (FleetMemberAPI satellite : satellites) {
            if (params.xCoord > params.xThresholdForWrapping) {
                params.xCoord = params.initialXCoord;
                params.yCoord += params.yOffset;
            }
            ShipAPI satelliteShip = null;
            if (fleetManager.getDeployedCopy().contains(satellite)) {
               // niko_MPC_debugUtils.displayError("satellite already deployed");
                satelliteShip = fleetManager.getShipFor(satellite);
            }
            deploySatellite(satellite, new Vector2f(params.xCoord, params.yCoord), params.facing, fleetManager, side, satelliteShip);
            params.xCoord += params.xOffset;
        }
    }

    private void deploySatellite(FleetMemberAPI satelliteFleetMember, Vector2f vector2f, float facing, CombatFleetManagerAPI fleetManager, FleetSide side, ShipAPI satellite) {
        if (satellite == null) {
            satellite = fleetManager.spawnFleetMember(satelliteFleetMember, vector2f, facing, 0);
        }
        satellite.getLocation().set(vector2f);
        satellite.setFacing(facing);

        int owner = 1;
        if (side == FleetSide.PLAYER) {
            owner = 0;
        }
        String name = Global.getSector().getFaction(Factions.DERELICT).pickRandomShipName();
        if (name == null) {
            niko_MPC_debugUtils.displayError("deploySatellite null name");
            name = "this name is an error, please report this to niko";
        }

        List<ShipAPI> modulesWithSatellite = (satellite.getChildModulesCopy());
        modulesWithSatellite.add(satellite);

        for (ShipAPI module : modulesWithSatellite) {
            if (module.getFleetMember() != null) {
                module.getFleetMember().setShipName(name);
                module.getFleetMember().setAlly(true);
                module.getFleetMember().setOwner(owner);
            }
            module.setOwner(owner);
            if (owner == 0) {
                module.setAlly(true);
            }
            if (Global.getSettings().getModManager().isModEnabled("automatic-orders")) {
                module.getVariant().addMod("automatic_orders_no_retreat");
            }
        }

        for (FighterWingAPI wing : satellite.getAllWings()) {
            for (ShipAPI fighter : wing.getWingMembers()) {
                fighter.getLocation().set(vector2f);
                if (owner == 0) {
                    fighter.setAlly(true);
                }
            }
        }

        satellite.setFixedLocation(vector2f); //todo: how the fuck am i gonna handle the possibility that the satellites spawn in other ships
        Global.getCombatEngine().addPlugin(new niko_MPC_satellitePositionDebugger(satellite, vector2f, facing));

        Set<ShipAPI> shipWithinBounds = new HashSet<>();
    }
}
