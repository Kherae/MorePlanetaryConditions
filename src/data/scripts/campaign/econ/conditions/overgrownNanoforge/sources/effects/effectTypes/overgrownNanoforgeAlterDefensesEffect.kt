package data.scripts.campaign.econ.conditions.overgrownNanoforge.sources.effects.effectTypes

import com.fs.starfarer.api.impl.campaign.ids.Stats
import data.scripts.campaign.econ.conditions.overgrownNanoforge.handler.overgrownNanoforgeHandler
import data.scripts.campaign.econ.conditions.overgrownNanoforge.handler.overgrownNanoforgeIndustryHandler
import data.scripts.campaign.econ.conditions.overgrownNanoforge.sources.effects.overgrownNanoforgeEffectCategories
import data.scripts.campaign.econ.conditions.overgrownNanoforge.industries.overgrownNanoforgeIndustry
import kotlin.math.abs

class overgrownNanoforgeAlterDefensesEffect(
    handler: overgrownNanoforgeHandler,
    val increment: Float

): overgrownNanoforgeRandomizedEffect(handler) {
    override fun getCategory(): overgrownNanoforgeEffectCategories {
        if (defenseIsNegative()) return overgrownNanoforgeEffectCategories.DEFICIT else return overgrownNanoforgeEffectCategories.BENEFIT
    }

    private fun defenseIsNegative(): Boolean {
        return increment < 0f
    }

    override fun getName(): String {
        if (defenseIsNegative()) return "Indefensible" else return "Exceptionally defensible"
    }

    override fun getDescription(): String {
        return "placeholder"
    }

    override fun applyBenefits() {
        if (defenseIsNegative()) return
        getMarket().stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat(getId(), increment, getNameForModifier())
    }

    override fun applyDeficits() {
        if (!defenseIsNegative()) return
        getMarket().stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat(getId(), increment, getNameForModifier())

        val casted = (handler as overgrownNanoforgeIndustryHandler).junkHandlers

        "a"
    }

    override fun unapplyBenefits() {
        if (defenseIsNegative()) return
        getMarket().stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).unmodifyFlat(getId())
    }

    override fun unapplyDeficits() {
        if (!defenseIsNegative()) return
        getMarket().stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).unmodifyFlat(getId())    }

    override val baseFormat: String = "Market defense rating $adjectiveChar by $changeChar"

    override fun getChange(positive: Boolean, vararg args: Any): String {
        return "${abs(increment)}"
    }

    override fun getAllFormattedEffects(positive: Boolean): MutableList<String> {
        val list = ArrayList<String>()
        if (positive && defenseIsNegative()) return list
        if (!positive && !defenseIsNegative()) return list
        return super.getAllFormattedEffects(positive)
    }

}
