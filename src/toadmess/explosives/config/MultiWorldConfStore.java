package toadmess.explosives.config;
import static toadmess.explosives.config.ConfProps.CONF_DEBUGCONFIG;
import static toadmess.explosives.config.ConfProps.CONF_ENTITIES;
import static toadmess.explosives.config.ConfProps.CONF_WORLDS;

import java.util.ArrayList;
import java.util.Collections;
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
import org.bukkit.util.config.Configuration;

import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.config.entity.EntityConfReader;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.handlers.EventRouter;
import toadmess.explosives.events.handlers.HandleDamageCreature;
import toadmess.explosives.events.handlers.HandleDamageItem;
import toadmess.explosives.events.handlers.HandleDamagePlayer;
import toadmess.explosives.events.handlers.HandleFire;
import toadmess.explosives.events.handlers.HandlePreventTerrainDamage;
import toadmess.explosives.events.handlers.HandleRadius;
import toadmess.explosives.events.handlers.HandleTNTFuse;
import toadmess.explosives.events.handlers.HandleTNTPreventPrime;
import toadmess.explosives.events.handlers.HandleYield;
import toadmess.explosives.events.handlers.TNTTracker;

/**
 * Contains all of the different EntityConf instances for all worlds and all entities.
 * It is used to procure a relevant configuration for a specific event. 
 * 
 * @author John Revill
 */
public class MultiWorldConfStore {
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
		
		final boolean isDebugConf = conf.getBoolean(CONF_DEBUGCONFIG.toString(), false);

		final EntityConf defWorldConfig = new EntityConfReader(conf, confEntityPath, log).readEntityConf();
		this.add(defWorldConfig, entityType, MultiWorldConfStore.DEF_WORLD_NAME);
		if(isDebugConf) {
			if(defWorldConfig.isEmptyConfig()) {
				log.info("HigherExplosives: There is no default config for " + entityName + ". Those explosions will be left unaffected unless they have a world specific configuration.");				
			} else {				
				log.info("HigherExplosives: Default config for " + entityName + " is:\n" + defWorldConfig);
			}
		}

		final List<String> worldNames = conf.getKeys(CONF_WORLDS.toString());
		if(null != worldNames) {
			for(final String worldName : worldNames) {
				final String worldEntityPath = CONF_WORLDS + "." + worldName + "." + confEntityPath;
			
				if(null != conf.getProperty(worldEntityPath)) {
					final EntityConf worldConf = new EntityConfReader(conf, worldEntityPath, this.log).readEntityConf();
					
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
		
		final String worldName = location.getWorld().getName();
		final Map<String, EntityConf> worldConfMap = this.worldConfs.get(entityType);
		
		final EntityConf ec;
		ec = worldConfMap.get(worldConfMap.containsKey(worldName) ? worldName : DEF_WORLD_NAME);
		
		if(ec.getActiveBounds().isWithinBounds(location)) {
			return ec;
		}
		return null;
	}
	
	public Set<EntityConf> allConfigsAndSubConfigs() {
		final Set<EntityConf> allConfigs = new HashSet<EntityConf>();
		
		for(final Map<String, EntityConf> worldConfMap : this.worldConfs.values()) {
			final List<EntityConf> thisWorldsConfs = new ArrayList<EntityConf>();
			thisWorldsConfs.addAll(worldConfMap.values());
			thisWorldsConfs.add(worldConfMap.get(DEF_WORLD_NAME));
			
			for(final EntityConf rootConfig : thisWorldsConfs) {
				allConfigs.add(rootConfig);
				// TODO: Shift this lot into the EntityConf in a method like getAllSubConfigs(). 
				// It shouldn't be necessary to put knowledge of specific sub configs into this class
				if(rootConfig.hasTNTPrimeByHandConfig()) {
					allConfigs.add(rootConfig.getTNTPrimeByHandConfig());
				}
				if(rootConfig.hasTNTPrimeByFireConfig()) {
					allConfigs.add(rootConfig.getTNTPrimeByFireConfig());
				}
				if(rootConfig.hasTNTPrimeByRedstoneConfig()) {
					allConfigs.add(rootConfig.getTNTPrimeByRedstoneConfig());
				}
				if(rootConfig.hasTNTPrimeByExplosionConfig()) {
					allConfigs.add(rootConfig.getTNTPrimeByExplosionConfig());
				}
				if(rootConfig.hasCreeperChargedConfig()) {
					allConfigs.add(rootConfig.getCreeperChargedConfig());
				}
			}
		}
		
		return Collections.unmodifiableSet(allConfigs);
	}	
}