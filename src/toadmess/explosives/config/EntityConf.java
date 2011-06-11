package toadmess.explosives.config;

import static toadmess.explosives.config.ConfProps.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;

import toadmess.explosives.Bounds;
import toadmess.explosives.MCNative;

/**
 * Represents a single entity's configuration within a single world.
 * 
 * @author John Revill
 */
public class EntityConf {
	private final Logger log; 
	
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
	
	public EntityConf(final EntityConf parent, final Configuration conf, final String confPathPrefix, final Logger log) {
		this(parent, conf, confPathPrefix, log, new Random());
	}
	
	public EntityConf(final Configuration conf, final String confPathPrefix, final Logger log) {
		this(null, conf, confPathPrefix, log, new Random());
	}
	
	public EntityConf(final EntityConf parent, final Configuration conf, final String confPathPrefix, final Logger log, final Random rng) {
		this.log = log;
		
		this.rng = rng;
		
		this.parent = parent;
		
		this.properties = new Object[ConfProps.values().length];
		
		this.properties[CONF_BOUNDS.ordinal()] = new Bounds(conf, confPathPrefix);
		
		this.properties[CONF_ENTITY_RADIUSMULT.ordinal()] = readMultipliers(conf, confPathPrefix + "." + CONF_ENTITY_RADIUSMULT);
		this.properties[CONF_ENTITY_PLAYER_DAMAGEMULT.ordinal()] = readMultipliers(conf, confPathPrefix + "." + CONF_ENTITY_PLAYER_DAMAGEMULT);
		this.properties[CONF_ENTITY_CREATURE_DAMAGEMULT.ordinal()] = readMultipliers(conf, confPathPrefix + "." + CONF_ENTITY_CREATURE_DAMAGEMULT);
		this.properties[CONF_ENTITY_ITEM_DAMAGEMULT.ordinal()] = readMultipliers(conf, confPathPrefix + "." + CONF_ENTITY_ITEM_DAMAGEMULT);
		this.properties[CONF_ENTITY_TNT_FUSEMULT.ordinal()] = readMultipliers(conf, confPathPrefix + "." + CONF_ENTITY_TNT_FUSEMULT);

		this.properties[CONF_ENTITY_FIRE.ordinal()] = conf.getProperty(confPathPrefix + "." + CONF_ENTITY_FIRE);
		this.properties[CONF_ENTITY_PREVENT_TERRAIN_DAMAGE.ordinal()] = conf.getProperty(confPathPrefix + "." + CONF_ENTITY_PREVENT_TERRAIN_DAMAGE);

		this.properties[CONF_ENTITY_YIELD.ordinal()] = readOptionalFloat(conf, confPathPrefix + "." + CONF_ENTITY_YIELD);
		this.properties[CONF_ENTITY_YIELD_SPECIFIC.ordinal()] = readSpecificYields(conf, confPathPrefix + "." + CONF_ENTITY_YIELD_SPECIFIC);
		
		this.properties[CONF_ENTITY_TNT_TRIGGER_PREVENTED.ordinal()] = conf.getProperty(confPathPrefix + "." + CONF_ENTITY_TNT_TRIGGER_PREVENTED);
		
		this.properties[CONF_ENTITY_TNT_TRIGGER_HAND.ordinal()] = readSubConfig(conf, confPathPrefix + "." + CONF_ENTITY_TNT_TRIGGER_HAND);
		this.properties[CONF_ENTITY_TNT_TRIGGER_FIRE.ordinal()] = readSubConfig(conf, confPathPrefix + "." + CONF_ENTITY_TNT_TRIGGER_FIRE);
		this.properties[CONF_ENTITY_TNT_TRIGGER_REDSTONE.ordinal()] = readSubConfig(conf, confPathPrefix + "." + CONF_ENTITY_TNT_TRIGGER_REDSTONE);
		this.properties[CONF_ENTITY_TNT_TRIGGER_EXPLOSION.ordinal()] = readSubConfig(conf, confPathPrefix + "." + CONF_ENTITY_TNT_TRIGGER_EXPLOSION);
		
		if(null != conf.getProperty(confPathPrefix + ".trialTNTFuseMultiplier")) {
			this.log.warning("HigherExplosives: The \"trialTNTFuseMultiplier\" configuration is no longer used. Please rename it to \"" + CONF_ENTITY_TNT_FUSEMULT + "\"");
		}
		
		if(null != conf.getProperty("everyExplosion") || 
		   null != conf.getProperty(confPathPrefix + ".everyExplosion")) {
			this.log.warning("HigherExplosives: The \"everyExplosion\" configuration is no longer used. Instead, specify the explosion \"yield\" on the individual entities.");
		}
	}

	private EntityConf readSubConfig(final Configuration conf, final String confPathPrefix) {
		if(conf.getProperty(confPathPrefix) == null) {
			// There is no sub entity config at this configuration path 
			return null; 
		}
		
		return new EntityConf(this, conf, confPathPrefix, this.log);
	}

	private Float readOptionalFloat(final Configuration conf, final String path) {
		final Object o = conf.getProperty(path);
		
		if(o == null || !(o instanceof Number)) {
			return null;
		}
		
		return Double.valueOf(conf.getDouble(path, 0.0D)).floatValue();
	}
		
	/**
	 * @return A sparse array whose index is a specific block type ID and whose value is the yield 
	 * percentage (from 0.0 to 1.0) to use for that block type. 
	 */
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
	 * Otherwise, a list of pairs of floats. Each pair is a chance (head) and multiplier (tail).
	 * This is a sorted list of pairs of floats representing the multipliers to apply. 
	 * The pair's head is the chance (0.0 to 1.0) and tail is the multiplier (0.0 and above). 
	 * The list is sorted according to the pair's chance, pairs with higher chances coming first.
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

