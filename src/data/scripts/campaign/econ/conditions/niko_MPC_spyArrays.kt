package data.scripts.campaign.econ.conditions

import com.fs.starfarer.api.campaign.RepLevel
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import data.utilities.niko_MPC_marketUtils.isInhabited

class niko_MPC_spyArrays: niko_MPC_baseNikoCondition() {

    val sameFactionSensorProfileMult = 0.75f
    val stabilityIncrement = 1f
    val groundDefenseIncrement = 400f

    override fun advance(amount: Float) {
        super.advance(amount)

        val market = getMarket() ?: return
        val containingLocation = market.containingLocation ?: return

        if (market.isInHyperspace) return
        if (!market.isInhabited()) return

        val id = modId
        for (fleet in containingLocation.fleets) {
            if (fleet.isInHyperspaceTransition) continue
            if (fleet.faction.getRelationshipLevel(market.faction) >= RepLevel.FRIENDLY) {
                val profileMod = fleet.stats.detectedRangeMod.getMultBonus(id)
                if (profileMod == null || profileMod.value >= sameFactionSensorProfileMult) {
                    fleet.stats.addTemporaryModMult(
                        0.25f, id,
                        "${market.name} $name", sameFactionSensorProfileMult,
                        fleet.stats.detectedRangeMod
                    )
                }
            }
        }
    }

    override fun apply(id: String) {
        super.apply(id)

        val market = getMarket() ?: return

        market.stability.modifyFlat(id, stabilityIncrement, name)
        market.stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat(id, groundDefenseIncrement, name)
    }

    override fun unapply(id: String?) {
        super.unapply(id)

        if (id == null) return
        val market = getMarket() ?: return

        market.stability.unmodify(id)
        market.stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id)
    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createTooltipAfterDescription(tooltip, expanded)
        if (tooltip == null) return

        tooltip.addPara(
            "%s stability",
            10f,
            Misc.getHighlightColor(),
            "+$stabilityIncrement"
        )

        tooltip.addPara(
            "%s defense rating",
            10f,
            Misc.getHighlightColor(),
            "+$groundDefenseIncrement"
        )

        tooltip.addPara(
            "Friendly fleets in system get %s",
            10f,
            Misc.getHighlightColor(),
            "${sameFactionSensorProfileMult}x detected-at range"
        )
    }
}