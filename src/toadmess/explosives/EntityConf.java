package toadmess.explosives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.util.config.Configuration;

/**
 * Represents a single entity's configuration within a single world.
 * 
 * @author John Revill
 */
public class EntityConf implements ConfConstants {
	private final Logger log; 
	
	/** 
	 * A sorted list of pairs of floats representing the radius multipliers to apply when modifying the explosion radius. 
	 * The pair's head is the chance (0.0 to 1.0) and tail is the multiplier (0.0 and above). 
	 * The list is sorted according to the pair's chance, pairs with higher chances coming first.
	 * If null, the configuration provided no radius multipliers. 
	 */
	private final List<List<Float>> radiusMultipliers;
	/**
	 * A sorted list of pairs of floats representing the damage multipliers to apply when modifying explosion damage to the player.
	 * If null, the configuration provided no player damage multipliers.
	 */
	private final List<List<Float>> playerDamageMultipliers;
	/**
	 * A sorted list of pairs of floats representing the damage multipliers to apply when modifying explosion damage to creatures.
	 * If null, the configuration provided no creature damage multipliers.
	 */
	private final List<List<Float>> creatureDamageMultipliers;
	/** Same kind of list of multipliers as the radiusMultiplier, applies to non living item entities. */
	private final List<List<Float>> itemDamageMultipliers;
	/** Same kind of list of multipliers as the radiusMultiplier, applies to TNT fuse durations */
	private final List<List<Float>> fuseMultipliers;
	
	/** Used when conjuring up a new multiplier based on some random chance */
	private final Random rng;

	private final Bounds allowedBounds;
	
	/** Whether the explosion causes fire */
	private final Boolean fire;
	
	/** Whether the explosion causes terrain damage */
	private final Boolean preventTerrainDamage;

	/** The percentage (from 0.0 to 1.0) of damaged blocks in the explosion that drop items */
	private final Float yield;

	/** 
	 * A sparse array whose index is a specific block type ID and whose value is the yield 
	 * percentage (from 0.0 to 1.0) to use for that block type. 
	 */
	private final Float[] specificYields;
	
	/** 
	 * True if the MiningTNT plugin was detected in the list of plugins. False otherwise. 
	 * Set by the Plugin main class. 
	 */
	protected static boolean hasConflictWithMiningTNT = false;
	
	public EntityConf(final Configuration conf, final String confPathPrefix, final Logger log) {
		this(conf, confPathPrefix, log, new Random());
	}
	
	public EntityConf(final Configuration conf, final String confPathPrefix, final Logger log, final Random rng) {
		this.log = log;
		
		this.rng = rng;
		
		this.allowedBounds = new Bounds(conf, confPathPrefix);
		
		this.radiusMultipliers = readMultipliers(conf, confPathPrefix + "." + CONF_ENTITY_RADIUSMULT);
		this.playerDamageMultipliers = readMultipliers(conf, confPathPrefix + "." + CONF_ENTITY_PLAYER_DAMAGEMULT);
		this.creatureDamageMultipliers = readMultipliers(conf, confPathPrefix + "." + CONF_ENTITY_CREATURE_DAMAGEMULT);
		this.itemDamageMultipliers = readMultipliers(conf, confPathPrefix + "." + CONF_ENTITY_ITEM_DAMAGEMULT);
		this.fuseMultipliers = readMultipliers(conf, confPathPrefix + "." + CONF_ENTITY_TNT_FUSEMULT);

		this.fire = (Boolean) conf.getProperty(confPathPrefix + "." + CONF_ENTITY_FIRE);
		this.preventTerrainDamage = (Boolean) conf.getProperty(confPathPrefix + "." + CONF_ENTITY_PREVENT_TERRAIN_DAMAGE);
		
		this.yield = readOptionalFloat(conf, confPathPrefix + "." + CONF_ENTITY_YIELD);
		this.specificYields = readSpecificYields(conf, confPathPrefix + "." + CONF_ENTITY_YIELD_SPECIFIC);
		
		if(null != conf.getProperty(confPathPrefix + ".trialTNTFuseMultiplier")) {
			this.log.warning("HigherExplosives: The \"trialTNTFuseMultiplier\" configuration is no longer used. Please rename it to \"" + CONF_ENTITY_TNT_FUSEMULT + "\"");
		}
		
		if(null != conf.getProperty("everyExplosion") || 
		   null != conf.getProperty(confPathPrefix + ".everyExplosion")) {
			this.log.warning("HigherExplosives: The \"everyExplosion\" configuration is no longer used. Instead, specify the explosion \"yield\" on the individual entities.");
		}
	}

	private Float readOptionalFloat(final Configuration conf, final String path) {
		final Object o = conf.getProperty(path);
		
		if(o == null || !(o instanceof Number)) {
			return null;
		}
		
		return Double.valueOf(conf.getDouble(path, 0.0D)).floatValue();
	}
		
