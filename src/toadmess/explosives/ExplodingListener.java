package toadmess.explosives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimedEvent;
import org.bukkit.util.config.Configuration;

/**
 * <p>
 * This listens to all explosions just before they explode. 
 * Each instance of this listener will listen to a specific type of explosive entity (e.g. Creeper, 
 * TNTPrimed, or Fireball).
 * </p><p>
 * When an explosion is about to happen, this listener will check whether the epicentre of the 
 * explosion lies within some allowed bounds (an optional configuration). If the epicentre is 
 * outside of these bounds, the explosion is left untouched. If there are no bounds, or the 
 * epicentre is within the acceptable bounds, then:
 * </p>
 * <ul>
 * <li>The explosion's radius is multiplied by the entity's "radiusMultiplier" configuration value.
 * <li>The explosion will be flagged as causing fire if the "fire" configuration is true.
 * </ul>
 * 
 * @author John Revill
 */
public class ExplodingListener extends EntityListener {
	private final Class<?> entityType;
	
	private final ExplodingConf defWorldConf;
	
	private final HashMap<String, ExplodingConf> otherWorldConfs;
	
	public ExplodingListener(final Configuration conf, final Class<? extends Entity> entityType) {
		// Get the unqualified class name of the entity. This is used for looking it up in the configuration.
		final String entityName = entityType.getName().substring(entityType.getName().lastIndexOf('.')+1);
		
		final String confEntityPath = HEMain.CONF_ENTITIES + "." + entityName;
		
		this.defWorldConf = new ExplodingConf(conf, confEntityPath);
		if(HEMain.IS_DEBUG_CONF) {
			System.out.println("Default config for " + entityName + " is " + this.defWorldConf);
		}
		
		this.otherWorldConfs = new HashMap<String, ExplodingConf>();
		final List<String> worldNames = conf.getKeys(HEMain.CONF_WORLDS);
		if(null != worldNames) {
			for(final String worldName : worldNames) {
				final String worldEntityPath = HEMain.CONF_WORLDS + "." + worldName + "." + confEntityPath;
			
				if(null != conf.getProperty(worldEntityPath)) {
					final ExplodingConf worldConf = new ExplodingConf(conf, worldEntityPath);
					
					this.otherWorldConfs.put(worldName, worldConf);
					if(HEMain.IS_DEBUG_CONF) {
						System.out.println(worldName + " config for " + entityName + " is " + worldConf);
					}
				}
			}
		}
		
		this.entityType = entityType; 
	}

	@Override
	public void onExplosionPrimed(final ExplosionPrimedEvent event) {
		final Entity primed = event.getEntity();

		if(!isValidPrimedEntity(primed)) {
			return;
		}

		final Location epicentre = primed.getLocation();
		final ExplodingConf worldConf = findWorldConf(epicentre.getWorld());
		
		if(!worldConf.allowedBounds.isWithinBounds(epicentre)) {
			return;
		}
		
		event.setRadius(worldConf.getNextMultiplier() * event.getRadius());
		event.setFire(worldConf.fire);
	}

	private boolean isValidPrimedEntity(final Entity e) {
		return (null != e && this.entityType.isInstance(e));
	}
	
	private ExplodingConf findWorldConf(final World world) {
		final String worldName = world.getName();
		if(this.otherWorldConfs.containsKey(worldName)) {
			return this.otherWorldConfs.get(worldName);
		}
		
		return this.defWorldConf;
	}
	
	protected class ExplodingConf {
		/** 
		 * List of pairs (sorted by chance of happening, highest first), where each
		 * pair contains the cumulative chance (first) and the multiplier (second). 
		 */
		private final List<List<Float>> radiusMultipliers = new ArrayList<List<Float>>();
		
		private final Bounds allowedBounds;
		private final boolean fire;
		
		private Random rng = new Random();

