package toadmess.explosives;

import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.util.config.Configuration;

public class ExplodedListener extends EntityListener {
	private final float yield;
	
	public ExplodedListener(final Configuration conf) {	
		this.yield = Math.max(0.0f, Math.min(1.0f, (float) conf.getDouble(HEMain.CONF_YIELD, 0.3f)));
	}
	
	@Override
	public void onEntityExplode(final EntityExplodeEvent event) {
		event.setYield(this.yield);
	}
}
