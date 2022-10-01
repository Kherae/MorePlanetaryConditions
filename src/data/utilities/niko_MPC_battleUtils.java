package data.utilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class niko_MPC_battleUtils {

    @Nullable
    public static LocationAPI getContainingLocationOfBattle(@NotNull BattleAPI battle) {
        LocationAPI containingLocation = null;
        if (battle.isPlayerInvolved()) {
            containingLocation = Global.getSector().getPlayerFleet().getContainingLocation();
        }
        else {
            for (CampaignFleetAPI fleet : battle.getBothSides()) { //have to do this, because some fleet dont HAVE a containing location
                if (fleet.getContainingLocation() != null) { //ideally, this will only iterate once or twice before finding a location
                    containingLocation = fleet.getContainingLocation();
                    break; //we found a location, no need to check everyone else
                }
            }
        }
        return containingLocation;
    }

    @Nullable
    public static CampaignFleetAPI getStationFleetOfBattle(@NotNull BattleAPI battle) {
        for (CampaignFleetAPI potentialStationFleet : battle.getStationSide()) {
            if (potentialStationFleet.isStationMode()) {
                return potentialStationFleet; // fun fact about battles, there can only ever be one fleet with isstationmode set to true in a battle
                // so, this is risk-free
            }
        }
        return null;
    }

}
