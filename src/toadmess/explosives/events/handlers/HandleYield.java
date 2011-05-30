package toadmess.explosives.events.handlers;

import org.bukkit.event.entity.EntityExplodeEvent;

import toadmess.explosives.EntityConf;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

public class HandleYield implements Handler {

	@Override
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!worldConf.hasYieldConfig()) {
			return;
		}
		
		((EntityExplodeEvent) ev.event).setYield(worldConf.getYield());
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { TippingPoint.CAN_CHANGE_EXPLOSION_YIELD };
	}
}
