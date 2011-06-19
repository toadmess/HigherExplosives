package toadmess.explosives.events.handlers;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

public class HandleRadius implements Handler {
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!worldConf.hasRadiusConfig()) {
			return;
		}
		
		final ExplosionPrimeEvent downcastEvent = (ExplosionPrimeEvent) ev.event;
		
		downcastEvent.setRadius(worldConf.getNextRadiusMultiplier() * downcastEvent.getRadius());
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { TippingPoint.CAN_CHANGE_EXPLOSION_RADIUS };
	}
	

	@Override
	public Type[] getBukkitEventsRequired() {
		return new Type[] { Event.Type.EXPLOSION_PRIME };
	}
	
	@Override
	public boolean isNeededBy(final EntityConf thisConfig) {
		return thisConfig.hasRadiusConfig();
	}
}
