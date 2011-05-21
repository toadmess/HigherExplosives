package toadmess.explosives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

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
		
	public void add(final EntityConf conf, final Class<? extends Entity> entityClass, final String worldName) {
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
		System.out.println("MultiWorldConfStore.procure("+entityClass+","+worldName+")");
		
		if(worldConfMap.containsKey(worldName)) {
			return worldConfMap.get(worldName);
		}
		
		return worldConfMap.get(DEF_WORLD_NAME);
	}
	
	/**
	 * Finds all the event types required by any and all configs in all worlds.
	 * @return A collection of the event types that we should register and listen to
	 */
	public Set<Type> getNeededEvents(final PluginManager pm, final Plugin heMain, final Class<? extends Entity> entityClass) {
		final Map<String, EntityConf> worldConfMap = this.worldConfs.get(entityClass);
		
		final List<EntityConf> allConfs = new ArrayList<EntityConf>();
		allConfs.addAll(worldConfMap.values());
		allConfs.add(worldConfMap.get(DEF_WORLD_NAME));
		
		final HashSet<Event.Type> neededEvents = new HashSet<Event.Type>();
		
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
		
		return neededEvents;
	}
}