package data.scripts.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.econ.industries.niko_MPC_defenseSatelliteLuddicSuppressor;
import data.scripts.campaign.misc.niko_MPC_satelliteHandler;
import data.scripts.everyFrames.niko_MPC_satelliteCustomEntityRemovalScript;
import data.utilities.niko_MPC_debugUtils;
import data.utilities.niko_MPC_ids;
import data.utilities.niko_MPC_satelliteUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static data.utilities.niko_MPC_debugUtils.logEntityData;
import static data.utilities.niko_MPC_ids.luddicPathSuppressorStructureId;
import static data.utilities.niko_MPC_satelliteUtils.defenseSatellitesApplied;

public class niko_MPC_antiAsteroidSatellites extends BaseHazardCondition {

    private static final Logger log = Global.getLogger(niko_MPC_antiAsteroidSatellites.class);

    static {
        log.setLevel(Level.ALL);
    }

    /**
     * A hashmap containing variantId, plus weight, in float, to be picked, when a satellite is spawned.
     */
    public HashMap<String, Float> weightedVariantIds = new HashMap<>();
    public int maxPhysicalSatellites = 0;
    public int maxBattleSatellites = 0;
    public String satelliteFleetName = "Domain-era defense satellites";
    /**
     * The internal Id that will be applied to satellite entities, not fleets. Always is appended by its position in the satellite list.
     */
    public String satelliteId = "niko_MPC_derelict_anti_asteroid_satellite";

    //todo: do i need this
    public String satelliteFactionId = "derelict";

    // These variables handle the condition's shit itself
    public float baseAccessibilityIncrement = -0.30f; //also placeholder
    public float baseGroundDefenseIncrement = 500;
    public float baseStabilityIncrement = 1;
    public float baseGroundDefenseMult = 1.5f;
    public String fakeSatelliteFactionId = niko_MPC_ids.derelictSatelliteFakeFactionId;

    public niko_MPC_antiAsteroidSatellites() {
        weightedVariantIds.put("niko_MPC_defenseSatelliteCore_barrage", 10f);
        weightedVariantIds.put("niko_MPC_defenseSatelliteCore_standard", 20f);
        weightedVariantIds.put("niko_MPC_defenseSatelliteCore_shielded", 5f);
        weightedVariantIds.put("niko_MPC_defenseSatelliteCore_beamer", 1.5f);
        weightedVariantIds.put("niko_MPC_defenseSatelliteCore_ordnance", 2f);
        weightedVariantIds.put("niko_MPC_defenseSatelliteCore_swarm", 0.01f); // :)
    }

    @Override
    public void apply(String id) {
        SectorEntityToken primaryEntity = market.getPrimaryEntity();
        if (primaryEntity != null) {
            // important for ensuring the same density of satellites for each entity. they will all have the same ratio of satellite to radius
            maxPhysicalSatellites = niko_MPC_satelliteUtils.getMaxPhysicalSatellitesBasedOnEntitySize(primaryEntity);
            maxBattleSatellites = niko_MPC_satelliteUtils.getMaxBattleSatellites(primaryEntity);
            if (!defenseSatellitesApplied(primaryEntity)) { // if our entity is not supposed to have satellites
                initializeSatellitesOntoHolder(); // we should make it so that it should
            } else if (niko_MPC_satelliteUtils.marketsDesynced(primaryEntity, market)) { // if it is, check if its tracking market correctly
                ensureOldMarketHasNoReferencesFailsafe();
                niko_MPC_satelliteUtils.syncMarket(primaryEntity, market); // if not, set its memory key to market
            }
            else marketApplicationOrderTest(); //if markets arent desynced, make sure the order didnt get fucked up.
            if (!niko_MPC_debugUtils.assertEntityHasSatellites(primaryEntity)) return;
            niko_MPC_satelliteHandler handler = niko_MPC_satelliteUtils.getEntitySatelliteHandler(primaryEntity);
            handler.updateFactionForSelfAndSatellites();
        }
        handleConditionAttributes(id, market); //whenever we apply or re-apply this condition, we first adjust our numbered bonuses and malices
    }

