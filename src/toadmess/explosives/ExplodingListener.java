package toadmess.explosives;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimedEvent;
import org.bukkit.util.config.Configuration;

/**
 * <p>
 * This listens to all explosions just before they explode. 
 * Each instance of this listener will listen to a specific type of explosive entity (e.g. Creeper, 
 * TNTPrimed, or Fireball).
 * </p><p>
 * When an explosion is about to happen, this listener will check whether the epicentre of the 
 * explosion lies within some allowed bounds (an optional configuration). If the epicentre is 
 * outside of these bounds, the explosion is left untouched. If there are no bounds, or the 
 * epicentre is within the acceptable bounds, then:
 * </p>
 * <ul>
 * <li>The explosion's radius is multiplied by the entity's "radiusMultiplier" configuration value.
 * <li>The explosion will be flagged as causing fire if the "fire" configuration is true.
 * </ul>
 * 
 * @author John Revill
 */
public class ExplodingListener extends EntityListener {
	private final Class<?> entityType;
	
	private final float radiusMultiplier;
	private final boolean fire;

	private final Bounds allowedBounds;

	public ExplodingListener(final Configuration conf, final Class<? extends Entity> entityType) {
		// Get the unqualified class name of the entity. This is used for looking it up in the configuration.
		final String entityName = entityType.getName().substring(entityType.getName().lastIndexOf('.')+1);
		
		final String confPath = HEMain.CONF_ENTITIES + "." + entityName + ".";
		
		this.entityType = entityType; 

		this.fire = conf.getBoolean(confPath + HEMain.CONF_FIRE, false);
		this.radiusMultiplier = Math.max(0.0f, (float) conf.getDouble(confPath + HEMain.CONF_ENTITY_RADIUS_MULT, 1.0f));
		
		this.allowedBounds = new Bounds(conf, confPath + HEMain.CONF_BOUNDS);
	}

	@Override
	public void onExplosionPrimed(final ExplosionPrimedEvent event) {
		final Entity primed = event.getEntity();
				
		if(!isValidPrimedEntity(primed)) {
			return;
		}

		if(!this.allowedBounds.isWithinBounds(primed.getLocation())) {
			return;
		}
		
		event.setRadius(this.radiusMultiplier * event.getRadius());
		event.setFire(this.fire);
	}

	private boolean isValidPrimedEntity(final Entity e) {
		return (null != e && this.entityType.isInstance(e));
	}
}
