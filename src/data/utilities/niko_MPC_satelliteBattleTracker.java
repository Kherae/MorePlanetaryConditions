package data.utilities;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.campaign.H;
import com.fs.starfarer.campaign.fleet.Battle;
import data.scripts.campaign.misc.niko_MPC_satelliteParams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Save-specific global list that stores a hashmap of battleAPI->(satelliteParams->battleside). Will eventually replace the
 * list on params.
 */
public class niko_MPC_satelliteBattleTracker {

    public Map<BattleAPI, Map<niko_MPC_satelliteParams, BattleAPI.BattleSide>> battles = new HashMap<>();

    /**
     * Associates a hashmap entry of (battle->(params->side)). The side is stored for future reference.
     * <p>
     * If the hashmap doesnt contain battle, or if the v alue of battle is null, instantiates a new hashmap and assigns (battle->Hashmap()).
     * @param battle The battle to associate with.
     * @param params The satellite params we are associating.
     * @param side The stored side of the battle.
     */
    public void associateSatellitesWithBattle(BattleAPI battle, niko_MPC_satelliteParams params, BattleAPI.BattleSide side) {
        if (!battles.containsKey(battle) || battles.get(battle) == null) battles.put(battle, new HashMap<niko_MPC_satelliteParams, BattleAPI.BattleSide>());

        Map<niko_MPC_satelliteParams, BattleAPI.BattleSide> currentBattles = battles.get(battle);
        currentBattles.put(params, side);
    }

    public Map<BattleAPI, Map<niko_MPC_satelliteParams, BattleAPI.BattleSide>> getBattles() {
        return battles;
    }

    public Set<niko_MPC_satelliteParams> getSatellitesInfluencingBattle(BattleAPI battle) {

        return (battles.get(battle) == null ? new HashSet<niko_MPC_satelliteParams>() : battles.get(battle).keySet());
    }

    public BattleAPI.BattleSide getSideOfSatellitesForBattle(BattleAPI battle, niko_MPC_satelliteParams params) {
        return (battles.get(battle) == null ? null : battles.get(battle).get(params));
    }

    public boolean areSatellitesInvolvedInBattle(BattleAPI battle, niko_MPC_satelliteParams params) {
        if (battles.containsKey(battle)) {
            if (battles.get(battle).containsKey(params)) {
                return true;
            }
            else {
                /*updateBattleAssociationWithScan(battle);
                return battles.get(battle).containsKey(params);*/
                return false;
            }
        }
        return false;
    }

    public HashMap<niko_MPC_satelliteParams, CampaignFleetAPI> scanBattleForSatellites(BattleAPI battle) {
        HashMap<niko_MPC_satelliteParams, CampaignFleetAPI> paramsToFleetMap = new HashMap<>();
        for (CampaignFleetAPI fleet : battle.getBothSides()) {
            niko_MPC_satelliteParams foundParams = niko_MPC_satelliteUtils.getEntitySatelliteParams(fleet);
            if (foundParams != null) {
                paramsToFleetMap.put(foundParams, fleet);
            }
        }
        return paramsToFleetMap;
    }

    public void updateBattleAssociationWithScan(BattleAPI battle) {
        HashMap<niko_MPC_satelliteParams, CampaignFleetAPI> paramsToFleetMap = scanBattleForSatellites(battle);

        for (Map.Entry<niko_MPC_satelliteParams, CampaignFleetAPI> entry : paramsToFleetMap.entrySet()) {
            niko_MPC_satelliteParams params = entry.getKey();
            CampaignFleetAPI fleet = entry.getValue();

            if (!areSatellitesInvolvedInBattle(battle, params)) {
                HashMap<niko_MPC_satelliteParams, BattleAPI.BattleSide> paramsToSideMap = new HashMap<>();
                paramsToSideMap.put(params, battle.pickSide(fleet));
                battles.put(battle, paramsToSideMap);
            }
        }
    }

    public void removeBattle(BattleAPI battle) {
        battles.remove(battle);
    }

    public void removeParamsFromBattle(BattleAPI battle, niko_MPC_satelliteParams params) {
        if (battles.get(battle) != null) {
            battles.get(battle).remove(params);
        }
    }

    public void removeParamsFromAllBattles(niko_MPC_satelliteParams params) {
        for (BattleAPI battle : battles.keySet()) {
            removeParamsFromBattle(battle, params);
        }
    }

}