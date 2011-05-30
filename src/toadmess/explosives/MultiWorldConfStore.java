package toadmess.explosives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

import toadmess.explosives.events.Handler;
import toadmess.explosives.events.handlers.HandleDamageCreature;
import toadmess.explosives.events.handlers.HandleDamageItem;
import toadmess.explosives.events.handlers.HandleDamagePlayer;
import toadmess.explosives.events.handlers.HandleFire;
import toadmess.explosives.events.handlers.HandlePreventTerrainDamage;
import toadmess.explosives.events.handlers.HandleRadius;
import toadmess.explosives.events.handlers.HandleYield;
import toadmess.explosives.events.handlers.TNTTracker;

/**
 * Contains all of the different EntityConf instances for all worlds and all entities.
 * It is used to procure a relevant configuration for a specific event. 
 * 
 * @author John Revill
 */
public class MultiWorldConfStore implements ConfConstants {
	/**
	 * Maps the entity type to a map. The returned map maps the world name to the config to use. 
	 */
	private final Map<Class<? extends Entity>, Map<String, EntityConf>> worldConfs = new HashMap<Class<? extends Entity>, Map<String, EntityConf>>();

	/** 
	 * This is the name used to reference the default world.
	 * Used to key into the map of ExplodingConf to get the default world config
	 */
	public static final String DEF_WORLD_NAME = "";
	
	public final Logger log;
	
	public MultiWorldConfStore(final Logger log) {
		this.log = log;
	}
	
	public void readConfsForEntity(final Class<? extends Entity> entityType, final Configuration conf) {
		// Get the unqualified class name of the entity. This is used for looking it up in the configuration.
		final String entityName = entityType.getName().substring(entityType.getName().lastIndexOf('.')+1);
		
		final String confEntityPath = CONF_ENTITIES + "." + entityName;
		
		final boolean isDebugConf = conf.getBoolean(CONF_DEBUGCONFIG, false);

		final EntityConf defWorldConfig = new EntityConf(conf, confEntityPath, log);
		this.add(defWorldConfig, entityType, MultiWorldConfStore.DEF_WORLD_NAME);
		if(isDebugConf) {
			if(defWorldConfig.isEmptyConfig()) {
				log.info("HigherExplosives: There is no default config for " + entityName + ". Those explosions will be left unaffected unless they have a world specific configuration.");				
			} else {				
				log.info("HigherExplosives: Default config for " + entityName + " is:\n" + defWorldConfig);
			}
		}

		final List<String> worldNames = conf.getKeys(CONF_WORLDS);
		if(null != worldNames) {
			for(final String worldName : worldNames) {
				final String worldEntityPath = CONF_WORLDS + "." + worldName + "." + confEntityPath;
			
				if(null != conf.getProperty(worldEntityPath)) {
					final EntityConf worldConf = new EntityConf(conf, worldEntityPath, this.log);
					
					this.add(worldConf, entityType, worldName);
					if(isDebugConf) {
						if(!worldConf.isEmptyConfig()) {
							this.log.info("HigherExplosives: World \"" + worldName + "\" config for " + entityName + " is " + worldConf);
						}
					}
				}
			}
		}
	}
	
	private void add(final EntityConf conf, final Class<? extends Entity> entityClass, final String worldName) {
		final Map<String, EntityConf> worldConfMap;
		
		if(this.worldConfs.containsKey(entityClass)) {
			worldConfMap = this.worldConfs.get(entityClass); 
		} else {
			worldConfMap = new HashMap<String, EntityConf>();
		}
		
		worldConfMap.put(worldName, conf);
		worldConfs.put(entityClass, worldConfMap);
	}
	
	/**
	 * Gets an active config (within the active bounds) for the given entity type at the given world location.
	 * 
	 * @param entityType
	 * @param location
	 * 
	 * @return Null if there is no active configuration at the given location for the given type.
	 */
	public EntityConf getActiveConf(final Class<? extends Entity> entityType, final Location location) {
		if(null == location) {
			return null;
		}
		
		final EntityConf ec = procure(entityType, location);
		
		if(ec.getActiveBounds().isWithinBounds(location)) {
			return ec;
		}
		return null;
	}
	
