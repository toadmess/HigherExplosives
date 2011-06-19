package toadmess.explosives.events;

import org.bukkit.Location;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;

import toadmess.explosives.config.MultiWorldConfStore;
import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.handlers.TNTTracker;

public class HEEvent {
	public final TippingPoint type;
	public final Event event;
	public final MultiWorldConfStore confStore;
	
	private EntityConf applicableConfig = null;
	private Location eventLocation = null;
	
	/**
	 * TODO: Clean up this tracker. Find a better way to deal with tracking of entities.
	 */
	private static TNTTracker tntTracker = null;
	
	public HEEvent(final TippingPoint interestingEvent, final Event bukkitEvent, final MultiWorldConfStore confStore) {
		this.type = interestingEvent;
		this.event = bukkitEvent;
		this.confStore = confStore;
	}
	
	public boolean hasApplicableConfig() {
		return null != getApplicableConfig();
	}
	
	/**
	 * @param confStore The configuration store to interrogate for the relevant EntityConf.
	 * 
	 * @return An active config for the event's location and event's causal entity. 
	 * Or null if there is no applicable config. 
	 */
	public EntityConf getApplicableConfig() {
		if(this.applicableConfig == null) {
			final EntityConf activeConf = this.confStore.getActiveConf(getConfigEntityClass(), getEventLocation());
			
			this.applicableConfig = getApplicableSubConfig(activeConf);
		}
		return this.applicableConfig;
	}
	
	/**
	 * TODO: Clean up these trackers and get some better structure in place for them.
	 */
	public static void setTNTTracker(final TNTTracker tracker) {
		HEEvent.tntTracker = tracker;
	}
	
	private EntityConf getApplicableSubConfig(final EntityConf fromThisParentConfig) {
		// Get any relevant sub config for this event type
		switch(this.type) {
		case TNT_PRIME_BY_PLAYER:
			if(fromThisParentConfig.hasTNTPrimeByHandConfig()) {
				return fromThisParentConfig.getTNTPrimeByHandConfig();
			}
			break;
		case TNT_PRIME_BY_FIRE:
			if(fromThisParentConfig.hasTNTPrimeByFireConfig()) {
				return fromThisParentConfig.getTNTPrimeByFireConfig();
			}
			break;
		case TNT_PRIME_BY_REDSTONE:
			if(fromThisParentConfig.hasTNTPrimeByRedstoneConfig()) {
				return fromThisParentConfig.getTNTPrimeByRedstoneConfig();
			}
			break;
		case TNT_PRIME_BY_EXPLOSION:
			if(fromThisParentConfig.hasTNTPrimeByExplosionConfig()) {
				return fromThisParentConfig.getTNTPrimeByExplosionConfig();
			}
			break;
		default:
			// This event is not the triggering event, so let's go and find the triggering event..
			final HEEvent triggeringEvent = findTriggeringEvent(getRelevantEntity());
			
			if(triggeringEvent != null) {
				return triggeringEvent.getApplicableConfig();
			}
		}
		
		return fromThisParentConfig;
	}
	
	protected HEEvent findTriggeringEvent(final Entity relevantEntity) {
		if(relevantEntity instanceof TNTPrimed && tntTracker != null) {
			return tntTracker.getTriggerFor((TNTPrimed) relevantEntity);
		} else if(relevantEntity instanceof Creeper) {
			// TODO: Charged creeper sub configs if the creeper in question was charged
		}
		
		return null;
	}
	
	public Location getEventLocation() {
		if(this.eventLocation == null) {		
			switch(this.type) {
			case CAN_CHANGE_EXPLOSION_RADIUS:
			case CAN_CHANGE_EXPLOSION_FIRE_FLAG:
			case CAN_CHANGE_EXPLOSION_YIELD:
			case CAN_PREVENT_TERRAIN_DAMAGE:
			case AN_EXPLOSION:
			case AN_EXPLOSION_CANCELLED:
				this.eventLocation = ((EntityEvent)event).getEntity().getLocation();
				break;
				
			case CAN_CHANGE_PLAYER_DAMAGE:
			case CAN_CHANGE_CREATURE_DAMAGE:
			case CAN_CHANGE_ITEM_DAMAGE:
				this.eventLocation = ((EntityDamageByEntityEvent) event).getDamager().getLocation();
				break;
				
			case TNT_PRIME_BY_FIRE:
			case TNT_PRIME_BY_PLAYER:	
			case TNT_PRIME_BY_REDSTONE:
				this.eventLocation = ((BlockEvent) event).getBlock().getLocation();
				break;
			
			default: 
				System.err.println("HEEvent.getEventLocation(): Not sure what the location is of unknown event type " + this.type);
			}
			
			if(this.eventLocation != null) {
				// Clone the event just in case Location instances are reused and this HEEvent is held onto for a long time. 
				this.eventLocation = this.eventLocation.clone();
			}
		}
		
		return this.eventLocation;
	}
	
	/**
	 * @return The class of the bukkit entity - the name of which corresponds to 
	 * the section in the config.yml file that we want to use for this event.  
	 */
	public Class<? extends Entity> getConfigEntityClass() {
		final Entity relevantEntity = getRelevantEntity();
		
		if(relevantEntity != null) {
			if(relevantEntity instanceof TNTPrimed) {
				return TNTPrimed.class;
			} else if(relevantEntity instanceof Creeper) {
				return Creeper.class;
			} else if(relevantEntity instanceof Fireball) {
				return Fireball.class;
			} else {
				System.err.println("HEEvent.getApplicableConfig(): not sure how to get the config for entity " + relevantEntity);
				return Chicken.class;
			}
		} else {
			switch(this.type) {
			
			case TNT_PRIME_BY_FIRE:
			case TNT_PRIME_BY_PLAYER:	
			case TNT_PRIME_BY_REDSTONE:
			case AN_EXPLOSION:
			case AN_EXPLOSION_CANCELLED:
				return TNTPrimed.class;
			
			default: 
				System.err.println("HEEvent.getApplicableConfig(): not sure how to get the config for event type " + type + ". this="+this);
				return Chicken.class;
			}
		}
	}
	
	/**
	 * @return Where it makes sense, this will return the event's interesting 
	 * entity instance (the one that is the entity we will be looking up configs against).
	 * If there is no bukkit event or entity for the event then null is returned. 
	 */
	protected Entity getRelevantEntity() {
		switch(this.type) {
		case CAN_CHANGE_EXPLOSION_RADIUS:
		case CAN_CHANGE_EXPLOSION_FIRE_FLAG:
		case CAN_CHANGE_EXPLOSION_YIELD:
		case CAN_PREVENT_TERRAIN_DAMAGE:
			return ((EntityEvent)event).getEntity();
			
		case CAN_CHANGE_PLAYER_DAMAGE:
		case CAN_CHANGE_CREATURE_DAMAGE:
		case CAN_CHANGE_ITEM_DAMAGE:
			return ((EntityDamageByEntityEvent) event).getDamager();
			
		default:
			return null;
		}
	}
		
	@Override
	public String toString() {
		return "HEEvent("+type+", "+event+")";
	}
}
