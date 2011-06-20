package toadmess.explosives.events.handlers;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockPhysicsEvent;

import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.HEWrappedTNTEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

public class HandleTNTPreventPrime implements Handler {
	@Override
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!worldConf.hasTNTPrimePrevented()) {
			return;
		}

		if(worldConf.getTNTPrimePrevented()) {
			switch(ev.type) {
			case TNT_PRIME_BY_REDSTONE:
				// For some reason the BlockPhysicsEvent does not implement Cancellable  
				((BlockPhysicsEvent) ev.event).setCancelled(true);
				break;
			case TNT_PRIME_BY_EXPLOSION:
				// Well, the TNT has already been primed. 
				// Not much we can do about that other than scouring each explosion's block list for TNT blocks.
				// We kill the TNTPrimed entity here and now before it does anything.
				((HEWrappedTNTEvent) ev).primedTnt.remove();
				break;
			default:
				((Cancellable) ev.event).setCancelled(true);
			}
		}
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { 
			TippingPoint.TNT_PRIME_BY_FIRE,
			TippingPoint.TNT_PRIME_BY_PLAYER,
			TippingPoint.TNT_PRIME_BY_REDSTONE,
			TippingPoint.TNT_PRIME_BY_EXPLOSION
		};
	}	

	@Override
	public Type[] getBukkitEventsRequired() {
		return new Type[]{}; // Relies on the TNTTracker
	}
	
	@Override
	public boolean isNeededBy(final EntityConf thisConfig) {
		return thisConfig.hasTNTPrimePrevented();
	}
}
