package toadmess.explosives;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimedEvent;
import org.bukkit.util.config.Configuration;

public class ExplodingListener extends EntityListener {
	private Class<?> entityType;
	
	private final float radiusMultiplier;
	private final boolean fire;

	public ExplodingListener(final Configuration conf, final Class<? extends Entity> entityType) {	
		final String confPath = HEMain.CONF_ENTITIES + "." + entityType.getName() + ".";
		
		this.fire = conf.getBoolean(confPath + HEMain.CONF_FIRE, false);
		this.radiusMultiplier = Math.max(0.0f, (float) conf.getDouble(confPath + HEMain.CONF_RADIUS_MULT, 2.0f));

		this.entityType = entityType; 
	}
	
	@Override
	public void onExplosionPrimed(final ExplosionPrimedEvent event) {
		final Entity primed = event.getEntity();
		
		if(!isValidPrimedEntity(primed)) {
			return;
		}
		
		event.setRadius(this.radiusMultiplier * event.getRadius());
		event.setFire(this.fire);
	}
	
	private boolean isValidPrimedEntity(final Entity e) {
		return (null != e && this.entityType.isInstance(e));
	}
}
