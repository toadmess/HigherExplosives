package toadmess.explosives.config.entity;

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
import toadmess.explosives.config.ConfProps;

public class EntityConfReader {
	private final Logger log;
	private final Configuration conf;
	private final String confPathPrefix;
	private final EntityConf parent; 
	private final Random rng;
	
	public EntityConfReader(final Configuration conf, final String confPathPrefix, final Logger log) {
		this(conf, confPathPrefix, log, null, new Random());
	}
	
	public EntityConfReader(final Configuration conf, final String confPathPrefix, final Logger log, final EntityConf parent) {
		this(conf, confPathPrefix, log, parent, new Random());
	}
	
	public EntityConfReader(final Configuration conf, final String confPathPrefix, final Logger log, final EntityConf parent, final Random rng) {
		this.log = log;
		this.conf = conf;
		this.confPathPrefix = confPathPrefix;
		this.parent = parent;
		this.rng = rng;
	}
	
	public EntityConf readEntityConf() {
		final EntityConf ec = new EntityConf(this.parent, this.rng);
		
		final Object[] properties = new Object[ConfProps.values().length];
		
		readBounds(properties);
		
		readMultipliers(properties, CONF_ENTITY_RADIUSMULT);
		readMultipliers(properties, CONF_ENTITY_PLAYER_DAMAGEMULT);
		readMultipliers(properties, CONF_ENTITY_CREATURE_DAMAGEMULT);
		readMultipliers(properties, CONF_ENTITY_ITEM_DAMAGEMULT);
		readMultipliers(properties, CONF_ENTITY_TNT_FUSEMULT);

		readProperty(properties, CONF_ENTITY_FIRE);
		readProperty(properties, CONF_ENTITY_PREVENT_TERRAIN_DAMAGE);

		readOptionalFloat(properties, CONF_ENTITY_YIELD);
		readSpecificYields(properties);
		
		readProperty(properties, CONF_ENTITY_TNT_TRIGGER_PREVENTED);
		
		readSubConfig(ec, properties, CONF_ENTITY_TNT_TRIGGER_HAND);
		readSubConfig(ec, properties, CONF_ENTITY_TNT_TRIGGER_FIRE);
		readSubConfig(ec, properties, CONF_ENTITY_TNT_TRIGGER_REDSTONE);
		readSubConfig(ec, properties, CONF_ENTITY_TNT_TRIGGER_EXPLOSION);
		readSubConfig(ec, properties, CONF_ENTITY_CREEPER_CHARGED);
		
		if(null != this.conf.getProperty(confPathPrefix + ".trialTNTFuseMultiplier")) {
			this.log.warning("HigherExplosives: The \"trialTNTFuseMultiplier\" configuration is no longer used. Please rename it to \"" + CONF_ENTITY_TNT_FUSEMULT + "\"");
		}
		
		if(null != this.conf.getProperty("everyExplosion") || 
		   null != this.conf.getProperty(confPathPrefix + ".everyExplosion")) {
			this.log.warning("HigherExplosives: The \"everyExplosion\" configuration is no longer used. Instead, specify the explosion \"yield\" on the individual entities.");
		}
		
		ec.setProperties(properties);
		
		return ec;
	}

	private void readSubConfig(final EntityConf parent, final Object[] properties, final ConfProps confProperty) {
		final String confPath = this.confPathPrefix + "." + confProperty.toString();
		
		if(this.conf.getProperty(confPath) == null) {
			// There is no sub entity config at this configuration path 
			properties[confProperty.ordinal()] = null;
		} else {
			final EntityConfReader ecr = new EntityConfReader(this.conf, confPath, this.log, parent);
			
			properties[confProperty.ordinal()] = ecr.readEntityConf();			
		}
		
	}

	private void readProperty(final Object[] properties, final ConfProps confProperty) {
		properties[confProperty.ordinal()] = this.conf.getProperty(this.confPathPrefix + "." + confProperty);
	}
	
	private void readOptionalFloat(final Object[] properties, final ConfProps confProperty) {
		final String path = this.confPathPrefix + "." + confProperty;
		
		final Object o = this.conf.getProperty(path);
		
		if(o == null || !(o instanceof Number)) {
			properties[confProperty.ordinal()] = null;
		} else {
			properties[confProperty.ordinal()] = Double.valueOf(this.conf.getDouble(path, 0.0D)).floatValue();
		}
	}
	
	private void readBounds(final Object[] properties) {
		if(this.parent != null) {
			// Make all sub-configs inherit bounds from the parent
			properties[CONF_BOUNDS.ordinal()] = null;
		} else {			
			properties[CONF_BOUNDS.ordinal()] = new Bounds(this.conf, this.confPathPrefix);
		}
		
	}
		
	/**
	 * @return A sparse array whose index is a specific block type ID and whose value is the yield 
	 * percentage (from 0.0 to 1.0) to use for that block type. 
	 */
	private void readSpecificYields(final Object[] properties) {
		final String pathToYieldSpecific = this.confPathPrefix + "." + CONF_ENTITY_YIELD_SPECIFIC;
		
		final Object specYieldsProp = this.conf.getProperty(pathToYieldSpecific);
		if (specYieldsProp instanceof HashMap<?,?>) {
			final HashMap<?,?> specYields = (HashMap<?,?>) specYieldsProp;
			
			final Float[] yieldsSparseArr = new Float[MCNative.getHighestBlockId()+1];
			
			for(final Object blockID : specYields.keySet()) {
				yieldsSparseArr[(Integer) blockID] = ((Number) specYields.get(blockID)).floatValue();
			}
			
			properties[CONF_ENTITY_YIELD_SPECIFIC.ordinal()] = yieldsSparseArr;
		} else {
			properties[CONF_ENTITY_YIELD_SPECIFIC.ordinal()] = null;			
		}
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
	private void readMultipliers(final Object[] properties, final ConfProps confProperty) {
		final String pathToMultiplier = this.confPathPrefix + "." + confProperty; 
		
		final List<List<Float>> multipliers = new ArrayList<List<Float>>(); // A list of pairs of floats. Each pair is a chance (head) and multiplier (tail).
		
		// First extract the multiplier chance/value pairs from the configuration and add them all to the multipliers list. 
		final Object multiplierProp = this.conf.getProperty(pathToMultiplier);
		if (multiplierProp instanceof Number) {
			addMultiplier(multipliers, 1.0F, Math.max(0.0F, (float)this.conf.getDouble(pathToMultiplier, 1.0D)));
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
			properties[confProperty.ordinal()] = null;
		} else {
			properties[confProperty.ordinal()] = multipliers;			
		}
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
}