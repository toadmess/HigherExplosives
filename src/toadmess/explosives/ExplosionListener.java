package toadmess.explosives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
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
public class ExplosionListener {
	private final Logger log;
	
	private final Class<?> entityType;
	
	private final ExplodingConf defWorldConf;
	
	private final Map<String, ExplodingConf> otherWorldConfs;
	
	// Keep track of the listeners that we have registered so we know 
	// what we have after the plugin has been enabled. 
	// We may need to register more listeners later on (e.g. MiningTNT workaround or commands etc.).  
	private final HashSet<Event.Type> registeredEvents = new HashSet<Event.Type>();
	
	/** 
	 * Contains IDs for TNTPrimed entities that have recently had their fuse lengths modified.
	 * TODO: Move TNT fuse handling somewhere else. It's polluting this class.
	 */
	private final Set<Integer> handledTNTPrimedEntities = new HashSet<Integer>();
	/** 
	 * The Task ID of the last scheduled task for finding new TNTPrimed entities, within specific worlds. Maps world name to Task ID.
	 * TODO: Move TNT fuse stuff out elsewhere. It's polluting this class. 
	 */
	private static Map<String,Integer> lastTntPrimedSearchTaskIDs = new HashMap<String,Integer>();
	
	private final Plugin plugin;
	
	private final EntityListener entityHandler;
	private final BlockListener blockHandler;
	
	public ExplosionListener(final Plugin plugin, final Logger log, final Class<? extends Entity> entityType) {
		this.log = log;
		
		this.plugin = plugin;
		
		// Get the unqualified class name of the entity. This is used for looking it up in the configuration.
		final String entityName = entityType.getName().substring(entityType.getName().lastIndexOf('.')+1);
		
		final String confEntityPath = HEMain.CONF_ENTITIES + "." + entityName;
		
		final Configuration conf = plugin.getConfiguration();
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
		
		this.entityHandler = new EntityHandler();
		this.blockHandler = new BlockHandler(this.plugin);
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

	public class EntityHandler extends EntityListener {
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

			final Location epicentre = damager.getLocation();
			final ExplodingConf worldConf = findWorldConf(epicentre.getWorld());
			
			if(!worldConf.getActiveBounds().isWithinBounds(epicentre)) {
				return;
			}
			
			if(event.isCancelled()) {
				return;
			}
			
			if(damagee instanceof Player) {
				if(worldConf.hasPlayerDamageConfig()) {
					event.setDamage((int) (event.getDamage() * worldConf.getNextPlayerDamageMultiplier()));
				}
			} else if(damagee instanceof LivingEntity){
				if(worldConf.hasCreatureDamageConfig()) {
					event.setDamage((int) (event.getDamage() * worldConf.getNextCreatureDamageMultiplier()));
				}
			} else {
				if(worldConf.hasItemDamageConfig()) {
					event.setDamage((int) (event.getDamage() * worldConf.getNextItemDamageMultiplier()));
				}
				return;
			}
		}

		@Override
		public void onEntityExplode(final EntityExplodeEvent event) {
			final Location epicentre = event.getLocation();
			if(null == epicentre) {
				return;
			}
			
			final Entity smithereens = event.getEntity();
			
			final ExplodingConf worldConf = findWorldConf(epicentre.getWorld());
			
			// If we have a fuse config for the TNT listener, detect TNT priming 
			// for all explosion types. 
			if(entityType.isAssignableFrom(TNTPrimed.class) && worldConf.hasTNTFuseConfig()) {
				if(smithereens instanceof TNTPrimed) {
					// Clean up entity ID from our collection of tweaked TNTPrimed entity IDs.
					handledTNTPrimedEntities.remove(smithereens.getEntityId());
				}
				
				// For every explosion type, we always want to make sure that if any TNT that 
				// gets caught in the blast, it will have it's fuse tweaked. 
				dealWithTNTPrimedInBlast(epicentre, worldConf);
			}
			
			if(!isCorrectEntity(event.getEntity())) {
				return;
			}
			
			if(event.isCancelled()) {
				return;
			}
			
			if(!worldConf.getActiveBounds().isWithinBounds(epicentre)) {
				return;
			}
			
			if(worldConf.hasYieldConfig()) {
				event.setYield(worldConf.getYield());
			}
			
			if(worldConf.hasPreventTerrainDamageConfig() && worldConf.getPreventTerrainDamage()) {			
				event.setCancelled(true);
				
				MCNative.playSoundExplosion(epicentre);
			}
		}		
	}
	
	public class BlockHandler extends BlockListener {
		final Plugin pluginRef;
		public BlockHandler(final Plugin heMain) {
			this.pluginRef = heMain;
		}

