package toadmess.explosives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
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
public class ExplosionListener extends EntityListener {
	private final Logger log;
	
	private final Class<?> entityType;
	
	private final ExplodingConf defWorldConf;
	
	private final HashMap<String, ExplodingConf> otherWorldConfs;
	
	// Keep track of the listeners that we have registered so we know 
	// what we have after the plugin has been enabled. 
	// We may need to register more listeners later on (e.g. MiningTNT workaround or commands etc.).  
	private final HashSet<Event.Type> registeredListeners = new HashSet<Event.Type>();
	
	public ExplosionListener(final Configuration conf, final Logger log, final Class<? extends Entity> entityType) {
		this.log = log;
		
		// Get the unqualified class name of the entity. This is used for looking it up in the configuration.
		final String entityName = entityType.getName().substring(entityType.getName().lastIndexOf('.')+1);
		
		final String confEntityPath = HEMain.CONF_ENTITIES + "." + entityName;
		
		final boolean isDebugConf = conf.getBoolean(HEMain.CONF_DEBUGCONFIG, false);
		
		this.defWorldConf = new ExplodingConf(conf, confEntityPath, this.log);
		if(isDebugConf) {
			if(defWorldConf.isEmptyConfig()) {
				this.log.info("HigherExplosives: There is no default config for " + entityName + ". Those explosions will be left unaffected unless they have a world specific configuration.");				
			} else {				
				this.log.info("HigherExplosives: Default config for " + entityName + " is:\n" + this.defWorldConf);
			}
		}
		
		this.otherWorldConfs = new HashMap<String, ExplodingConf>();
		final List<String> worldNames = conf.getKeys(HEMain.CONF_WORLDS);
		if(null != worldNames) {
			for(final String worldName : worldNames) {
				final String worldEntityPath = HEMain.CONF_WORLDS + "." + worldName + "." + confEntityPath;
			
				if(null != conf.getProperty(worldEntityPath)) {
					final ExplodingConf worldConf = new ExplodingConf(conf, worldEntityPath, this.log);
					
					this.otherWorldConfs.put(worldName, worldConf);
					if(isDebugConf) {
						if(!worldConf.isEmptyConfig()) {
							this.log.info("HigherExplosives: World \"" + worldName + "\" config for " + entityName + " is " + worldConf);
						}
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

		if(event.isCancelled()) {
			return;
		}
		
		if(worldConf.hasRadiusConfig()) {
			event.setRadius(worldConf.getNextRadiusMultiplier() * event.getRadius());
		}
		
		if(worldConf.hasFireConfig()) {
			event.setFire(worldConf.getFire());
		}
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
		
		if(event.isCancelled()) {
			return;
		}
		
		if(damagee instanceof CraftPlayer) {
			if(worldConf.hasPlayerDamageConfig()) {
				event.setDamage((int) (event.getDamage() * worldConf.getNextPlayerDamageMultiplier()));
			}
		} else {
			if(worldConf.hasCreatureDamageConfig()) {
				event.setDamage((int) (event.getDamage() * worldConf.getNextCreatureDamageMultiplier()));
			}
		}
	}

	@Override
	public void onEntityExplode(final EntityExplodeEvent event) {
		final Location epicentre = event.getLocation();
		if(null == epicentre) {
			return;
		}
		
		if(!isCorrectEntity(event.getEntity())) {
			return;
		}
		
		final ExplodingConf worldConf = findWorldConf(epicentre.getWorld());
		
		if(!worldConf.getActiveBounds().isWithinBounds(epicentre)) {
			return;
		}
		
		if(event.isCancelled()) {
			return;
		}
		
		if(worldConf.hasYieldConfig()) {
			event.setYield(worldConf.getYield());
		}
		
		if(worldConf.hasPreventTerrainDamageConfig() && worldConf.getPreventTerrainDamage()) {			
			event.setCancelled(true);
		}
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

	/**
	 * Registers event listeners if they're needed by the config.
	 * This is safely re-runnable without registering a listener more than once (in 
	 * cases where the config somehow changes, e.g. commands).
	 */
	public void registerNeededEvents(final PluginManager pm, final Plugin heMain) {
		final List<ExplodingConf> allConfs = new ArrayList<ExplodingConf>();
		allConfs.addAll(this.otherWorldConfs.values());
		allConfs.add(this.defWorldConf);
		
		final HashSet<Event.Type> neededEvents = new HashSet<Event.Type>();
		
		for(final ExplodingConf c : allConfs) {
			if(c.hasFireConfig() || c.hasRadiusConfig()) {
				neededEvents.add(Event.Type.EXPLOSION_PRIME);
			}
			
			if(c.hasPreventTerrainDamageConfig() || c.hasYieldConfig()) {
				neededEvents.add(Event.Type.ENTITY_EXPLODE);
			}
			
			if(c.hasPlayerDamageConfig() || c.hasCreatureDamageConfig()) {
				neededEvents.add(Event.Type.ENTITY_DAMAGE);
			}
		}
		
		for(final Event.Type evType : neededEvents) {
			if(!this.registeredListeners.contains(evType)) { // Only register if we haven't done so before				
				pm.registerEvent(evType, this, Event.Priority.Normal, heMain);
				this.registeredListeners.add(evType);
			}
		}
	}
}
