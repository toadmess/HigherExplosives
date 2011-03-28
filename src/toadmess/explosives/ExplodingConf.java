package toadmess.explosives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.util.config.Configuration;

public class ExplodingConf {
	private final List<List<Float>> radiusMultipliers;
	private final List<List<Float>> playerDamageMultipliers;
	private final List<List<Float>> creatureDamageMultipliers;

	private final Bounds allowedBounds;
	private final boolean fire;

	private final Random rng = new Random();

	public ExplodingConf(final Configuration conf, final String confPathPrefix) {
		this.allowedBounds = new Bounds(conf, confPathPrefix + "." + HEMain.CONF_BOUNDS);

		this.radiusMultipliers = getMultipliers(conf, confPathPrefix + "." + HEMain.CONF_ENTITY_RADIUSMULT);
		this.playerDamageMultipliers = getMultipliers(conf, confPathPrefix + "." + HEMain.CONF_ENTITY_PLAYER_DAMAGEMULT);
		this.creatureDamageMultipliers = getMultipliers(conf, confPathPrefix + "." + HEMain.CONF_ENTITY_CREATURE_DAMAGEMULT);

		this.fire = conf.getBoolean(confPathPrefix + "." + HEMain.CONF_ENTITY_FIRE, false);
	}

	/**
	 * Extracts a list of chance/multipler pairs from some place in the configuration. 
	 * It detects whether the configuration contains just a single multiplier, or if there are several chance/multipliers listed.
	 * 
	 * @param conf
	 * @param pathToMultiplier The configuration path prefix up to where the chance/value(s) are listed.
	 * 
	 * @return A list of pairs of floats. Each pair is a chance (head) and multiplier (tail). 
	 */
	private List<List<Float>> getMultipliers(final Configuration conf, final String pathToMultiplier) {
		final List<List<Float>> multipliers = new ArrayList<List<Float>>(); // A list of pairs of floats. Each pair is a chance (head) and multiplier (tail).

		// First extract the multiplier chance/value pairs from the configuration and add them all to the multipliers list. 
		final Object multiplierProp = conf.getProperty(pathToMultiplier);
		if (multiplierProp instanceof Number) {
			addMultiplier(multipliers, 1.0F, Math.max(0.0F, (float)conf.getDouble(pathToMultiplier, 1.0D)));
		} else if (multiplierProp instanceof List<?>) {
			for(final Object multiplierListItemProp : (List<?>) multiplierProp) {
				if (multiplierListItemProp instanceof HashMap<?,?>) {
					final HashMap<?,?> chanceAndValueProp = (HashMap<?,?>) multiplierListItemProp;

					final Object chanceProp = chanceAndValueProp.get(HEMain.CONF_MULTIPLIER_CHANCE);
					final Object valueProp = chanceAndValueProp.get(HEMain.CONF_MULTIPLIER_VALUE);

					if ((chanceProp instanceof Double) && (valueProp instanceof Double)) {
						addMultiplier(multipliers, (float)Math.min(1.0D, Math.max(0.0D, ((Double)chanceProp).doubleValue())), (float)Math.max(0.0D, ((Double)valueProp).doubleValue()));
					} else {
						System.out.println("WARN: HigherExplosives: Config problem. Ignoring list item under " + pathToMultiplier + " as either the " + HEMain.CONF_MULTIPLIER_CHANCE + " of (" + chanceProp + ") or the " + HEMain.CONF_MULTIPLIER_VALUE + " of (" + valueProp + ") doesn't look like a number. Was expecting a double.");
					}
				} else {
					System.out.println("WARN: HigherExplosives: Config problem. Ignoring strange list item under " + pathToMultiplier + ". Was expecting " + HEMain.CONF_MULTIPLIER_CHANCE + " and " + HEMain.CONF_MULTIPLIER_VALUE + " keys");
				}
			}

			// Make a check that the cumulative chance of all the chance/value pairs totals up to 1.0
			float cumulativeChance = 0.0F;
			for(final List<Float> chanceValuePair : multipliers) {
				cumulativeChance += chanceValuePair.get(0);
			}

			if (Math.abs(cumulativeChance - 1.0F) > 0.0001D) {
				System.out.println("WARN: HigherExplosives: Config problem. Total probability for " + pathToMultiplier + " doesn't quite " + "add up to 1.0. It's " + cumulativeChance);
			}

		}

		// Add in a default multiplier if nothing was found in the configuration
		if (multipliers.size() == 0) {
			addMultiplier(multipliers, 1.0F, 1.0F);
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
		final float randomNum = this.rng.nextFloat();
		float cumulativeChance = 0.0F;

		for (final List<Float> multiplierPair : sortedMultipliers) {
			cumulativeChance += multiplierPair.get(0);

			if (cumulativeChance > randomNum) {
				return multiplierPair.get(1);
			}
		}

		// Well, the chance values didn't add up to 1.0. Just return the most likely multiplier (the first)
		return sortedMultipliers.get(0).get(1);
	}

	public String toString() {
		String str = "ExplodingConf(";

		str = str + "radiusMultiplier={" + multiplersToString(this.radiusMultipliers) + "},";
		str = str + "playerDamageMultiplier={" + multiplersToString(this.playerDamageMultipliers) + "},";
		str = str + "creatureDamageMultiplier={" + multiplersToString(this.creatureDamageMultipliers) + "},";
		str = str + "fire=" + this.fire + ",";
		str = str + "activeBounds=" + this.allowedBounds + ")";

		return str;
	}

	private String multiplersToString(final List<List<Float>> paramList) {
		String str = "";
		for (final List<?> localList : paramList) {
			str = str + "(chance:" + localList.get(0);
			str = str + ",value:" + localList.get(1) + ")";
		}
		return str;
	}

	public Bounds getActiveBounds() {
		return this.allowedBounds;
	}

	public boolean getFire() {
		return this.fire;
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
}