					final Object chanceProp = chanceAndValueProp.get(CONF_MULTIPLIER_CHANCE.toString());
					final Object valueProp = chanceAndValueProp.get(CONF_MULTIPLIER_VALUE.toString());

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

	public String toString() {
		String str = ""; 
		
		str += "activeBounds=" + this.getInheritedProp(CONF_BOUNDS) + "\n";		
		
		str += multipliersToString(ConfProps.CONF_ENTITY_RADIUSMULT);
		str += multipliersToString(ConfProps.CONF_ENTITY_PLAYER_DAMAGEMULT);
		str += multipliersToString(ConfProps.CONF_ENTITY_CREATURE_DAMAGEMULT);
		str += multipliersToString(ConfProps.CONF_ENTITY_ITEM_DAMAGEMULT);
		str += multipliersToString(ConfProps.CONF_ENTITY_TNT_FUSEMULT);
		
		str += propToString(ConfProps.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE);
		str += propToString(ConfProps.CONF_ENTITY_FIRE);
		str += propToString(ConfProps.CONF_ENTITY_YIELD);
		
		str += specificYieldsToString();
		
		str += propToString(ConfProps.CONF_ENTITY_TNT_TRIGGER_PREVENTED);
		
		str += subConfigToString(ConfProps.CONF_ENTITY_TNT_TRIGGER_HAND);
		str += subConfigToString(ConfProps.CONF_ENTITY_TNT_TRIGGER_FIRE);
		str += subConfigToString(ConfProps.CONF_ENTITY_TNT_TRIGGER_REDSTONE);
		str += subConfigToString(ConfProps.CONF_ENTITY_TNT_TRIGGER_EXPLOSION);
		
		return "Conf(\n" + indent(str) + "\n)";
	}

	private String indent(final String lines) {
		String indented = "";
		
		for(final String line : lines.split("\n")) {
			indented += indented.equals("") ? "" : "\n" ;
			indented += "  " + line;
		}
		
		return indented;
	}

	private String subConfigToString(final ConfProps subConfProperty) {
		final EntityConf subConf = (EntityConf) this.properties[subConfProperty.ordinal()];
		
		String str = "";
		
		if(isSubConfig()) {
			// Some sub-config properties don't make sense themselves in sub configs, such as these..
			switch(subConfProperty) {
			case CONF_ENTITY_TNT_TRIGGER_HAND:
			case CONF_ENTITY_TNT_TRIGGER_FIRE:
			case CONF_ENTITY_TNT_TRIGGER_REDSTONE:
			case CONF_ENTITY_TNT_TRIGGER_EXPLOSION:
				return "";
			default:
				// Carry on..
			}
		}
		
		if(subConf == null) {
			str += "no sub-configuration specified";
		} else {
			str += subConf.toString();
		}
		
		return subConfProperty.toString() + "=" + str + ",\n";
	}
	
	private String propToString(final ConfProps confProperty) {
		String str = "";
		
		final Object prop = this.getInheritedProp(confProperty);
		
		if(prop == null) {
			if(isSubConfig()) {
				return "";
			} else {
				str += "not configured, will be left unaffected";				
			}
		} else if(!hasOwnProp(confProperty)) {
			str += "inherited";
		} else {			
			str += prop;
		}
		
		return confProperty.toString() + "=" + str + ",\n";
	}
	
	private String multipliersToString(final ConfProps multiplierProperty) {
		String str = "";
		
		@SuppressWarnings("unchecked")
		final List<List<Float>> paramList = (List<List<Float>>) getInheritedProp(multiplierProperty);
		if(paramList == null) {
			if(isSubConfig()) {
				return ""; // For conciseness, don't bother showing unspecified properties for sub configs
			} else {
				str += "no multiplier configured. will leave unaffected";
			}
		} else if(!hasOwnProp(multiplierProperty)) {
			str += "inherited";
		} else {
			str += "\n";
			
			for (final List<?> localList : paramList) {
				str = str + "(chance:" + localList.get(0) + ", value:" + localList.get(1) + ")\n";
			}
			str = "{" + indent(str) + "\n}";
		}

		return multiplierProperty + "=" + str + ",\n";
	}

	private String specificYieldsToString() {
		final Float[] specificYields = this.getSpecificYieldConfig();
		
		String str = "";
		
		if(specificYields == null) {
			if(isSubConfig()) {
				return "";
			}
			str += "no specific block yields configured";
		} else if(!hasOwnProp(ConfProps.CONF_ENTITY_YIELD_SPECIFIC)) {
			str += "inherited";
		} else {
			for (int i = 0; i < specificYields.length; i++) {
				if(specificYields[i] != null) {
					str += "(block ID " + i + " has yield " + specificYields[i] + ")\n";
				}
			}
			str = "{\n" + indent(str) + "\n}";
		}
		
		return "yieldSpecific=" + str + ",\n";
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
	
	private boolean hasOwnProp(final ConfProps property) {
		return (hasInheritedProp(property) && (this.properties[property.ordinal()] != null));
	}
	
	private boolean hasInheritedProp(final ConfProps property) {
		return null != getInheritedProp(property);
	}
	
	private Object getInheritedProp(final ConfProps property) {
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
	
	private boolean isSubConfig() {
		return this.parent != null;
	}
}