	private Float[] readSpecificYields(final Configuration conf, final String pathToYieldSpecific) {
		final Object specYieldsProp = conf.getProperty(pathToYieldSpecific);
		if (specYieldsProp instanceof HashMap<?,?>) {
			final HashMap<?,?> specYields = (HashMap<?,?>) specYieldsProp;
			
			final Float[] yieldsSparseArr = new Float[MCNative.getHighestBlockId()+1];
			
			for(final Object blockID : specYields.keySet()) {
				yieldsSparseArr[(Integer) blockID] = ((Number) specYields.get(blockID)).floatValue();
			}
			
			return yieldsSparseArr;
		}
		
		return null;
	}
	
	/**
	 * Extracts a list of chance/multiplier pairs from some place in the configuration. 
	 * It detects whether the configuration contains just a single multiplier, or if there are several chance/multipliers listed.
	 * 
	 * @param conf
	 * @param pathToMultiplier The configuration path prefix up to where the chance/value(s) are listed.
	 * 
	 * @return null if no multiplier configuration was found at the given path. 
	 * OTherwise, a list of pairs of floats. Each pair is a chance (head) and multiplier (tail). 
	 */
	private List<List<Float>> readMultipliers(final Configuration conf, final String pathToMultiplier) {
		final List<List<Float>> multipliers = new ArrayList<List<Float>>(); // A list of pairs of floats. Each pair is a chance (head) and multiplier (tail).

		// First extract the multiplier chance/value pairs from the configuration and add them all to the multipliers list. 
		final Object multiplierProp = conf.getProperty(pathToMultiplier);
		if (multiplierProp instanceof Number) {
			addMultiplier(multipliers, 1.0F, Math.max(0.0F, (float)conf.getDouble(pathToMultiplier, 1.0D)));
		} else if (multiplierProp instanceof List<?>) {
			for(final Object multiplierListItemProp : (List<?>) multiplierProp) {
				if (multiplierListItemProp instanceof HashMap<?,?>) {
					final HashMap<?,?> chanceAndValueProp = (HashMap<?,?>) multiplierListItemProp;

					final Object chanceProp = chanceAndValueProp.get(CONF_MULTIPLIER_CHANCE);
					final Object valueProp = chanceAndValueProp.get(CONF_MULTIPLIER_VALUE);

					if ((chanceProp instanceof Double) && (valueProp instanceof Double)) {
						addMultiplier(multipliers, (float)Math.min(1.0D, Math.max(0.0D, ((Double)chanceProp).doubleValue())), (float)Math.max(0.0D, ((Double)valueProp).doubleValue()));
					} else {
						this.log.warning("HigherExplosives: Config problem. Ignoring list item under " + pathToMultiplier + " as either the " + CONF_MULTIPLIER_CHANCE + " of (" + chanceProp + ") or the " + CONF_MULTIPLIER_VALUE + " of (" + valueProp + ") doesn't look like a number. Was expecting a double.");
					}
				} else {
					this.log.warning("HigherExplosives: Config problem. Ignoring strange list item under " + pathToMultiplier + ". Was expecting " + CONF_MULTIPLIER_CHANCE + " and " + CONF_MULTIPLIER_VALUE + " keys");
				}
			}

			// Make a check that the cumulative chance of all the chance/value pairs totals up to 1.0
			float cumulativeChance = 0.0F;
			for(final List<Float> chanceValuePair : multipliers) {
				cumulativeChance += chanceValuePair.get(0);
			}

			if (Math.abs(cumulativeChance - 1.0F) > 0.0001D) {
				this.log.warning("HigherExplosives: Config problem. Total probability for " + pathToMultiplier + " doesn't quite " + "add up to 1.0. It's " + cumulativeChance);
			}

		}

		if (multipliers.size() == 0) {
			return null;
		}

		return multipliers;
	}

