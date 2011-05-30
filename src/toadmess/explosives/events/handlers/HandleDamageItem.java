package toadmess.explosives.events.handlers;

import toadmess.explosives.EntityConf;
import toadmess.explosives.events.TippingPoint;

public class HandleDamageItem extends HandleDamageEntity {
	@Override
	protected boolean hasConfig(final EntityConf conf) {
		return conf.hasItemDamageConfig();
	}

	@Override
	protected float getDamageMultiplier(final EntityConf conf) {
		return conf.getNextItemDamageMultiplier();
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { TippingPoint.CAN_CHANGE_ITEM_DAMAGE };
	}
}