	public EntityConf procure(final Class<? extends Entity> entityClass, final Location epicentre) {
		return procure(entityClass, epicentre.getWorld().getName());
	}
	
	public EntityConf procure(final Class<? extends Entity> entityClass, final String worldName) {
		final Map<String, EntityConf> worldConfMap = this.worldConfs.get(entityClass);
		
		if(worldConfMap.containsKey(worldName)) {
			return worldConfMap.get(worldName);
		}
		
		return worldConfMap.get(DEF_WORLD_NAME);
	}
	
	/**
	 * Finds all the event types required by any and all configs in all worlds.
	 * @return A collection of the event types that we should register and listen to
	 */
	public Set<Type> getNeededBukkitEvents() {
		final HashSet<Event.Type> neededEvents = new HashSet<Event.Type>();
		
		for(final Map<String, EntityConf> worldConfMap : this.worldConfs.values()) {
			final List<EntityConf> allConfs = new ArrayList<EntityConf>();
			allConfs.addAll(worldConfMap.values());
			allConfs.add(worldConfMap.get(DEF_WORLD_NAME));
			
			for(final EntityConf c : allConfs) {
				if(c.hasFireConfig() || c.hasRadiusConfig()) {
					neededEvents.add(Event.Type.EXPLOSION_PRIME);
				}
				
				if(c.hasPreventTerrainDamageConfig() || c.hasYieldConfig()) {
					neededEvents.add(Event.Type.ENTITY_EXPLODE);
				}
				
				if(c.hasPlayerDamageConfig() || c.hasCreatureDamageConfig() || c.hasItemDamageConfig()) {
					neededEvents.add(Event.Type.ENTITY_DAMAGE);
				}
				
				if(c.hasTNTFuseConfig()) {
					neededEvents.add(Event.Type.BLOCK_DAMAGE);
					neededEvents.add(Event.Type.BLOCK_BURN);
					neededEvents.add(Event.Type.ENTITY_EXPLODE);
					neededEvents.add(Event.Type.BLOCK_PHYSICS);
				}
			}
			
		}
		return neededEvents;
	}
	
	public Set<Handler> getNeededHandlers(final Plugin heMain) {
		final LinkedHashSet<Handler> neededHandlers = new LinkedHashSet<Handler>();
		
		// Have just single instances of these. We don't want duplicate handlers in the returned Set
		final HandleFire handleFire = new HandleFire();
		final HandleRadius handleRadius = new HandleRadius();
		final HandlePreventTerrainDamage handlePreventTerrainDamage = new HandlePreventTerrainDamage();
		final HandleYield handleYield = new HandleYield();
		final HandleDamagePlayer handleDamagePlayer = new HandleDamagePlayer();
		final HandleDamageCreature handleDamageCreature = new HandleDamageCreature();
		final HandleDamageItem handleDamageItem = new HandleDamageItem();
		final TNTTracker tntTracker = new TNTTracker(heMain);
		
		for(final Map<String, EntityConf> worldConfMap : this.worldConfs.values()) {
			final List<EntityConf> allConfs = new ArrayList<EntityConf>();
			allConfs.addAll(worldConfMap.values());
			allConfs.add(worldConfMap.get(DEF_WORLD_NAME));
			
			for(final EntityConf c : allConfs) {
				if(c.hasFireConfig()) neededHandlers.add(handleFire);
				if(c.hasRadiusConfig()) neededHandlers.add(handleRadius);
				
				if(c.hasPreventTerrainDamageConfig()) neededHandlers.add(handlePreventTerrainDamage);
				if(c.hasYieldConfig()) neededHandlers.add(handleYield);
					
				if(c.hasPlayerDamageConfig()) neededHandlers.add(handleDamagePlayer);
				if(c.hasCreatureDamageConfig()) neededHandlers.add(handleDamageCreature);
				if(c.hasItemDamageConfig()) neededHandlers.add(handleDamageItem);
				
				if(c.hasTNTFuseConfig()) neededHandlers.add(tntTracker);
			}
			
		}
		return neededHandlers;
	}
}