		public ExplodingConf(final Configuration conf, final String pathToEntity) {
			this.allowedBounds = new Bounds(conf, pathToEntity + "." + HEMain.CONF_BOUNDS);
			
			final String radiusPath = pathToEntity + "." + HEMain.CONF_ENTITY_RADIUSMULT;
			
			final Object radiusProp = conf.getProperty(radiusPath);
			if(radiusProp instanceof Number) {
				this.addMultiplier(1.0F, (Math.max(0.0f, (float) conf.getDouble(radiusPath, 1.0f))));
			} else if(radiusProp instanceof List) {
				for(final Object el : (List<?>) radiusProp) {
					if(el instanceof HashMap) {
						final HashMap<?, ?> probAndValue = (HashMap<?, ?>) el;

						final Object chance = probAndValue.get(HEMain.CONF_ENTITY_RADIUSMULT_CHANCE);
						final Object value = probAndValue.get(HEMain.CONF_ENTITY_RADIUSMULT_VALUE);
						
						if(chance instanceof Double && value instanceof Double) {
							this.addMultiplier((float) Math.min(1.0D, Math.max(0D, (Double) chance)), (float) Math.max(0.0D, (Double) value));							
						} else {
							System.out.println("WARN: HigherExplosives: Ignoring list item in the radiusMultiplier list (either the "+HEMain.CONF_ENTITY_RADIUSMULT_CHANCE+" of ("+chance+") or the "+HEMain.CONF_ENTITY_RADIUSMULT_VALUE+" of ("+value+") doesn't look like a number, was expecting a double)");						
						}
					} else {
						System.out.println("WARN: HigherExplosives: Ignoring strange list item in the radiusMultiplier list. Was expecting "+HEMain.CONF_ENTITY_RADIUSMULT_CHANCE+" and "+HEMain.CONF_ENTITY_RADIUSMULT_VALUE+" keys");
					}
				}

				float totalProb = 0.0F;
				for(List<Float> pair : this.radiusMultipliers) {
					totalProb += pair.get(0);
				}
				
				if(Math.abs(totalProb - 1.0F) > 0.0001) {
					System.out.println("WARN: HigherExplosives: Total probability for radiusMultiplier doesn't quite add up to 1.0. It's " + totalProb);
				}				
			}
			
			if(radiusMultipliers.size() == 0) {
				this.addMultiplier(1.0F, 0.3F);
			}
			
			this.fire = conf.getBoolean(pathToEntity + "." + HEMain.CONF_ENTITY_FIRE, false);
		}
		
		private void addMultiplier(float chance, float multiplier) {
			final List<Float> pair = new ArrayList<Float>(2);
			
			pair.add(chance);
			pair.add(multiplier);
		
			this.radiusMultipliers.add(pair);

			final Comparator<List<Float>> cmp = new Comparator<List<Float>>() {
				@Override
				public int compare(final List<Float> a, final List<Float> b) {
					if(a.get(0) == b.get(0)) {
						return 0;
					}
					
					if(a.get(0) < b.get(0)) return 1;
					
					return -1;
				}
			};
			Collections.sort(this.radiusMultipliers, cmp);
		}

		private float getNextMultiplier() {
			// Walk through the list until the number is exceeded
			float choice = rng.nextFloat();

			float cumulativeProb = 0.0F;
			for(final List<Float> pair : this.radiusMultipliers) {
				cumulativeProb += pair.get(0);
				
				if(cumulativeProb > choice) {
					return pair.get(1);
				}
			}
			
			// Shouldn't really be here if the probabilities all added up to one..
			return radiusMultipliers.get(0).get(1);
		}
		
		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer("ExplodingConf(");
			
			sb.append(HEMain.CONF_ENTITY_RADIUSMULT);
			sb.append("={");
			for(final List<Float> pair : radiusMultipliers) {
				sb.append("(").append(HEMain.CONF_ENTITY_RADIUSMULT).append(":");
				sb.append(pair.get(0));
				sb.append(",value:").append(pair.get(1)).append(")");
			}
			sb.append("},");
			sb.append(HEMain.CONF_ENTITY_FIRE).append("=").append(fire);
			sb.append(",").append(HEMain.CONF_BOUNDS).append("=").append(allowedBounds).append(")");
			
			return sb.toString();
		}
	}
}
