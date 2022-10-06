package data;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import data.scripts.campaign.listeners.niko_MPC_satelliteDiscoveredListener;
import data.scripts.campaign.listeners.niko_MPC_satelliteEventListener;
import data.scripts.campaign.plugins.niko_MPC_campaignPlugin;
import data.utilities.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class niko_MPC_modPlugin extends BaseModPlugin {

    private static final Logger log = Global.getLogger(niko_MPC_modPlugin.class);

    static {
        log.setLevel(Level.ALL);
    }

    @Override
    public void onApplicationLoad() throws RuntimeException {
        boolean isMagicLibEnabled = Global.getSettings().getModManager().isModEnabled("MagicLib");
        if (!isMagicLibEnabled) {
            throw(new RuntimeException("MagicLib is required for more planetary conditions!"));
        }
        boolean isLazyLibEnabled = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
        if (!isLazyLibEnabled) {
            throw(new RuntimeException("LazyLib is required for more planetary conditions!"));
        }

        try {
            niko_MPC_settings.loadSettings();
        } catch (IOException | JSONException | NullPointerException ex) {
            throw new RuntimeException(niko_MPC_ids.niko_MPC_masterConfig + " loading failed during application load! Exception: " + ex);
        }
    }

   /* @Override
    public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship) {
        if (ship.getHullSpec().hasTag("niko_MPC_isSatelliteHullId")) {

            return new PluginPick<ShipAIPlugin>(Global.getSettings().createDefaultShipAI(ship, config), CampaignPlugin.PickPriority.MOD_SPECIFIC);
        }
    } */

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        Global.getSector().addTransientListener(new niko_MPC_satelliteEventListener(false));
        if (niko_MPC_settings.DISCOVER_SATELLITES_IN_BULK) {
            Global.getSector().getListenerManager().addListener(new niko_MPC_satelliteDiscoveredListener(), true);
        }

        Global.getSector().registerPlugin(new niko_MPC_campaignPlugin());

        MemoryAPI globalMemory = Global.getSector().getMemory();

        if (!globalMemory.contains(niko_MPC_ids.satelliteBattleTrackerId)) {
            niko_MPC_memoryUtils.createNewSatelliteTracker();
        }
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        super.onNewGameAfterEconomyLoad();

        removeSatellitesFromMarkets(niko_MPC_satelliteUtils.getAllDefenseSatellitePlanets(), niko_MPC_settings.DEFENSE_SATELLITES_ENABLED);
    }

    public void removeSatellitesFromMarkets(@Nullable List<SectorEntityToken> toRemoveFrom, boolean removeOnlyFromCore) {
        if (toRemoveFrom == null) {
            toRemoveFrom = niko_MPC_satelliteUtils.getAllDefenseSatellitePlanets(); //by default: nuke it
        }

        for (SectorEntityToken entity : toRemoveFrom) {
            LocationAPI containingLocation = entity.getContainingLocation();
            if (!removeOnlyFromCore || containingLocation.getTags().contains(Tags.THEME_CORE)) { //we dont want to spawn in core worlds
                MarketAPI market = entity.getMarket();
                if (market != null) {
                    List<MarketConditionAPI> conditionsCopy = new ArrayList<>(market.getConditions());
                    for (MarketConditionAPI condition : conditionsCopy) {
                        if (niko_MPC_ids.satelliteConditionIds.contains(condition.getId())) {
                            market.removeCondition(condition.getId());
                        }
                    }
                } else {
                    niko_MPC_satelliteUtils.purgeSatellitesFromEntity(entity);
                }
            }
        }
    }
}

