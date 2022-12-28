package data.scripts.campaign.econ.industries

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry
import com.sun.org.apache.xpath.internal.operations.Bool

abstract class baseNikoIndustry: BaseIndustry() {

    open fun delete() {
        market.removeIndustry(this.id, null, false)
    }

}