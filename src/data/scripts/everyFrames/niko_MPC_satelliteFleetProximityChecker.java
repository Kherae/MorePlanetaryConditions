package data.scripts.everyFrames;

import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import data.scripts.campaign.misc.niko_MPC_satelliteHandler;
import data.utilities.niko_MPC_fleetUtils;
import org.lazywizard.lazylib.campaign.CampaignUtils;

import java.util.List;

public class niko_MPC_satelliteFleetProximityChecker implements EveryFrameScriptWithCleanup {

    public SectorEntityToken entity;
    public boolean done = false;
    public niko_MPC_satelliteHandler satelliteHandler;
    private float deltaTime = 0f;

    public niko_MPC_satelliteFleetProximityChecker(niko_MPC_satelliteHandler handler, SectorEntityToken entity) {
        this.satelliteHandler = handler;
        this.entity = entity;

        init();
    }

    private void init() {
        if (entity == null) {
            prepareForGarbageCollection();
        }
    }

    @Override
    public void cleanup() {
        prepareForGarbageCollection();
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        //CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        //if (playerFleet.getContainingLocation() != entity.getContainingLocation()) return;

        // using deltatime because i want a bit more performance
        // its not like we need to run EVERY frame, only enough
        deltaTime += amount;
        float thresholdForAdvancement = 0.2f;
        if (deltaTime < thresholdForAdvancement) {
            return;
        } else {
            deltaTime = 0;
        }

        List<CampaignFleetAPI> fleetsWithinInterferenceDistance = CampaignUtils.getNearbyFleets(entity, satelliteHandler.getSatelliteInterferenceDistance());
        for (CampaignFleetAPI fleet : fleetsWithinInterferenceDistance) {
            if (!niko_MPC_fleetUtils.isFleetValidEngagementTarget(fleet)) continue;
            BattleAPI battle = (fleet.getBattle());
            if (battle == null) continue;

            satelliteHandler.makeEntitySatellitesEngageFleet(fleet);
            // this method does checks to see if they should be engaged
        }
    }
    public void prepareForGarbageCollection () {
        entity = null;

        if (satelliteHandler != null) {
            satelliteHandler.satelliteFleetProximityChecker = null;
            satelliteHandler = null;
        }

        done = true;
    }
}