		@Override
		public void onBlockDamage(final BlockDamageEvent event) {
			final Block damaged = event.getBlock();
			if(damaged.getType() != Material.TNT) {
				return;
			}
			
			final Player fireStarter = event.getPlayer();
			if(null == fireStarter) {
				return;
			}
			
			final Location epicentre = damaged.getLocation();
			final ExplodingConf worldConf = findWorldConf(epicentre.getWorld());
			
			if(!worldConf.getActiveBounds().isWithinBounds(epicentre)) {
				return;
			}

			if(event.isCancelled()) {
				return;
			}
			
			if(worldConf.hasTNTFuseConfig()) {
				dealWithTNTPrimedInBlast(damaged.getLocation(), worldConf);
			}
		}
		
		@Override
		public void onBlockPhysics(final BlockPhysicsEvent event) {
			final Block charged = event.getBlock();
			if(charged.getType() != Material.TNT) {
				return;
			}

			if(charged.getBlockPower() <= 0) {
				return;
			}
			
			if(event.isCancelled()) {
				return;
			}
			
			final Location epicentre = charged.getLocation();
			final ExplodingConf worldConf = findWorldConf(epicentre.getWorld());
			
			if(!worldConf.getActiveBounds().isWithinBounds(epicentre)) {
				return;
			}
			
			if(worldConf.hasTNTFuseConfig()) {
				dealWithTNTPrimedInBlast(charged.getLocation(), worldConf);
			}
		}
		
		@Override
		public void onBlockBurn(final BlockBurnEvent event) {
			if(!entityType.isAssignableFrom(TNTPrimed.class)) {
				// Burn event is only relevant for TNT entity listeners (to detect when TNT is rendered aflame).
				return;
			}
			
			final Block burnt = event.getBlock();
			
			if(burnt.getType() != Material.TNT) {
				return;
			}
			
			if(event.isCancelled()) {
				return;
			}
			
			final Location flamingHeart = burnt.getLocation();
			final ExplodingConf worldConf = findWorldConf(flamingHeart.getWorld());
			
			if(!worldConf.getActiveBounds().isWithinBounds(flamingHeart)) {
				return;
			}
			
			if(worldConf.hasTNTFuseConfig()) {
				dealWithTNTPrimedInBlast(burnt.getLocation(), worldConf);
			}
		}
	}
	
	// Called by the TNT listener when any explosion is detected.
	// It will schedule a search, in the next few ticks, for any TNTPrimed entities in 
	// that world which have not yet been re-fused. 
	private void dealWithTNTPrimedInBlast(final Location epicentre, final ExplodingConf worldConf) {
		final String worldName = epicentre.getWorld().getName();
		final BukkitScheduler bs = this.plugin.getServer().getScheduler();
		final Integer lastTaskIDForSearchInThisWorld = ExplosionListener.lastTntPrimedSearchTaskIDs.get(worldName);
		
		if(lastTaskIDForSearchInThisWorld != null && bs.isQueued(lastTaskIDForSearchInThisWorld)) {
			// We've already scheduled a TNTPrimed search in this world.
			return;
		}
		
		final int taskId = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			final Location loc = epicentre.clone();

			@Override
			public void run() {				
				// TODO: Find an efficient bukkit way to get the TNTPrimed entity near the destroyed TNT block.
				for(final Entity e : this.loc.getWorld().getEntities()) {
					if(e instanceof TNTPrimed) {
						if(handledTNTPrimedEntities.add(e.getEntityId())) {
							if(worldConf.getActiveBounds().isWithinBounds(e.getLocation())) {
								MCNative.multiplyTNTFuseDuration(((TNTPrimed) e), worldConf.getNextTNTFuseMultiplier());
							}
						}
					}
				}
			}
		});
		
		ExplosionListener.lastTntPrimedSearchTaskIDs.put(worldName, taskId);
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
			
			if(c.hasPlayerDamageConfig() || c.hasCreatureDamageConfig() || c.hasItemDamageConfig()) {
				neededEvents.add(Event.Type.ENTITY_DAMAGE);
			}
			
			if(c.hasTNTFuseConfig() && this.entityType.isAssignableFrom(TNTPrimed.class)) {
				neededEvents.add(Event.Type.BLOCK_DAMAGE);
				neededEvents.add(Event.Type.BLOCK_BURN);
				neededEvents.add(Event.Type.ENTITY_EXPLODE);
				neededEvents.add(Event.Type.BLOCK_PHYSICS);
			}
		}
		
		for(final Event.Type evType : neededEvents) {
			if(!this.registeredEvents.contains(evType)) { // Only register if we haven't done so before				
				switch(evType.getCategory()) {
				case LIVING_ENTITY:
					pm.registerEvent(evType, this.entityHandler, Event.Priority.Normal, heMain);
					break;
				case BLOCK:
					pm.registerEvent(evType, this.blockHandler, Event.Priority.Normal, heMain);
					break;
				}
				this.registeredEvents.add(evType);
			}
		}
	}
	
	public EntityListener getEntityListener() {return this.entityHandler;}
	public BlockListener getBlockListener() {return this.blockHandler;}
}
