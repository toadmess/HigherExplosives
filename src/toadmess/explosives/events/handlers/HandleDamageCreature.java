package toadmess.explosives.events.handlers;

import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.TippingPoint;

public class HandleDamageCreature extends HandleDamageEntity {
	@Override
	protected boolean hasConfig(final EntityConf conf) {
		return conf.hasCreatureDamageConfig();
	}

	@Override
	protected float getDamageMultiplier(final EntityConf conf) {
		return conf.getNextCreatureDamageMultiplier();
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { TippingPoint.CAN_CHANGE_CREATURE_DAMAGE };
	}
}
