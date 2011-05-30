package toadmess.explosives;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

public class ExplosionListener implements ConfConstants {
	private final MultiWorldConfStore confStore;
	
	private final Plugin plugin;
	
	// Keep track of the event listeners that we have registered so we know 
	// what we have after the plugin has been enabled. 
	// We may need to register more listeners later on (e.g. MiningTNT workaround or commands etc.).  
	private final HashSet<Event.Type> registeredEvents = new HashSet<Event.Type>();
	
	private final EntityListener entityListener;
	private final BlockListener blockListener;
	
	private final Handler handler ;
	
	public ExplosionListener(final Plugin plugin, final Handler defaultHandler, final MultiWorldConfStore confStore) {
		this.plugin = plugin;
		this.confStore = confStore;
		this.handler = defaultHandler;
		
		this.entityListener = new EntityListenerImpl();
		this.blockListener = new BlockListenerImpl(this.plugin);
	}

	public class EntityListenerImpl extends EntityListener {
		@Override
		public void onExplosionPrime(final ExplosionPrimeEvent event) {
			if(event.isCancelled()) {
				return;
			}

			handler.handle(new HEEvent(TippingPoint.CAN_CHANGE_EXPLOSION_RADIUS, event, confStore));
			handler.handle(new HEEvent(TippingPoint.CAN_CHANGE_EXPLOSION_FIRE_FLAG, event, confStore));
		}

		@Override
		public void onEntityDamage(final EntityDamageEvent event) {
			if(!(event instanceof EntityDamageByEntityEvent)) {
				return;
			}
			
			if(event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
				return;
			}
			
			if(event.isCancelled()) {
				return;
			}
			
			final Entity damagee = event.getEntity();
			
			if(damagee instanceof Player) {
				handler.handle(new HEEvent(TippingPoint.CAN_CHANGE_PLAYER_DAMAGE, event, confStore));
			} else if(damagee instanceof LivingEntity){
				handler.handle(new HEEvent(TippingPoint.CAN_CHANGE_CREATURE_DAMAGE, event, confStore));
			} else {
				handler.handle(new HEEvent(TippingPoint.CAN_CHANGE_ITEM_DAMAGE, event, confStore));
			}
		}

		@Override
		public void onEntityExplode(final EntityExplodeEvent event) {
			final Location epicentre = event.getLocation();
			if(null == epicentre) {
				return;
			}
			
			final Entity smithereens = event.getEntity();
			
			if(smithereens instanceof TNTPrimed) {
				handler.handle(new HEEvent(TippingPoint.TNT_FUSE_HAS_RAN_OUT, event, confStore));
			}
				
			if(event.isCancelled()) {
				return;
			}
			
			// For every explosion type, we always want to make sure that if any TNT that 
			// gets caught in the blast, it will have it's fuse tweaked.
			handler.handle(new HEEvent(TippingPoint.ENTITY_EXPLODING, event, confStore));
			
			handler.handle(new HEEvent(TippingPoint.CAN_CHANGE_EXPLOSION_YIELD, event, confStore));
			handler.handle(new HEEvent(TippingPoint.CAN_PREVENT_TERRAIN_DAMAGE, event, confStore));
		}		
	}
	
	public class BlockListenerImpl extends BlockListener {
		final Plugin pluginRef;
		public BlockListenerImpl(final Plugin heMain) {
			this.pluginRef = heMain;
		}

		private boolean commonTNTBlockChecks(final BlockEvent event) {
			final Block damaged = event.getBlock();
			if(damaged == null || damaged.getType() != Material.TNT) {
				return false;
			}
			
			if(((Cancellable) event).isCancelled()) {
				return false;
			}
			
			return true;
		}
		
		@Override
		public void onBlockDamage(final BlockDamageEvent event) {
			if(!commonTNTBlockChecks(event)) {
				return;
			}
			
			if(null == event.getPlayer()) {
				return;
			}

			handler.handle(new HEEvent(TippingPoint.TNT_PRIMED_BY_PLAYER, event, confStore));
		}
		
		@Override
		public void onBlockPhysics(final BlockPhysicsEvent event) {
			if(!commonTNTBlockChecks(event)) {
				return;
			}
			
			if(event.getBlock().getBlockPower() <= 0) {
				return;
			}
			
			handler.handle(new HEEvent(TippingPoint.TNT_PRIMED_BY_REDSTONE, event, confStore));
		}
		
		@Override
		public void onBlockBurn(final BlockBurnEvent event) {
			if(!commonTNTBlockChecks(event)) {
				return;
			}
			
			handler.handle(new HEEvent(TippingPoint.TNT_PRIMED_BY_FIRE, event, confStore));
		}
	}
		
	/**
	 * Registers event listeners if they're needed by the config.
	 * This is safely re-runnable without registering a listener more than once (in 
	 * cases where the config somehow changes, e.g. commands).
	 */
	public void registerNeededEvents(final PluginManager pm, final Plugin heMain) {
		for(final Event.Type evType : this.confStore.getNeededBukkitEvents(pm, heMain)) {
			if(!this.registeredEvents.contains(evType)) { // Only register if we haven't done so before				
				switch(evType.getCategory()) {
				case LIVING_ENTITY:
					pm.registerEvent(evType, this.entityListener, Event.Priority.Normal, heMain);
					break;
				case BLOCK:
					pm.registerEvent(evType, this.blockListener, Event.Priority.Normal, heMain);
					break;
				}
				this.registeredEvents.add(evType);
			}
		}
	}
	
	public EntityListener getEntityListener() {return this.entityListener;}
	public BlockListener getBlockListener() {return this.blockListener;}
}
