package toadmess.explosives.events;

import org.bukkit.event.Event.Type;

import toadmess.explosives.config.entity.EntityConf;

public interface Handler {
	void handle(final HEEvent event);
	
	TippingPoint[] getTippingPointsHandled();
	
	Type[] getBukkitEventsRequired();
	
	boolean isNeededBy(final EntityConf thisConfig);
}
