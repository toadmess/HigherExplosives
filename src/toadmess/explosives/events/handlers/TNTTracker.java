package toadmess.explosives.events.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
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
	 * Contains IDs for TNTPrimed entities that have recently had their fuse lengths modified.
	 */
	private final Set<Integer> handledTNTPrimedEntities = new HashSet<Integer>();
	
	/** 
	 * The Task ID of the last scheduled task for finding new TNTPrimed entities, within specific worlds. Maps world name to Task ID.
	 */
	private Map<String,Integer> lastTntPrimedSearchTaskIDs = new HashMap<String,Integer>();

	private final Plugin plugin;
	
	public TNTTracker(final Plugin p) {
		this.plugin = p;
	}
	
	@Override
	public TippingPoint[] getTippingPointsHandled() {
		return new TippingPoint[] {
			TippingPoint.TNT_PRIMED_BY_EXPLOSION,
			TippingPoint.TNT_PRIMED_BY_FIRE,
			TippingPoint.TNT_PRIMED_BY_PLAYER,
			TippingPoint.TNT_PRIMED_BY_REDSTONE,
			TippingPoint.AN_EXPLOSION,
		};
	}
	
	@Override
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!worldConf.hasTNTFuseConfig()) {
			return;
		}
		
		switch(ev.type) {
		case TNT_PRIMED_BY_EXPLOSION:
		case TNT_PRIMED_BY_FIRE:
		case TNT_PRIMED_BY_PLAYER:
		case TNT_PRIMED_BY_REDSTONE:
			dealWithAnyTNTJustPrimed(ev.getEventLocation(), worldConf);
			break;
			
		case AN_EXPLOSION:
			final Entity e = ((EntityExplodeEvent) ev.event).getEntity();
			if(e instanceof TNTPrimed) {
				// This was a TNTPrimed entity that just exploded.
				// Clean up its entity ID from our collection of tweaked TNTPrimed entity IDs.
				handledTNTPrimedEntities.remove(e.getEntityId());				
			}
			
			// Some other TNT block may have been caught in this blast, so search for new primed TNTs
			dealWithAnyTNTJustPrimed(ev.getEventLocation(), worldConf);
			break;
		}
	}
	
	// Called when any priming is detected or suspected.
	// It will schedule a search, in the next few ticks, for any TNTPrimed entities in 
	// that world which have not yet been re-fused. 
	private void dealWithAnyTNTJustPrimed(final Location epicentre, final EntityConf worldConf) {
		final String worldName = epicentre.getWorld().getName();
		final BukkitScheduler bs = this.plugin.getServer().getScheduler();
		final Integer lastTaskIDForSearchInThisWorld = this.lastTntPrimedSearchTaskIDs.get(worldName);
		
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
		
		this.lastTntPrimedSearchTaskIDs.put(worldName, taskId);
	}
}