	/**
	 * Adds the chance and value as a new pair to the existing list of multpliers.
	 * 
	 * @param multipliers The existing list of chance/value multiplier pairs
	 * 
	 * @param chance The chance of this value being chosen.
	 * @param value The multiplier value itself.
	 */
	private void addMultiplier(final List<List<Float>> multipliers, final float chance, final float value) {
		final List<Float> chanceValuePair = new ArrayList<Float>(2);

		chanceValuePair.add(chance);
		chanceValuePair.add(value);

		multipliers.add(chanceValuePair);

		// Comparator that places higher chances before lower chances
		final Comparator<List<Float>> chanceComparator = new Comparator<List<Float>>() {
			public int compare(List<Float> a, List<Float> b) {
				if (a.get(0) == b.get(0)) {
					return 0;
				}

				if (a.get(0) < b.get(0)) return 1;

				return -1;
			}
		};

		Collections.sort(multipliers, chanceComparator);
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

	public String toString() {
		String str = "Conf(";
		str = str + "\n  radiusMultiplier={\n" + multipliersToString(this.radiusMultipliers) + "  },";
		str = str + "\n  playerDamageMultiplier={\n" + multipliersToString(this.playerDamageMultipliers) + "  },";
		str = str + "\n  creatureDamageMultiplier={\n" + multipliersToString(this.creatureDamageMultipliers) + "  },";
		str = str + "\n  itemDamageMultiplier={\n" + multipliersToString(this.itemDamageMultipliers) + "  },";
		str = str + "\n  fuseMultiplier={\n" + multipliersToString(this.fuseMultipliers) + "  },";
		str = str + "\n  fire=" + (hasFireConfig() ? getFire() : "no fire configured. will leave unaffected") + ",";
		str = str + "\n  yield=" + (hasYieldConfig() ? getYield() : "no yield configured. will leave unaffected") + ",";
		str = str + "\n  yieldSpecific=" + specificYieldsToString(this.specificYields) + ",";
		str = str + "\n  preventTerrainDamage=" + (hasPreventTerrainDamageConfig() ? getPreventTerrainDamage() : "not configured, terrain damage is as normal") + ",";
		str = str + "\n  activeBounds=" + this.allowedBounds;
		str = str + "\n)";

		return str;
	}

	private String multipliersToString(final List<List<Float>> paramList) {
		if(paramList == null) {
			return "    no multiplier configured. will leave unaffected\n";
		}

		String str = "";
		
		for (final List<?> localList : paramList) {
			str = str + "    (chance:" + localList.get(0) + ", value:" + localList.get(1) + ")\n";
		}
		return str;
	}

	private String specificYieldsToString(final Float[] specificYields) {
		if(specificYields == null) {
			return "no specific block yields configured";
		}

		String str = "{\n";
		
		for (int i = 0; i < specificYields.length; i++) {
			if(specificYields[i] != null) {
				str += "    (Block ID " + i + " has yield " + specificYields[i] + ")\n";
			}
		}
		
		str += "  }";
		
		return str;
	}
	
	public Bounds getActiveBounds() {
		return this.allowedBounds;
	}

	public boolean getFire() {
		return hasFireConfig() ? this.fire : false ;
	}
	
	public float getYield() {
		if(!hasYieldConfig()) {
			return 0.3F; // Report the yield as the minecraft default.
		}
			
		if(EntityConf.hasConflictWithMiningTNT && this.yield == null) {
			// There's no yield specified in the config.yml, but because the 
			// MiningTNT plugin is in use on this server, we set the yield to 1.0.
			// This is MiningTNT's default.
			return 1.0F;
		}
		
		return this.yield.floatValue();
	}
	
	// Breaks encapsulation, but why would any caller modify this array's contents? 
	public Float[] getSpecificYieldConfig() {
		return hasSpecificYieldConfig() ? this.specificYields : new Float[]{};
	}
	
	public boolean getPreventTerrainDamage() {
		return hasPreventTerrainDamageConfig() ? this.preventTerrainDamage : false ;
	}
	
	public float getNextTNTFuseMultiplier() {
		return getNextMultiplier(this.fuseMultipliers);
	}

	public float getNextRadiusMultiplier() {
		return getNextMultiplier(this.radiusMultipliers);
	}

	public float getNextPlayerDamageMultiplier() {
		return getNextMultiplier(this.playerDamageMultipliers);
	}

	public float getNextCreatureDamageMultiplier() {
		return getNextMultiplier(this.creatureDamageMultipliers);
	}
	
	public float getNextItemDamageMultiplier() {
		return getNextMultiplier(this.itemDamageMultipliers);
	}
	
	public boolean hasFireConfig() {
		return this.fire != null;
	}
	
	public boolean hasRadiusConfig() {
		return this.radiusMultipliers != null; 
	}
	
	public boolean hasCreatureDamageConfig() {
		return this.creatureDamageMultipliers != null; 
	}
	
	public boolean hasPlayerDamageConfig() {
		return this.playerDamageMultipliers != null;
	}
	
	public boolean hasItemDamageConfig() {
		return this.itemDamageMultipliers != null;
	}
	
	public boolean hasYieldConfig() {
		return (this.yield != null) || EntityConf.hasConflictWithMiningTNT;
	}
	
	public boolean hasSpecificYieldConfig() {
		return this.specificYields != null;
	}
	
	public boolean hasPreventTerrainDamageConfig() {
		return this.preventTerrainDamage != null;
	}
	
	public boolean hasTNTFuseConfig() {
		return this.fuseMultipliers != null;
	}
	
	public boolean isEmptyConfig() {
		return !(hasFireConfig() || hasYieldConfig() || hasSpecificYieldConfig() ||
				 hasPreventTerrainDamageConfig() || hasRadiusConfig() || hasTNTFuseConfig() || 
				 hasCreatureDamageConfig() || hasPlayerDamageConfig() || hasItemDamageConfig());
	}
}