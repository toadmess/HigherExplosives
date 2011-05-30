package toadmess.explosives.events.handlers;

import java.util.ArrayList;
import java.util.logging.Logger;

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
	
	public EventRouter(final Logger l) {
		this.log = l;
		
		this.handlers = new ArrayList<ArrayList<Handler>>();
		for(final TippingPoint event : TippingPoint.values()) {
			this.handlers.add(new ArrayList<Handler>());
		}
		this.handlers.trimToSize();
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
}
