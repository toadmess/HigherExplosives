package toadmess.explosives;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;
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
	private final ExplodedConf defConf;
	private final HashMap<String, ExplodedConf> otherWorldConfs;
	
	public ExplodedListener(final Configuration conf) {
		this.defConf = new ExplodedConf(conf, HEMain.CONF_EVERY);
		
		if(HEMain.IS_DEBUG_CONF) {
			System.out.println("Default yield config is " + this.defConf);
		}
		
		this.otherWorldConfs = new HashMap<String, ExplodedConf>();
		for(final String worldName : conf.getKeys(HEMain.CONF_WORLDS)) {
			final String worldConfPath = HEMain.CONF_WORLDS + "." + worldName + "." + HEMain.CONF_EVERY;

			if(null != conf.getProperty(worldConfPath)) {				
				final ExplodedConf worldConf = new ExplodedConf(conf, worldConfPath);
				
				this.otherWorldConfs.put(worldName, worldConf);
				if(HEMain.IS_DEBUG_CONF) {
					System.out.println(worldName + " yield config is " + worldConf);
				}
			}
			
		}

	}
	
	@Override
	public void onEntityExplode(final EntityExplodeEvent event) {
		final Location epicentre = event.getLocation();
		if(null == epicentre) {
			return;
		}

		final ExplodedConf worldConf = findWorldConf(epicentre.getWorld());
		
		if(!worldConf.yieldChangeAllowedBounds.isWithinBounds(event.getLocation())) {
			return;
		}
		
		event.setYield(worldConf.yield);
	}
	
	private ExplodedConf findWorldConf(final World world) {
		final String worldName = world.getName();
		if(this.otherWorldConfs.containsKey(worldName)) {
			return this.otherWorldConfs.get(worldName);
		}
		
		return this.defConf;
	}
	
	protected class ExplodedConf {
		public final float yield;
		
		public final Bounds yieldChangeAllowedBounds;
		
		public ExplodedConf(final Configuration conf, final String confPath) {
			this.yield = Math.max(0.0f, Math.min(1.0f, (float) conf.getDouble(confPath + "." + HEMain.CONF_EVERY_YIELD, 0.3f)));
			
			this.yieldChangeAllowedBounds = new Bounds(conf, confPath + "." + HEMain.CONF_EVERY_YIELD_BOUNDS);
		}
		
		@Override
		public String toString() {
			return "ExplodedConf("+HEMain.CONF_EVERY_YIELD+"="+yield+","+HEMain.CONF_EVERY_YIELD_BOUNDS+"="+yieldChangeAllowedBounds+")";
		}
	}
}
