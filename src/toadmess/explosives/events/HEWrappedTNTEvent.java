package toadmess.explosives.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;

public class HEWrappedTNTEvent extends HEEvent {
	public final TNTPrimed primedTnt;
	
	public HEWrappedTNTEvent(final TippingPoint tp, final TNTPrimed primedTnt, final HEEvent triggeringEvent) {
		super(tp, triggeringEvent.event, triggeringEvent.confStore);
		
		this.primedTnt = primedTnt;
	}
	
	@Override
	public Class<? extends Entity> getConfigEntityClass() {
		return TNTPrimed.class;
	}
	
	@Override
	public Location getEventLocation() {
		return this.primedTnt.getLocation();
	}
			
	@Override
	protected Entity getRelevantEntity() {
		return this.primedTnt;
	}
	
	@Override
	public String toString() {
		return "HEWrappedTNTEvent(tp="+this.type+", primedTnt="+primedTnt+", triggerEvent="+event+")";
	}
}
