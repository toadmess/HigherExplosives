package toadmess.explosives.events.handlers;

import org.bukkit.event.Cancellable;

import toadmess.explosives.EntityConf;
import toadmess.explosives.MCNative;
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
}
