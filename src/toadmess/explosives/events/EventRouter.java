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
import toadmess.explosives.MCNative;
import toadmess.explosives.MultiWorldConfStore;

/**
 * Intended to take some points of interest from the different bukkit listeners and match them up with
 * the relevant configuration. If all is good then forward them on to all the things that want to 
 * take action.
 * 
 * TODO: At the moment all of the actions are in this class. Split them out into some sensible classes.
 */
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
		this.log.fine("primedByPlayer");
	}
	
	public void primedByExplosion() {
		this.log.fine("primedByExplosion");
	}
	
	public void primedByRedstone(final BlockPhysicsEvent event) {
		this.log.fine("primedByRedstone");
	}
	
	/**
	 * Called for TNT blocks that have just been burnt. 
	 */
	public void primedByFire(final BlockBurnEvent event, final Block burntTNT) {
		this.log.fine("primedByFire");
	}
	
	
	public void canChangeExplosionRadius(final ExplosionPrimeEvent event, final Class<? extends Entity> entityType) {
		this.log.fine("canChangeExplosionRadius");
		
		final EntityConf worldConf = this.confStore.getActiveConf(entityType, event.getEntity().getLocation());
		
		if(null == worldConf || !worldConf.hasRadiusConfig()) {
			return;
		}
		
		event.setRadius(worldConf.getNextRadiusMultiplier() * event.getRadius());
	}
	
	public void canChangeExplosionFireFlag(final ExplosionPrimeEvent event, final Class<? extends Entity> entityType) {
		this.log.fine("canChangeFireFlag");
		
		final EntityConf worldConf = this.confStore.getActiveConf(entityType, event.getEntity().getLocation());
		
		if(null == worldConf || !worldConf.hasFireConfig()) {
			return;
		}
		
		event.setFire(worldConf.getFire());
	}
	
	public void canChangeExplosionYield(final EntityExplodeEvent event, final Class<? extends Entity> entityType) {
		this.log.fine("canChangeExplosionYield");
		
		final EntityConf worldConf = this.confStore.getActiveConf(entityType, event.getEntity().getLocation());
		
		if(null == worldConf || !worldConf.hasYieldConfig()) {
			return;
		}
		
		event.setYield(worldConf.getYield());
	}
	
	public void canChangePlayerDamage(final EntityDamageEvent event, final Entity damager, final Class<? extends Entity> entityType) {
		this.log.fine("canChangePlayerDamage");
		
		final EntityConf worldConf = this.confStore.getActiveConf(entityType, damager.getLocation());
		
		if(null == worldConf || !worldConf.hasPlayerDamageConfig()) {
			return;
		}
		
		event.setDamage((int) (event.getDamage() * worldConf.getNextPlayerDamageMultiplier()));
	}

	public void canChangeCreatureDamage(final EntityDamageEvent event, final Entity damager, final Class<? extends Entity> entityType) {
		this.log.fine("canChangeCreatureDamage");
		
		final EntityConf worldConf = this.confStore.getActiveConf(entityType, damager.getLocation());
		
		if(null == worldConf || !worldConf.hasCreatureDamageConfig()) {
			return;
		}
		
		event.setDamage((int) (event.getDamage() * worldConf.getNextCreatureDamageMultiplier()));
	}
	
	public void canChangeItemDamage(final EntityDamageEvent event, final Entity damager, final Class<? extends Entity> entityType) {
		this.log.fine("canChangeItemDamage");
		
		final EntityConf worldConf = this.confStore.getActiveConf(entityType, damager.getLocation());
		
		if(null == worldConf || !worldConf.hasItemDamageConfig()) {
			return;
		}
		
		event.setDamage((int) (event.getDamage() * worldConf.getNextItemDamageMultiplier()));
	}

	public void canPreventTerrainDamage(final EntityExplodeEvent event, final Class<? extends Entity> entityType) {
		this.log.fine("canPreventTerrainDamage");
		
		final Location epicentre = event.getEntity().getLocation();
		final EntityConf worldConf = this.confStore.getActiveConf(entityType, epicentre);
		
		if(null == worldConf || !worldConf.hasPreventTerrainDamageConfig()) {
			return;
		}
		
		if(worldConf.getPreventTerrainDamage()) {			
			event.setCancelled(true);
			
			MCNative.playSoundExplosion(epicentre);
		}
	}
}
