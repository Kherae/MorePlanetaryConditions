package data.scripts.campaign.econ.conditions.overgrownNanoforge

import data.scripts.campaign.econ.conditions.niko_MPC_industryAddingCondition
import data.utilities.niko_MPC_industryIds

class niko_MPC_overgrownNanoforge : niko_MPC_industryAddingCondition() {

    init {
        industryIds.add(niko_MPC_industryIds.overgrownNanoforgeIndustryId)

        //todo: add structure ids and override fun from super and shit
    }

}