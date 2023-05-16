package data.scripts.campaign.econ.conditions.overgrownNanoforge.intel

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import data.utilities.niko_MPC_marketUtils.exceedsMaxStructures
import data.utilities.niko_MPC_settings.OVERGROWN_NANOFORGE_PROGRESS_WHILE_UNDISCOVERED
import java.awt.Color
import kotlin.math.abs
import kotlin.math.roundToInt

class overgrownNanoforgeBaseIntelFactor(
    val baseProgress: Int, overgrownIntel: overgrownNanoforgeIntel
): baseOvergrownNanoforgeEventFactor(overgrownIntel) {
    override fun getProgress(intel: BaseEventIntel?): Int {
        return baseProgress
    }

    override fun getDesc(intel: BaseEventIntel?): String {
        return "Base spreading rate"
    }

    override fun createTooltip(): BaseFactorTooltip {
        return object : BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                if (tooltip == null) return
                val opad = 10f

                val stringToAdd = "The ${getNanoforgeName()} on ${getMarket().name} is spreading " +
                        "at a rate of $baseProgress per month."
                tooltip.addPara(stringToAdd, opad)
            }
        }
    }
}

class overgrownNanoforgeIntelFactorUndiscovered(overgrownIntel: overgrownNanoforgeIntel) :
    baseOvergrownNanoforgeEventFactor(overgrownIntel), EventFactor {

    override fun getProgress(intel: BaseEventIntel?): Int {
        if (!OVERGROWN_NANOFORGE_PROGRESS_WHILE_UNDISCOVERED) return 0
        if (!overgrownIntel.isHidden) return 0
        return -(overgrownIntel.baseFactor!!.baseProgress * 0.9).roundToInt()
    }

    override fun getDesc(intel: BaseEventIntel?): String {
        return "You shouldn't see this"
    }
}

class overgrownNanoforgeIntelFactorTooManyStructures(overgrownIntel: overgrownNanoforgeIntel) : baseOvergrownNanoforgeEventFactor(
    overgrownIntel
) {
    override fun getProgress(intel: BaseEventIntel?): Int {
        return if (overgrownIntel.getMarket().exceedsMaxStructures()) -overgrownIntel.getNaturalGrowthInt() else 0
    }

    override fun getDesc(intel: BaseEventIntel?): String {
        return "Not enough space to grow"
    }

    override fun createTooltip(): BaseFactorTooltip {
        return object: BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                if (tooltip == null) return
                val opad = 10f

                val stringToAdd = "${getMarket().name} has too many structures, making the ${getNanoforgeName()} unable to expand. " +
                        "This has the effect of completely halting all natural growth, making it far easier to cull any growths."

                tooltip.addPara(stringToAdd, opad)
            }
        }
    }
}

class overgrownNanoforgeIntelFactorCountermeasures(overgrownIntel: overgrownNanoforgeIntel) : baseOvergrownNanoforgeEventFactor(
    overgrownIntel
) {
    override fun getProgress(intel: BaseEventIntel?): Int {
        return overgrownIntel.getSuppressionRating()
    }

    private fun isCulling(): Boolean {
        return getProgress(overgrownIntel) <= 0
    }

    override fun getDesc(intel: BaseEventIntel?): String {
        return if (isCulling()) "Growth Suppression" else "Growth Cultivation"
    }

    override fun getDescColor(intel: BaseEventIntel?): Color {
        return Misc.getPositiveHighlightColor()
    }

    override fun shouldBeRemovedWhenSpreadingStops(): Boolean {
        return false
    }

    override fun createTooltip(): BaseFactorTooltip {
        return object: BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                if (tooltip == null) return
                val opad = 10f

                val stringToAdd = "${getMarket().name}'s government is currently using ${getUsedStrengthPercent()}% of its" +
                        " culling strength towards ${cullingOrCultivating()} the ${overgrownIntel.ourNanoforgeHandler.getCurrentName()}'s growth."

                tooltip.addPara(stringToAdd, opad)
            }
        }
    }

    private fun cullingOrCultivating(): String {
        return if (isCulling()) "culling" else "cultivating"
    }

    private fun getUsedStrengthPercent(): Float {
        return overgrownIntel.getUsedStrengthPercent()
    }
}