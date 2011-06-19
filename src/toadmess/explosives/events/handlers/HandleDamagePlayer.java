package toadmess.explosives.events.handlers;

import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.TippingPoint;

public class HandleDamagePlayer extends HandleDamageEntity {
	@Override
	protected boolean hasConfig(final EntityConf conf) {
		return conf.hasPlayerDamageConfig();
	}

	@Override
	protected float getDamageMultiplier(final EntityConf conf) {
		return conf.getNextPlayerDamageMultiplier();
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { TippingPoint.CAN_CHANGE_PLAYER_DAMAGE };
	}
	
	@Override
	public boolean isNeededBy(final EntityConf thisConfig) {
		return thisConfig.hasPlayerDamageConfig(); 
	}
}
