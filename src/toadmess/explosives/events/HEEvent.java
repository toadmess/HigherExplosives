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

import toadmess.explosives.EntityConf;
import toadmess.explosives.MultiWorldConfStore;

public class HEEvent {
	public final TippingPoint type;
	public final Event event;
	public final MultiWorldConfStore confStore;
	
	private EntityConf applicableConfig = null;
	
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
			this.applicableConfig = this.confStore.getActiveConf(getConfigEntityClass(), getEventLocation());
		}
		return this.applicableConfig;
	}
	
	public Location getEventLocation() {
		switch(this.type) {
		case CAN_CHANGE_EXPLOSION_RADIUS:
		case CAN_CHANGE_EXPLOSION_FIRE_FLAG:
		case CAN_CHANGE_EXPLOSION_YIELD:
		case CAN_PREVENT_TERRAIN_DAMAGE:
			return ((EntityEvent)event).getEntity().getLocation();
			
		case CAN_CHANGE_PLAYER_DAMAGE:
		case CAN_CHANGE_CREATURE_DAMAGE:
		case CAN_CHANGE_ITEM_DAMAGE:
			return ((EntityDamageByEntityEvent) event).getDamager().getLocation();
			
		case TNT_PRIMED_BY_EXPLOSION:
		case TNT_PRIMED_BY_FIRE:
		case TNT_PRIMED_BY_PLAYER:	
		case TNT_PRIMED_BY_REDSTONE:
			return ((BlockEvent) event).getBlock().getLocation();
			
		default: 
			System.err.println("HEEvent.getEventLocation(): Not sure what the location is of unknown event type " + type);
			return null;
		}
	}
	
	/**
	 * @return The class of the bukkit entity - the name of which corresponds to 
	 * the section in the config.yml file that we want to use for this event.  
	 */
	public Class<? extends Entity> getConfigEntityClass() {
		final Entity relevantEntity;
		
		switch(this.type) {
		case CAN_CHANGE_EXPLOSION_RADIUS:
		case CAN_CHANGE_EXPLOSION_FIRE_FLAG:
		case CAN_CHANGE_EXPLOSION_YIELD:
		case CAN_PREVENT_TERRAIN_DAMAGE:
			relevantEntity = ((EntityEvent)event).getEntity();
			break;
			
		case CAN_CHANGE_PLAYER_DAMAGE:
		case CAN_CHANGE_CREATURE_DAMAGE:
		case CAN_CHANGE_ITEM_DAMAGE:
			relevantEntity = ((EntityDamageByEntityEvent) event).getDamager();
			break;
			
		case TNT_PRIMED_BY_EXPLOSION:
		case TNT_PRIMED_BY_FIRE:
		case TNT_PRIMED_BY_PLAYER:	
		case TNT_PRIMED_BY_REDSTONE:
			return TNTPrimed.class;
			
		default: 
			System.err.println("HEEvent.getApplicableConfig(): not sure how to get the config for event type " + type);
			return Chicken.class;
		}
		
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
	}
		
	@Override
	public String toString() {
		return "HEEvent("+type+", "+event+")";
	}
}
