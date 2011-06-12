package toadmess.explosives.events.handlers;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

import toadmess.explosives.config.entity.EntityConf;
import toadmess.explosives.events.HEEvent;
import toadmess.explosives.events.Handler;

public abstract class HandleDamageEntity implements Handler {

	@Override
	public void handle(final HEEvent ev) {
		final EntityConf worldConf = ev.getApplicableConfig();
		
		if(!hasConfig(worldConf)) {
			return;
		}
		
		final EntityDamageByEntityEvent downcastEvent = ((EntityDamageByEntityEvent) ev.event);
		
		downcastEvent.setDamage((int) (downcastEvent.getDamage() * getDamageMultiplier(worldConf)));
	}	
	
	protected abstract boolean hasConfig(final EntityConf conf);
	protected abstract float getDamageMultiplier(final EntityConf conf);
}