    /**
     * Exists mainly so that I can put custom behavior here.
     */
    public void initializeSatellitesOntoHolder() {
        float orbitDistance = getSatelliteOrbitDistance(market.getPrimaryEntity());
        float interferenceDistance = getSatelliteInterferenceDistance(market.getPrimaryEntity(), orbitDistance);
        float barrageDistance = getSatelliteBarrageDistance(market.getPrimaryEntity());
        niko_MPC_satelliteHandler handler = new niko_MPC_satelliteHandler(
                market.getPrimaryEntity(),
                satelliteId,
                satelliteFactionId,
                satelliteFleetName,
                maxPhysicalSatellites,
                maxBattleSatellites,
                orbitDistance,
                interferenceDistance,
                barrageDistance,
                weightedVariantIds);

        niko_MPC_satelliteUtils.initializeSatellitesOntoEntity(market.getPrimaryEntity(), market, handler);
    }

    /**
     * Unused, currently. This was for the planned satellite barrage terrain.
     */
    private float getSatelliteBarrageDistance(@NotNull SectorEntityToken primaryEntity) {
        return (primaryEntity.getRadius()+500);
    }

    private float getSatelliteOrbitDistance(SectorEntityToken entity) {
        return getSatelliteOrbitDistance(entity, false);
    }

    private float getSatelliteOrbitDistance(SectorEntityToken entity, boolean useHandler) {
        if (useHandler) {
            return niko_MPC_satelliteUtils.getEntitySatelliteHandler(entity).getSatelliteOrbitDistance();
        }
        float extraRadius = 15f;
        return entity.getRadius() + extraRadius;
    }

    private float getSatelliteInterferenceDistance(SectorEntityToken entity) {
        return getSatelliteInterferenceDistance(entity, niko_MPC_satelliteUtils.getEntitySatelliteHandler(entity).getSatelliteOrbitDistance());
    }

    private float getSatelliteInterferenceDistance(SectorEntityToken primaryEntity, float orbitDistance) {
        return orbitDistance+95;
    }

