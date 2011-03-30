package toadmess.explosives;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
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
	
	private final ExplodingConf defWorldConf;
	
	private final HashMap<String, ExplodingConf> otherWorldConfs;
	
	public ExplodingListener(final Configuration conf, final Class<? extends Entity> entityType) {
		// Get the unqualified class name of the entity. This is used for looking it up in the configuration.
		final String entityName = entityType.getName().substring(entityType.getName().lastIndexOf('.')+1);
		
		final String confEntityPath = HEMain.CONF_ENTITIES + "." + entityName;
		
		this.defWorldConf = new ExplodingConf(conf, confEntityPath);
		if(HEMain.IS_DEBUG_CONF) {
			System.out.println("Default config for " + entityName + " is " + this.defWorldConf);
		}
		
		this.otherWorldConfs = new HashMap<String, ExplodingConf>();
		final List<String> worldNames = conf.getKeys(HEMain.CONF_WORLDS);
		if(null != worldNames) {
			for(final String worldName : worldNames) {
				final String worldEntityPath = HEMain.CONF_WORLDS + "." + worldName + "." + confEntityPath;
			
				if(null != conf.getProperty(worldEntityPath)) {
					final ExplodingConf worldConf = new ExplodingConf(conf, worldEntityPath);
					
					this.otherWorldConfs.put(worldName, worldConf);
					if(HEMain.IS_DEBUG_CONF) {
						System.out.println(worldName + " config for " + entityName + " is " + worldConf);
					}
				}
			}
		}
		
		this.entityType = entityType; 
	}

	@Override
	public void onExplosionPrime(final ExplosionPrimeEvent event) {
		final Entity primed = event.getEntity();

		if(!isCorrectEntity(primed)) {
			return;
		}

		final Location epicentre = primed.getLocation();
		final ExplodingConf worldConf = findWorldConf(epicentre.getWorld());
		
		if(!worldConf.getActiveBounds().isWithinBounds(epicentre)) {
			return;
		}
		
		event.setRadius(worldConf.getNextRadiusMultiplier() * event.getRadius());
		event.setFire(worldConf.getFire());
	}

	@Override
	public void onEntityDamage(final EntityDamageEvent event) {
		final Entity damager;
		if(event instanceof EntityDamageByEntityEvent) {
			damager = ((EntityDamageByEntityEvent) event).getDamager();
			if(!isCorrectEntity(damager)) {
				return;
			}
		} else {
			return;
		}
		
		if(event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
			return;
		}
		
		final Entity damagee = event.getEntity();
		if(!(damagee instanceof CraftLivingEntity)) {
			return;
		}

		final Location epicentre = damager.getLocation();
		final ExplodingConf worldConf = findWorldConf(epicentre.getWorld());
		
		if(!worldConf.getActiveBounds().isWithinBounds(epicentre)) {
			return;
		}
		
		final float damageMultiplier;
		if(damagee instanceof CraftPlayer) {
			damageMultiplier = worldConf.getNextPlayerDamageMultiplier();
		} else {
			damageMultiplier = worldConf.getNextCreatureDamageMultiplier();
		}
		
		event.setDamage((int) (event.getDamage() * damageMultiplier));
	}
	
	private boolean isCorrectEntity(final Entity e) {
		return (null != e && this.entityType.isInstance(e));
	}
	
	private ExplodingConf findWorldConf(final World world) {
		final String worldName = world.getName();
		if(this.otherWorldConfs.containsKey(worldName)) {
			return this.otherWorldConfs.get(worldName);
		}
		
		return this.defWorldConf;
	}
}
