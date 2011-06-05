package toadmess.explosives.events.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import toadmess.explosives.EntityConf;
import toadmess.explosives.MCNative;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

public class TNTTracker implements Handler {
	/**
	 * The Bukkit scheduler task ID scheduled
	 */
	private Integer scheduledTaskID = null;
	
	/**
	 * The HEEvents that need to be associated with the appropriate TNTPrimed entities.
	 */
	private LinkedList<HEEvent> triggerEventsToAssociate = new LinkedList<HEEvent>();

	/** 
	 * Maps already seen TNTPrimed entity's ID to the HEEvent that triggered it.
	 */
	private final Map<Integer, HEEvent> seenTNTPrimedEntities = new HashMap<Integer, HEEvent>();
	
	private final Plugin plugin;
	
	public TNTTracker(final Plugin p) {
		this.plugin = p;
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] {
			TippingPoint.TNT_PRIMED_BY_FIRE,
			TippingPoint.TNT_PRIMED_BY_PLAYER,
			TippingPoint.TNT_PRIMED_BY_REDSTONE,
			TippingPoint.AN_EXPLOSION,
			TippingPoint.AN_EXPLOSION_CANCELLED,
		};
	}
	
	@Override
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!worldConf.hasTNTFuseConfig()) {
			return;
		}
		
		switch(ev.type) {
		case TNT_PRIMED_BY_FIRE:
		case TNT_PRIMED_BY_PLAYER:
		case TNT_PRIMED_BY_REDSTONE:
			associateWithTNTPrimedEntity(ev, worldConf);
			break;
			
		case AN_EXPLOSION_CANCELLED:
			cleanupIfPrimedTNT((EntityExplodeEvent) ev.event);
			break; 
			
		case AN_EXPLOSION:
			cleanupIfPrimedTNT((EntityExplodeEvent) ev.event);
			
			// Some other TNT block may have been caught in this blast, so search for new primed TNTs
			associateWithTNTPrimedEntity(ev, worldConf);
			break;
		}
	}
	
	/**
	 * @param entityID
	 * @return The HEEvent that originally triggered the priming of the TNT 
	 */
	public HEEvent getTriggerFor(final TNTPrimed tnt) {
		return seenTNTPrimedEntities.get(tnt.getEntityId());
	}
	
	private void cleanupIfPrimedTNT(final EntityExplodeEvent ev) {
		final Entity entity = ev.getEntity();
		if(ev.getEntity() instanceof TNTPrimed) {
			seenTNTPrimedEntities.remove(entity.getEntityId());				
		}
	}
	
	// Called when any priming is detected or suspected.
	// It will schedule a search, in the next few ticks, for any TNTPrimed entities in 
	// that world which have not yet been re-fused. 
	private void associateWithTNTPrimedEntity(final HEEvent triggerEvent, final EntityConf worldConf) {		
		this.triggerEventsToAssociate.add(triggerEvent);
		
		final BukkitScheduler bs = this.plugin.getServer().getScheduler();
		
		if(this.scheduledTaskID != null && bs.isQueued(this.scheduledTaskID)) {
			// We've already scheduled a TNTPrimed search
			return;
		}

		final int taskId = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {

			@Override
			public void run() {
				TNTTracker.this.scheduledTaskID = null;
				
				// Find the worlds that we need to search
				final Set<String> worldsToSearch = new HashSet<String>();
				for(final HEEvent triggerEvent : TNTTracker.this.triggerEventsToAssociate) {
					worldsToSearch.add(triggerEvent.getEventLocation().getWorld().getName());
				}
				
				// Go through each world at a time and look for TNTPrimed entities that have not been seen before.
				for(final String worldName : worldsToSearch) {
					final World worldToSearch = TNTTracker.this.plugin.getServer().getWorld(worldName);
					
					System.out.println("Searching for new primed TNT in world " + worldName + "..");
					// TODO: Find an efficient bukkit way to get the TNTPrimed entity near the destroyed TNT block.
					for(final Entity e : worldToSearch.getEntities()) {
						if(e instanceof TNTPrimed) {
							if(!seenTNTPrimedEntities.containsKey(e.getEntityId())) {
								// This TNTPrimed entity has not been seen before. 
								// Try and match it up with it's triggering event's config
								final TNTPrimed tnt = (TNTPrimed) e;
								 
								// Go through all of the triggering events (extremely likely to be events of the 
								// same kind of TippingPoint) and find the nearest TNTPrimed entity to those 
								// event's locations.
								{
									HEEvent bestMatchFound = null;
									double bestMatchDistanceSq = Double.POSITIVE_INFINITY;
									
									for(final HEEvent triggeringEvent : TNTTracker.this.triggerEventsToAssociate) { 
										double distance = triggeringEvent.getEventLocation().toVector().distanceSquared(tnt.getLocation().toVector());
										
										if(distance < bestMatchDistanceSq) {
											bestMatchDistanceSq = distance;
											bestMatchFound = triggeringEvent;
										}
									}
								
									seenTNTPrimedEntities.put(tnt.getEntityId(), bestMatchFound);
								}

								if(worldConf.getActiveBounds().isWithinBounds(e.getLocation())) {
									MCNative.multiplyTNTFuseDuration(((TNTPrimed) e), worldConf.getNextTNTFuseMultiplier());
								}
							}
						}		
					}
				}
				
				TNTTracker.this.triggerEventsToAssociate.clear();
			}
		});
		
		this.scheduledTaskID = taskId;
	}
}
