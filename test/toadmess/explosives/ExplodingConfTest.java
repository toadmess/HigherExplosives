package toadmess.explosives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.util.config.Configuration;
import org.junit.Before;
import org.junit.Test;

public class ExplodingConfTest {
	private Configuration conf;
	
	@Before
	public void setup() {
		conf = new Configuration(null);
	}
	
	@Test
	public void testEmptyConfig() {
		checkEmptyConfigDefaults(conf, "");
	}
	
	@Test
	public void testEmptyConfig_InvalidPath() {
		checkEmptyConfigDefaults(conf, "someplace.overtherainbow");
	}
	
	private void checkEmptyConfigDefaults(final Configuration conf, final String confPath) {
		final ExplodingConf ec = new ExplodingConf(conf, confPath);
		
		// Check some defualt bounds are created
		checkBoundsAreDefault(ec);
		
		// Check the fire defaults to false even though it's not in the config.
		checkFire(ec, false, false);
		
		// Check the next radius multiplier defaults to 1.0 event though none was specified in the config
		checkRadiusMultiplier(ec, 1.0F, false);
		checkRadiusMultiplier(ec, 1.0F, false);
		
		// Check the next player damage multiplier defaults to 1.0 even though none was specified in the config
		checkPlayerDmgMultiplier(ec, 1.0F, false);
		checkPlayerDmgMultiplier(ec, 1.0F, false);
		
		// Check the next creature damage multiplier defaults to 1.0 even though none was specified in the config
		checkCreatureDmgMultiplier(ec, 1.0F, false);
		checkCreatureDmgMultiplier(ec, 1.0F, false);
	}
	
	@Test
	public void testFire_False() { testFire(false); }
	
	@Test
	public void testFire_True() { testFire(true); }
	
	private void testFire(final boolean confHasFireSetToTrue) {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_FIRE, confHasFireSetToTrue);
		checkFire(new ExplodingConf(conf, "someentity"), confHasFireSetToTrue, true);
	}
	
	@Test
	public void testRadiusMultiplierSingle() {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_RADIUSMULT, 0.23F);
		final ExplodingConf ec = new ExplodingConf(conf, "someentity");
		checkRadiusMultiplier(ec, 0.23F, true);
		checkRadiusMultiplier(ec, 0.23F, true);
		checkRadiusMultiplier(ec, 0.23F, true);
	}
	
	@Test
	public void testRadiusMultiplierMulti() {
		checkMultipliers(eMultiplierType.RADIUS);
	}

	private enum eMultiplierType { RADIUS, PLAYER_DMG, CREATURE_DMG };
	private void checkMultipliers(final eMultiplierType multiplerType) {
		final String confPrefix = "someentity";
		
		/**
		 * Set up a configuration like:
		 * 
		 * radiusMultiplier:
		 *     - {chance: 0.2, value: 2.0}
         *     - {chance: 0.7, value: 1.0}
         *     - {chance: 0.1, value: 0.0}
		 */
		final List<HashMap<String,Object>> listOMultipliers = new ArrayList<HashMap<String,Object>>();
		final HashMap<String,Object> firstMultiplier = new HashMap<String,Object>();
		final HashMap<String,Object> secondMultiplier = new HashMap<String,Object>();
		final HashMap<String,Object> thirdMultiplier = new HashMap<String,Object>();
		
		listOMultipliers.add(firstMultiplier);
		listOMultipliers.add(secondMultiplier);
		listOMultipliers.add(thirdMultiplier);
		
		firstMultiplier.put(HEMain.CONF_MULTIPLIER_CHANCE, 0.2D); 
		firstMultiplier.put(HEMain.CONF_MULTIPLIER_VALUE, 2.0D);
		
		secondMultiplier.put(HEMain.CONF_MULTIPLIER_CHANCE, 0.7D); 
		secondMultiplier.put(HEMain.CONF_MULTIPLIER_VALUE, 1.0D);
		
		thirdMultiplier.put(HEMain.CONF_MULTIPLIER_CHANCE, 0.1D); 
		thirdMultiplier.put(HEMain.CONF_MULTIPLIER_VALUE, 0.0D);
		
		conf.setProperty(confPrefix + "." + HEMain.CONF_ENTITY_RADIUSMULT, listOMultipliers);
		
		final PredicatableNumGen rng = new PredicatableNumGen(0.01D, 1.0D);
		final ExplodingConf ec = new ExplodingConf(conf, confPrefix, rng);
		
		for(int i = 0; i < 5; i++) { // Make our fake RNG Cycle round a few times
			final float expectedMultiplier;
			if(rng.peekNextDouble() <= 0.7D) {
				expectedMultiplier = 1.0F;
			} else if (rng.peekNextDouble() <= 0.9D) {
				expectedMultiplier = 2.0F;
			} else {
				expectedMultiplier = 0.0F;
			}

			switch(multiplerType) {
			case RADIUS:
				checkRadiusMultiplier(ec, expectedMultiplier, true);
				break;
			case PLAYER_DMG:
				checkPlayerDmgMultiplier(ec, expectedMultiplier, true);
				break;
			case CREATURE_DMG:
				checkCreatureDmgMultiplier(ec, expectedMultiplier, true);
				break;
			}
		}
	}
	
	@Test
	public void testPlayerDmgMultiplierSingle() {
		fail("TODO");
	}

	@Test
	public void testPlayerDmgMultiplierMulti() {
		checkMultipliers(eMultiplierType.PLAYER_DMG);
	}

	@Test
	public void testCreatureDmgMultiplierSingle() {
		fail("TODO");
	}

	@Test
	public void testCreatureDmgMultiplierMulti() {
		checkMultipliers(eMultiplierType.CREATURE_DMG);
	}
	
	private void checkRadiusMultiplier(final ExplodingConf ec, final float expectMultiplier, final boolean expectRadiusConfig) {
		assertEquals((Float) expectMultiplier, (Float) ec.getNextRadiusMultiplier());
		assertEquals(expectRadiusConfig, ec.hasRadiusConfig());
	}
	
	private void checkPlayerDmgMultiplier(final ExplodingConf ec, final float expectMultiplier, final boolean expectPlayerDmgConfig) {
		assertEquals((Float) expectMultiplier, (Float) ec.getNextRadiusMultiplier());
		assertEquals(expectPlayerDmgConfig, ec.hasPlayerDamageConfig());
	}
	
	private void checkCreatureDmgMultiplier(final ExplodingConf ec, final float expectMultiplier, final boolean expectCreatureDmgConfig) {
		assertEquals((Float) expectMultiplier, (Float) ec.getNextRadiusMultiplier());
		assertEquals(expectCreatureDmgConfig, ec.hasCreatureDamageConfig());
	}

	private void checkFire(final ExplodingConf ec, final boolean expectFire, final boolean expectFireConfig) {
		assertEquals(expectFire, ec.getFire());
		assertEquals(expectFireConfig, ec.hasFireConfig());
	}

	private void checkBoundsAreDefault(final ExplodingConf ec) {
		assertNull(ec.getActiveBounds().getMaxX());
		assertNull(ec.getActiveBounds().getMaxY());
		assertNull(ec.getActiveBounds().getMaxZ());
		assertNull(ec.getActiveBounds().getMaxX());
		assertNull(ec.getActiveBounds().getMaxY());
		assertNull(ec.getActiveBounds().getMaxZ());
	}
}
