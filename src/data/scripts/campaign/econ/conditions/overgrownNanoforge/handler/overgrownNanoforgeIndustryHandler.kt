package data.scripts.campaign.econ.conditions.overgrownNanoforge.handler

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import data.scripts.campaign.econ.conditions.overgrownNanoforge.industries.data.overgrownNanoforgeIndustrySource
import data.scripts.campaign.econ.conditions.overgrownNanoforge.industries.overgrownNanoforgeIndustry
import data.scripts.campaign.econ.conditions.overgrownNanoforge.intel.plugins.overgrownNanoforgeIndustryManipulationIntel
import data.scripts.campaign.econ.conditions.overgrownNanoforge.sources.effects.effectTypes.overgrownNanoforgeAlterSupplySource
import data.scripts.campaign.econ.conditions.overgrownNanoforge.sources.effects.overgrownNanoforgeEffectPrototypes
import data.scripts.campaign.econ.conditions.overgrownNanoforge.sources.overgrownNanoforgeEffectSource
import data.utilities.niko_MPC_debugUtils.displayError
import data.utilities.niko_MPC_industryIds.overgrownNanoforgeIndustryId
import data.utilities.niko_MPC_marketUtils.getOvergrownNanoforge
import data.utilities.niko_MPC_marketUtils.getOvergrownNanoforgeIndustryHandler
import data.utilities.niko_MPC_marketUtils.isInhabited
import data.utilities.niko_MPC_marketUtils.removeOvergrownNanoforgeIndustryHandler
import data.utilities.niko_MPC_marketUtils.setOvergrownNanoforgeIndustryHandler
import data.utilities.niko_MPC_settings.OVERGROWN_NANOFORGE_BASE_SCORE_MAX
import data.utilities.niko_MPC_settings.OVERGROWN_NANOFORGE_BASE_SCORE_MIN
import data.utilities.niko_MPC_settings.OVERGROWN_NANOFORGE_UNINHABITED_SPREAD_MULT
import org.lazywizard.lazylib.MathUtils

// WHAT THIS CLASS SHOULD HOLD
// 1. The base source of the industry
// 2. A list of structures spawned by this industry, or at least their parameters so they may be remade on demand
// 2.1: Alternatively, both, and just have this be the core's data hub it grabs everything from
// 3. The spreading scripts, as to allow spreading to happen when decivilized
// 4. The intel, to allow it to be active when uncolonized

