package toadmess.explosives.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;

public class HEFuseEvent extends HEEvent {
	public final TNTPrimed primedTnt;
	
	public HEFuseEvent(final TNTPrimed primedTnt, final HEEvent triggeringEvent) {
		super(TippingPoint.CAN_CHANGE_TNT_FUSE, triggeringEvent.event, triggeringEvent.confStore);
		
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
		return "HEFuseEvent(primedTnt="+primedTnt+", triggerEvent="+event+")";
	}
}
