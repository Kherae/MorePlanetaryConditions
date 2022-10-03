package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import data.utilities.niko_MPC_ids;

public class niko_MPC_overgrownNanoforge extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        if (!market.hasIndustry(niko_MPC_ids.overgrownNanoforgeIndustryId)) {
            addIndustryToMarket();
        }
        super.apply(id);
    }

    private void addIndustryToMarket() {
        market.addIndustry(niko_MPC_ids.overgrownNanoforgeIndustryId);
    }
}