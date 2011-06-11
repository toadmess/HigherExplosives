package toadmess.explosives.events.handlers;

import toadmess.explosives.MCNative;
import toadmess.explosives.config.EntityConf;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.HEFuseEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

public class HandleTNTFuse implements Handler {
	@Override
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!worldConf.hasTNTFuseConfig()) {
			return;
		}
		
		final HEFuseEvent fuseEvent = (HEFuseEvent) ev;
		
		MCNative.multiplyTNTFuseDuration(fuseEvent.primedTnt, worldConf.getNextTNTFuseMultiplier());
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { TippingPoint.CAN_CHANGE_TNT_FUSE };
	}
}
