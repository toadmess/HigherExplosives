package toadmess.explosives.config.entity;

import static toadmess.explosives.config.ConfProps.CONF_BOUNDS;
import static toadmess.explosives.config.ConfProps.CONF_ENTITY_FIRE;
import static toadmess.explosives.config.ConfProps.CONF_ENTITY_TNT_TRIGGER_EXPLOSION;
import static toadmess.explosives.config.ConfProps.CONF_ENTITY_TNT_TRIGGER_FIRE;
import static toadmess.explosives.config.ConfProps.CONF_ENTITY_TNT_TRIGGER_HAND;
import static toadmess.explosives.config.ConfProps.CONF_ENTITY_TNT_TRIGGER_REDSTONE;
import static toadmess.explosives.config.ConfProps.CONF_ENTITY_YIELD;
import static toadmess.explosives.config.ConfProps.CONF_ENTITY_CREEPER_CHARGED;

import java.util.List;
import java.util.Random;

import toadmess.explosives.Bounds;
import toadmess.explosives.config.ConfProps;

/**
 * Represents a single entity's configuration within a single world.
 * 
 * @author John Revill
 */
public class EntityConf {
	/**
	 * Sub configuration all have a reference to their parent containing 
	 * configuration, which allows inheritance of their properties.
	 * This will be null for root configurations.
	 */
	private final EntityConf parent;
	
	/** Used when conjuring up a new multiplier based on some random chance */
	private final Random rng;

	/**
	 * Contains all of the configuration properties. An array indexed by the ConfProps's ordinal value. 
	 * If the property has not been specified for this configuration then the value will be 
	 * null at that index.
	 */
	private Object[] properties;
	
	/** 
	 * True if the MiningTNT plugin was detected in the list of plugins. False otherwise. 
	 * Set by the Plugin main class. 
	 */
	public static boolean hasConflictWithMiningTNT = false;
	
	public EntityConf(final EntityConf parent, final Random rng) {		
		this.parent = parent;
		this.rng = rng;
	}

	protected void setProperties(final Object[] allTheReadConfigProperties) {
		this.properties = allTheReadConfigProperties;
	}
	
	@SuppressWarnings("unchecked")
	private float getNextMultiplier(final ConfProps multiplierProperty) {
		return getNextMultiplier((List<List<Float>>) this.getInheritedProp(multiplierProperty));
	}
	
	private float getNextMultiplier(final List<List<Float>> sortedMultipliers) {
		if(sortedMultipliers == null || sortedMultipliers.size() == 0) {
			// Default to 1.0 to be safe
			return 1.0F;
		}
		
		final float randomNum = this.rng.nextFloat();
		float cumulativeChance = 0.0F;

		for (final List<Float> multiplierPair : sortedMultipliers) {
			cumulativeChance += multiplierPair.get(0);

			if (cumulativeChance > randomNum) {
				return multiplierPair.get(1);
			}
		}

		// Well, the chance values didn't add up to 1.0. Just return the most likely multiplier (the first in the list)
		return sortedMultipliers.get(0).get(1);
	}

	public float getYield() {
		final Float yield = (Float) this.getInheritedProp(CONF_ENTITY_YIELD);
		
		if(EntityConf.hasConflictWithMiningTNT && yield == null) {
			// There's no yield specified in the config.yml, but because the 
			// MiningTNT plugin is in use on this server, we set the yield to 1.0.
			// This is MiningTNT's default.
			return 1.0F;
		}
		
		return yield;
	}
	
	
	// HACK: Breaks encapsulation and allow modification of the array's contents, but 
	// why would any caller modify this array's contents? 
	public Float[] getSpecificYieldConfig() { return (Float[]) this.getInheritedProp(ConfProps.CONF_ENTITY_YIELD_SPECIFIC); }
	
	public Bounds getActiveBounds() { return (Bounds) this.getInheritedProp(CONF_BOUNDS); }
	public boolean getFire() { return (Boolean) this.getInheritedProp(CONF_ENTITY_FIRE); }
	public boolean getPreventTerrainDamage() { return (Boolean) this.getInheritedProp(ConfProps.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE); }
	public boolean getTNTPrimePrevented() { return (Boolean) this.getInheritedProp(ConfProps.CONF_ENTITY_TNT_TRIGGER_PREVENTED); }
	
	public float getNextRadiusMultiplier() { return getNextMultiplier(ConfProps.CONF_ENTITY_RADIUSMULT); }
	public float getNextPlayerDamageMultiplier() { return getNextMultiplier(ConfProps.CONF_ENTITY_PLAYER_DAMAGEMULT); }
	public float getNextCreatureDamageMultiplier() { return getNextMultiplier(ConfProps.CONF_ENTITY_CREATURE_DAMAGEMULT); }
	public float getNextItemDamageMultiplier() { return getNextMultiplier(ConfProps.CONF_ENTITY_ITEM_DAMAGEMULT); }
	public float getNextTNTFuseMultiplier() { return getNextMultiplier(ConfProps.CONF_ENTITY_TNT_FUSEMULT); }

