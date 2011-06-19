package toadmess.explosives.events.handlers;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;

import toadmess.explosives.MCNative;
import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

public class HandlePreventTerrainDamage implements Handler {

	@Override
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!worldConf.hasPreventTerrainDamageConfig()) {
			return;
		}
		
		if(!worldConf.getPreventTerrainDamage()) {
			return;
		}
		
		((Cancellable) ev.event).setCancelled(true);
		
		MCNative.playSoundExplosion(ev.getEventLocation());
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { TippingPoint.CAN_PREVENT_TERRAIN_DAMAGE };
	}
	

	@Override
	public Type[] getBukkitEventsRequired() {
		return new Type[] { Event.Type.ENTITY_EXPLODE };
	}
	
	@Override
	public boolean isNeededBy(final EntityConf thisConfig) {
		return thisConfig.hasPreventTerrainDamageConfig(); 
	}
}
