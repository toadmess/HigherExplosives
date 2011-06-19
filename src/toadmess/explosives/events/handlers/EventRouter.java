package toadmess.explosives.events.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;

import toadmess.explosives.config.MultiWorldConfStore;
import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.Handler;
import toadmess.explosives.events.TippingPoint;

/**
 * Intended to take some events of interest from the different bukkit listeners 
 * and match them up with the relevant configuration. 
 * 
 * If all is good then delegate the event on to all the Handlers that are registered with that type of TippingPoint.
 */
public class EventRouter implements Handler {
	private final Logger log;
	
	private final ArrayList<ArrayList<Handler>> handlers;
	
	public EventRouter(final Logger l, final Plugin p, final MultiWorldConfStore confStore) {
		this.log = l;
		
		// Initialise the handlers list
		this.handlers = new ArrayList<ArrayList<Handler>>();
		for(final TippingPoint event : TippingPoint.values()) {
			this.handlers.add(new ArrayList<Handler>());
		}
		this.handlers.trimToSize();
		
		// Go through every single handler and every single configuration and only 
		// add those handlers that we could ever possibly need.  
		for(final Handler h : this.createAllHandlers(p)) {
			for(final EntityConf conf : confStore.allConfigsAndSubConfigs()) {
				if(h.isNeededBy(conf)) {
					this.addHandler(h);
					break;
				}
			}
		}
	}
	
	private Handler[] createAllHandlers(final Plugin p) {
		return new Handler[] {
			new HandleFire(),
			new HandleRadius(),
			new HandlePreventTerrainDamage(),
			new HandleYield(),
			new HandleDamagePlayer(),
			new HandleDamageCreature(),
			new HandleDamageItem(),
			new HandleTNTFuse(),
			new HandleTNTPreventPrime(),
			new TNTTracker(p, this)
		};
	}
		
	public void addHandler(final Handler h) {
		for(final TippingPoint forThisTippingPoint : h.getTippingPointsHandled()) {
			final ArrayList<Handler> handlerList = this.handlers.get(forThisTippingPoint.ordinal()); 
			
			handlerList.add(h);
			handlerList.trimToSize();
		}
	}
	
	@Override
	public void handle(final HEEvent event) {
		if(!event.hasApplicableConfig()) {
			return;
		}
		
		for(final Handler h : handlers.get(event.type.ordinal())) {
			h.handle(event);
		}
	}

	@Override
	public TippingPoint[] getTippingPointsHandled() {
		// We handle them all.
		return TippingPoint.values();
	}
	
	@Override
	public Type[] getBukkitEventsRequired() {
		return new Type[] {}; 
	}
	
	@Override
	public boolean isNeededBy(final EntityConf thisConfig) {
		return true; // This handler will always be needed
	}
	
	/**
	 * Finds all the event types required by any and all configs in all worlds.
	 * @return A collection of the event types that we should register and listen to
	 */
	public Set<Type> getNeededBukkitEvents() {
		final HashSet<Event.Type> neededEvents = new HashSet<Event.Type>();

		for(final List<Handler> handlersForTippingPoint : this.handlers) {
			for(final Handler handler : handlersForTippingPoint) {
				neededEvents.addAll(Arrays.asList(handler.getBukkitEventsRequired()));
			}
		}
		
		return neededEvents;
	}
}
