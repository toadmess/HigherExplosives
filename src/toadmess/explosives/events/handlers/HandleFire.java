package toadmess.explosives.events.handlers;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

public class HandleFire implements Handler {

	@Override
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!worldConf.hasFireConfig()) {
			return;
		}
		
		final ExplosionPrimeEvent downcastEvent = (ExplosionPrimeEvent) ev.event;
		
		downcastEvent.setFire(worldConf.getFire());
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { TippingPoint.CAN_CHANGE_EXPLOSION_FIRE_FLAG };
	}
	
	@Override
	public Type[] getBukkitEventsRequired() {
		return new Type[] { Event.Type.EXPLOSION_PRIME }; 
	}
	
	@Override
	public boolean isNeededBy(final EntityConf thisConfig) {
		return thisConfig.hasFireConfig(); 
	}
}
