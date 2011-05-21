package toadmess.explosives.events;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import toadmess.explosives.EntityConf;
import toadmess.explosives.MultiWorldConfStore;

public class EventRouter {
	private final Logger log;
	
	private MultiWorldConfStore confStore;
	
	public EventRouter(final Logger l) {
		this.log = l;
	}
	
	public void setConfStore(final MultiWorldConfStore confStore) {
		this.confStore = confStore;
	}
	
	public void primedByPlayer(final BlockDamageEvent event) {
		this.log.info("primedByPlayer");
	}
	
	public void primedByExplosion() {
		this.log.info("primedByExplosion");
	}
	
	public void primedByRedstone(final BlockPhysicsEvent event) {
		this.log.info("primedByRedstone");
	}
	
	/**
	 * Called for TNT blocks that have just been burnt. 
	 */
	public void primedByFire(final BlockBurnEvent event, final Block burntTNT) {
		this.log.info("primedByFire");
	}
	
	
	public void canChangeExplosionRadius(final ExplosionPrimeEvent event, final Class<? extends Entity> entityType) {
		this.log.info("canChangeExplosionRadius");
		
		final EntityConf worldConf = this.confStore.getActiveConf(entityType, event.getEntity().getLocation());
		
		if(null == worldConf || !worldConf.hasRadiusConfig()) {
			return;
		}
		
		event.setRadius(worldConf.getNextRadiusMultiplier() * event.getRadius());
	}
	
	public void canChangeExplosionFireFlag(final ExplosionPrimeEvent event, final Class<? extends Entity> entityType) {
		this.log.info("canChangeFireFlag");
		
		final EntityConf worldConf = this.confStore.getActiveConf(entityType, event.getEntity().getLocation());
		
		if(null == worldConf || !worldConf.hasFireConfig()) {
			return;
		}
		
		event.setFire(worldConf.getFire());
	}
	
	public void canChangeExplosionDamage() {
		this.log.info("canChangeEexplosionDamage");
	}
	
	public void canChangeExplosionYield(final EntityExplodeEvent event) {
		this.log.info("canChangeExplosionYield");
	}
	
	public void canChangePlayerDamage(final EntityDamageEvent event) {
		this.log.info("canChangePlayerDamage");
	}

	public void canChangeCreatureDamage(final EntityDamageEvent event) {
		this.log.info("canChangeCreatureDamage");
	}
	
	public void canChangeItemDamage(final EntityDamageEvent event) {
		this.log.info("canChangeItemDamage");
	}

	public void canPreventTerrainDamage(final EntityExplodeEvent event) {
		this.log.info("canPreventTerrainDamage");
	}
}