// All in all, this is the permanent representation of the existance of a overgrown nanoforge
// The industry itself is only the hook we use to put this class into the game
class overgrownNanoforgeIndustryHandler(
    initMarket: MarketAPI,
): overgrownNanoforgeHandler(initMarket), EveryFrameScript {

    override var growing: Boolean = false

    var exposed: Boolean = false
        set(value) {
            val oldField = field
            field = value
            if (oldField != field) {
                getCastedManipulationIntel()?.exposed = field
            }
        }

    val junkHandlers: MutableSet<overgrownNanoforgeJunkHandler> = HashSet()

    var discovered: Boolean = false
        set(value: Boolean) {
            if (value != field) {
                intelBrain.hidden = !value
            }
            field = value
        }

    val intelBrain: overgrownNanoforgeSpreadingBrain = createIntelBrain()

    init {
        if (Global.getSector().memoryWithoutUpdate["\$overgrownNanoforgeHandlerList"] == null) Global.getSector().memoryWithoutUpdate["\$overgrownNanoforgeHandlerList"] = HashSet<overgrownNanoforgeIndustryHandler>()
        (Global.getSector().memoryWithoutUpdate["\$overgrownNanoforgeHandlerList"] as HashSet<overgrownNanoforgeIndustryHandler>) += this
    }

    override fun createBaseSource(): overgrownNanoforgeIndustrySource {
        val baseScore = getBaseScore()
        val supplyEffect = getBaseEffectPrototype().getInstance(this, baseScore)
        if (supplyEffect == null) {
            displayError("null supplyeffect on basestats oh god oh god oh god oh god oh god help")
            val source = overgrownNanoforgeIndustrySource(
                this, //shuld never happen
                mutableSetOf(overgrownNanoforgeAlterSupplySource(this, hashMapOf(Pair(Commodities.ORGANS, 500))))
            )
            return source
        }
        return overgrownNanoforgeIndustrySource(this, mutableSetOf(supplyEffect))
    }

    override fun init(initBaseSource: overgrownNanoforgeEffectSource?, resistance: Int?, resistanceRegen: Int?) {
        super.init(initBaseSource, resistance, resistanceRegen)
        if (market.getOvergrownNanoforgeIndustryHandler() != this) {
            displayError("nanoforge handler created on market with pre-existing handler: ${market.name}")
        }
        toggleExposed()
    }

    override fun advance(amount: Float) {
        //junkSpreader.advance(adjustedAmount)
    }

    fun getAdjustedSpreadAmount(amount: Float): Float {
        var adjustedAmount = amount
        val isInhabited = market.isInhabited()
        if (!isInhabited) adjustedAmount *= OVERGROWN_NANOFORGE_UNINHABITED_SPREAD_MULT
        return adjustedAmount
    }

    override fun isDone(): Boolean {
        return deleted
    }

    override fun runWhilePaused(): Boolean {
        return false
    }

    override fun applyEffects() {
        super.applyEffects()
        Global.getSector().addScript(this)
        if (market.getOvergrownNanoforgeIndustryHandler() != this) {
            displayError("nanoforge handler created on market with pre-existing handler: ${market.name}")
        }
        for (ourJunk in junkHandlers) {
            ourJunk.apply()
        }
    }

    override fun delete() {
        super.delete()

        (Global.getSector().memoryWithoutUpdate["\$overgrownNanoforgeHandlerList"] as HashSet<overgrownNanoforgeIndustryHandler>) -= this
        for (ourJunk in junkHandlers.toMutableSet()) {
            ourJunk.delete()
        }
    }

    fun notifyJunkDeleted(junk: overgrownNanoforgeJunkHandler) {
        toggleExposed()
    }
    fun notifyJunkAdded(junk: overgrownNanoforgeJunkHandler) {
        toggleExposed()
    }

    fun notifySpreadingStarted() {
        toggleExposed()
    }
    fun notifySpreadingStopped() {
        toggleExposed()
    }

    fun shouldExposeSelf(): Boolean {
        if (junkHandlers.isEmpty() && intelBrain.spreadingState != spreadingStates.SPREADING) return true

        return false
    }
    fun toggleExposed() {
        exposed = shouldExposeSelf()
    }

    override fun unapply() {
        super.unapply()
        Global.getSector().removeScript(this)
        for (ourJunk in junkHandlers) {
            ourJunk.unapply()
        }
    }

    override fun addSelfToMarket(market: MarketAPI) {
        market.setOvergrownNanoforgeIndustryHandler(this)
        super.addSelfToMarket(market)
    }

    override fun removeSelfFromMarket(market: MarketAPI) {
        val currHandler = market.getOvergrownNanoforgeIndustryHandler()
        if (currHandler != this) {
            displayError("bad deletion attempt for overgrown nanoforge handler on ${market.name}")
        }
        market.removeOvergrownNanoforgeIndustryHandler()
        super.removeSelfFromMarket(market)
    }

    private fun getBaseEffectPrototype(): overgrownNanoforgeEffectPrototypes {
        return overgrownNanoforgeEffectPrototypes.ALTER_SUPPLY 
        // Originally we just wanted the base effect to be alter supply
        // THis method is just here for like. Potential future changes
    }

    override fun getStructure(): overgrownNanoforgeIndustry? {
        return market.getOvergrownNanoforge()
    }

    override fun getNewStructureId(): String {
        return overgrownNanoforgeIndustryId
    }

    override fun getCoreHandler(): overgrownNanoforgeIndustryHandler {
        return this
    }

    fun createIntelBrain(): overgrownNanoforgeSpreadingBrain {
        val brain = overgrownNanoforgeSpreadingBrain(this)
        brain.init()
        return brain
    }

    fun getJunkSources(): MutableSet<overgrownNanoforgeEffectSource> {
        val sources: MutableSet<overgrownNanoforgeEffectSource> = HashSet()
        for (handler in junkHandlers) {
            sources.add(handler.baseSource)
        }
        return sources
    }

    fun getSupply(commodityId: String): Int { //TODO: convert to a int var that updates when any of the values change
        if (getStructure() != null) return getStructure()!!.getSupply(commodityId).quantity.modifiedInt
        var supply: Int = 0
        for (source in getAllSources()) {
            for (effect in source.effects) {
                if (effect is overgrownNanoforgeAlterSupplySource) {
                    effect.positiveSupply[commodityId]?.let { supply += it }
                }
            }
        }
        return supply
    }

    override fun createManipulationIntel(): overgrownNanoforgeIndustryManipulationIntel {
        val intel = overgrownNanoforgeIndustryManipulationIntel(intelBrain, this, intelBrain.hidden)
        intel.init()
        return intel
    }

    fun getCastedManipulationIntel(): overgrownNanoforgeIndustryManipulationIntel? {
        return (manipulationIntel as? overgrownNanoforgeIndustryManipulationIntel)
    }

    companion object {
        fun getBaseScore(): Float {
            return MathUtils.getRandomNumberInRange(OVERGROWN_NANOFORGE_BASE_SCORE_MIN, OVERGROWN_NANOFORGE_BASE_SCORE_MAX)
        }
    }
}