    /**
     * Where all the condition attributes (accessability, hazard, etc) are handled. This is NOT where the satellite tracker is handled.
     * @param id
     * @param market
     */
    private void handleConditionAttributes(String id, @NotNull MarketAPI market) {
        if (market.hasCondition("meteor_impacts")) {
            market.suppressCondition("meteor_impacts"); //these things just fuck those things up
        }

        if (!(market.hasIndustry(luddicPathSuppressorStructureId))) { // when we apply, we check to see if our luddic path suppression is active
            market.addIndustry(luddicPathSuppressorStructureId); //if it isnt, we make it active
        }

        //maths to handle the changing values go in the getters
        float accessibilityIncrement = getAccessibilityBonus();
        float groundDefenseIncrement = getGroundDefenseBonus();
        float groundDefenseMult = getGroundDefenseMult();
        float stabilityIncrement = getStabilityBonus();

        market.getAccessibilityMod().modifyFlat(id, accessibilityIncrement, getName());
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, groundDefenseMult, getName());
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat(id, groundDefenseIncrement, getName());
        market.getStability().modifyFlat(id, stabilityIncrement, getName());
    }

    public MarketAPI getMarket() {
        return market;
    }
    public String getSatelliteId() {
        return satelliteId;
    }

    public float getAccessibilityBonus() {
        return baseAccessibilityIncrement;
    }

    public float getGroundDefenseBonus() {
        return baseGroundDefenseIncrement;
    }

    public float getStabilityBonus() {
        return baseStabilityIncrement;
    }

    public float getGroundDefenseMult() {
        return baseGroundDefenseMult;
    }

    public float getLuddicPathInterestBonus() {
        if (getMarket().hasIndustry(luddicPathSuppressorStructureId)) {
            niko_MPC_defenseSatelliteLuddicSuppressor industry = (niko_MPC_defenseSatelliteLuddicSuppressor) market.getIndustry(luddicPathSuppressorStructureId);
            return industry.getPatherInterest();
        }
        return 0;
    }

    @Override
    public void unapply(String id) {
    //    super.unapply(id);
        getMarket().getAccessibilityMod().unmodify(id);
        getMarket().getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        getMarket().getStability().unmodify(id);
        if (getMarket().hasIndustry(luddicPathSuppressorStructureId)) {
            getMarket().removeIndustry(luddicPathSuppressorStructureId, null, false);
        }
        getMarket().unsuppressCondition("meteor_impacts");

        if (!market.getPrimaryEntity().hasScriptOfClass(niko_MPC_satelliteCustomEntityRemovalScript.class)) {
            market.getPrimaryEntity().addScript(new niko_MPC_satelliteCustomEntityRemovalScript(market.getPrimaryEntity(), condition.getId())); //adds a script that will check the next frame if the market has no condition,
            // and will remove the satellites and such if it doesnt. whatever the case, it removes itself next frame
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        int patherInterestReductionAmount = 3;

        if (market.hasIndustry(luddicPathSuppressorStructureId)) {
            niko_MPC_defenseSatelliteLuddicSuppressor industry = (niko_MPC_defenseSatelliteLuddicSuppressor) market.getIndustry(luddicPathSuppressorStructureId);

            patherInterestReductionAmount = (int) Math.abs(industry.getPatherInterest());
        }
        tooltip.addPara(
                "The satellites completely saturate all possible entry vectors, making it %s to approach " +
                        market.getName() + " while the satellites consider you a threat. This seems to always be the case " +
                        "while the satellites have standard domain programming. Judging from observation (and the odd historical record), it would seem " +
                        "that this threat evaluation is based off reprogrammable values, meaning that whoever holds the " +
                        "planet can %s. Additionally, it can be inferred that %s if you are %s. It would also seem that " +
                        "the satellites cannot identify you if you %s, and will likely consider you a threat anyway, even if you hold " +
                        "the planet.",
                10f,
                Misc.getHighlightColor(),
                "entirely impossible",
                "make the satellites target their enemies",
                "the satellites will block your approach",
                "inhospitable to the holder",
                "have your transponder off"
        );

        tooltip.addPara(
                "%s stability",
                10f,
                Misc.getHighlightColor(),
                ("+" + getStabilityBonus())
        );

        int convertedAccessibilityBonus = (int) (getAccessibilityBonus() * 100);  //times 100 to convert out of decimal


        tooltip.addPara(
                "%s accessibility",
                10f,
                Misc.getHighlightColor(),
                (convertedAccessibilityBonus + "%")
        );

        tooltip.addPara(
                "%s defense rating",
                10f,
                Misc.getHighlightColor(),
                ("+" + getGroundDefenseBonus())
        );

        tooltip.addPara(
                "%s defense rating",
                10f,
                Misc.getHighlightColor(),
                ("+" + getGroundDefenseMult() + "x")
        );

        tooltip.addPara(
                "Danger from asteroid impacts %s.",
                10f,
                Misc.getHighlightColor(),
                "nullified"
        );

        tooltip.addPara(
                "Due to the satellites defaulting to hostile behavior when considering a fleet with no transponder " +
                        "(excluding programmed exceptions and manual overrides), customs control is significantly easier, " +
                        "leading to %s being reduced by %s.",
                10f,
                Misc.getHighlightColor(),
                "effective luddic path interest", Integer.toString(patherInterestReductionAmount)
        );

        tooltip.addPara(
                "Due to the fact that fleets are %s, " +
                        "and the fact that %s, " +
                        "orbital defenses are %s.",
                10f,
                Misc.getHighlightColor(),
                "forced to break into the satellite orbit to interact with the planet",
                "any battle that takes place very close to the planet will have satellites interfere",
                "enhanced"
        );
    }

    /**
     * Debugging tool, used for ensuring this condition doesn't apply itself to a entity which doesnt consider the conditions market
     * to be its market. Displays an error if conditions are true. May not be a required debugging tool.
     */
    public void marketApplicationOrderTest() {
        SectorEntityToken entity = market.getPrimaryEntity();

        if ((entity.getMarket() != market)) {
            niko_MPC_debugUtils.displayError("marketApplicationOrderTest failure, market: " + market + ", recognized market: " + entity.getMarket());
            logEntityData(entity);
        }
    }

    /**
     * Removes the luddic path suppressor industry from the entity's current market, if it isnt our market. Should
     * only ever be called if we know we're migrating markets or some such.
     */
    public void ensureOldMarketHasNoReferencesFailsafe() {
        SectorEntityToken entity = market.getPrimaryEntity();

        //while loading, getMarket() returns null? or maybe its something to do with condition markets being jank?
        if (entity.getMarket() != null) {
            if (entity.getMarket() != market && entity.getMarket().hasIndustry(luddicPathSuppressorStructureId)) {
           /* log.debug(entity.getName() + "'s " + entity.getMarket() + " failed the reference failsafe in " + entity.getStarSystem().getName());
            logEntityData(entity);
            if (Global.getSettings().isDevMode()) {
                displayErrorToCampaign("ensureOldMarketHasNoReferencesFailsafe failure");
            } */ //commented out since i got what i want from it: it DOES fire, meaning this IS needed
                entity.getMarket().removeIndustry(luddicPathSuppressorStructureId, null, false);
            }
        }
    }
}
