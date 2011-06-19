package toadmess.explosives.events.handlers;

import org.bukkit.event.Event.Type;

import toadmess.explosives.MCNative;
import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.HEWrappedTNTEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

public class HandleTNTFuse implements Handler {
	@Override
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!worldConf.hasTNTFuseConfig()) {
			return;
		}
		
		final HEWrappedTNTEvent fuseEvent = (HEWrappedTNTEvent) ev;
		
		MCNative.multiplyTNTFuseDuration(fuseEvent.primedTnt, worldConf.getNextTNTFuseMultiplier());
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { TippingPoint.CAN_CHANGE_TNT_FUSE };
	}
	
	@Override
	public Type[] getBukkitEventsRequired() {
		return new Type[]{}; // Relies on the TNTTracker
	}
	
	@Override
	public boolean isNeededBy(final EntityConf thisConfig) {
		return thisConfig.hasTNTFuseConfig();
	}
}