	public EntityConf getTNTPrimeByHandConfig() { return (EntityConf) this.getInheritedProp(CONF_ENTITY_TNT_TRIGGER_HAND); }
	public EntityConf getTNTPrimeByFireConfig() { return (EntityConf) this.getInheritedProp(CONF_ENTITY_TNT_TRIGGER_FIRE); }
	public EntityConf getTNTPrimeByRedstoneConfig() { return (EntityConf) this.getInheritedProp(CONF_ENTITY_TNT_TRIGGER_REDSTONE); }
	public EntityConf getTNTPrimeByExplosionConfig() { return (EntityConf) this.getInheritedProp(CONF_ENTITY_TNT_TRIGGER_EXPLOSION); }
	public EntityConf getCreeperChargedConfig() { return (EntityConf) this.getInheritedProp(CONF_ENTITY_CREEPER_CHARGED); }
	
	
	public boolean hasFireConfig() { return this.hasInheritedProp(CONF_ENTITY_FIRE); }
	public boolean hasRadiusConfig() { return this.hasInheritedProp(ConfProps.CONF_ENTITY_RADIUSMULT); }
	public boolean hasCreatureDamageConfig() { return this.hasInheritedProp(ConfProps.CONF_ENTITY_CREATURE_DAMAGEMULT); }
	public boolean hasPlayerDamageConfig() { return this.hasInheritedProp(ConfProps.CONF_ENTITY_PLAYER_DAMAGEMULT); }
	public boolean hasItemDamageConfig() { return this.hasInheritedProp(ConfProps.CONF_ENTITY_ITEM_DAMAGEMULT); }
	public boolean hasTNTFuseConfig() { return this.hasInheritedProp(ConfProps.CONF_ENTITY_TNT_FUSEMULT); }
	public boolean hasYieldConfig() {
		return this.hasInheritedProp(CONF_ENTITY_YIELD) || EntityConf.hasConflictWithMiningTNT;
	}
	public boolean hasSpecificYieldConfig() { return this.hasInheritedProp(ConfProps.CONF_ENTITY_YIELD_SPECIFIC); }
	public boolean hasPreventTerrainDamageConfig() { return this.hasInheritedProp(ConfProps.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE); }
	public boolean hasTNTPrimePrevented() { return this.hasInheritedProp(ConfProps.CONF_ENTITY_TNT_TRIGGER_PREVENTED); }
	
	public boolean hasTNTPrimeByHandConfig() { return this.hasInheritedProp(CONF_ENTITY_TNT_TRIGGER_HAND); }
	public boolean hasTNTPrimeByFireConfig() { return this.hasInheritedProp(CONF_ENTITY_TNT_TRIGGER_FIRE); }
	public boolean hasTNTPrimeByRedstoneConfig() { return this.hasInheritedProp(CONF_ENTITY_TNT_TRIGGER_REDSTONE); }
	public boolean hasTNTPrimeByExplosionConfig() { return this.hasInheritedProp(CONF_ENTITY_TNT_TRIGGER_EXPLOSION); }
	public boolean hasCreeperChargedConfig() { return this.hasInheritedProp(CONF_ENTITY_CREEPER_CHARGED); }
	
	protected boolean hasOwnProp(final ConfProps property) {
		return (hasInheritedProp(property) && (getOwnProp(property) != null));
	}
	
	protected Object getOwnProp(final ConfProps property) {
		return this.properties[property.ordinal()];
	}
	
	protected boolean hasInheritedProp(final ConfProps property) {
		return null != getInheritedProp(property);
	}
	
	protected Object getInheritedProp(final ConfProps property) {
		final Object ownProp = this.properties[property.ordinal()];
		
		if(null != ownProp) {
			return ownProp;
		}
		
		if(isSubConfig()) {
			return this.parent.getInheritedProp(property);
		}
		
		return null;
	}
	
	public boolean isEmptyConfig() {
		for(final ConfProps property : ConfProps.values()) {
			switch(property) {
			case CONF_BOUNDS:
				// Skip the bounds configuration. 
				// It's not so interesting if the config has no properties. 
				// And it defaults to having empty bounds as well.
				continue;
			case CONF_ENTITY_YIELD:
				// Yield has a special workaround for MiningTNT
				if(this.hasYieldConfig()) {
					return false;
				}
				break;
			default:
				if(this.hasInheritedProp(property)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isSubConfig() {
		return this.parent != null;
	}
	
	public String toString() {
		return (new EntityConfPrinter(this)).stringify();
	}
}