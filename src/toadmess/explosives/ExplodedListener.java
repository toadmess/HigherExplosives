package toadmess.explosives;

import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.util.config.Configuration;

/**
 * This listens to all explosions after they have exploded. 
 * It simply sets the yield of the explosion (percentage of blocks that drop items).
 * 
 * @author John Revill
 */
public class ExplodedListener extends EntityListener {
	private final float yield;
	
	private final Bounds yieldChangeAllowedBounds;
	
	public ExplodedListener(final Configuration conf) {	
		this.yield = Math.max(0.0f, Math.min(1.0f, (float) conf.getDouble(HEMain.CONF_EVERY + "." + HEMain.CONF_EVERY_YIELD, 0.3f)));
		
		this.yieldChangeAllowedBounds = new Bounds(conf, HEMain.CONF_EVERY + "." + HEMain.CONF_EVERY_YIELD_BOUNDS);
	}
	
	@Override
	public void onEntityExplode(final EntityExplodeEvent event) {
		if(!this.yieldChangeAllowedBounds.isWithinBounds(event.getLocation())) {
			return;
		}
		
		event.setYield(this.yield);
	}
}
