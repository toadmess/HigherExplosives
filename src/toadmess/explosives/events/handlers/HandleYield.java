package toadmess.explosives.events.handlers;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityExplodeEvent;

import toadmess.explosives.MCNative;
import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

public class HandleYield implements Handler {

	@Override
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!worldConf.hasYieldConfig() && !worldConf.hasSpecificYieldConfig()) {
			return;
		}

		final EntityExplodeEvent eee = (EntityExplodeEvent) ev.event;
		
		if(!worldConf.hasSpecificYieldConfig()) {
			eee.setYield(worldConf.getYield());
			return;
		}
		
		final Float[] specYields = worldConf.getSpecificYieldConfig();
		
		// We have specific block yields, so we'll override the 
		// usual minecraft yield handling and handle it ourselves.
		eee.setYield(0.0f);
		
		final float defYield;
		if(worldConf.hasYieldConfig()) {
			defYield = worldConf.getYield();
		} else {
			defYield = eee.getYield();
		}
		
		{
			Float specYield = null; 
			for(final Block b : eee.blockList()) {
				specYield = specYields[b.getTypeId()];
				MCNative.dropBlockItem(b, (specYield != null) ? specYield : defYield);
			}
		}
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] { TippingPoint.CAN_CHANGE_EXPLOSION_YIELD };
	}
	

	@Override
	public Type[] getBukkitEventsRequired() {
		return new Type[] { Event.Type.ENTITY_EXPLODE };
	}
	
	@Override
	public boolean isNeededBy(final EntityConf thisConfig) {
		return thisConfig.hasYieldConfig() || thisConfig.hasSpecificYieldConfig(); 
	}
}
