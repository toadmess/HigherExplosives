package toadmess.explosives;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.util.config.Configuration;
import org.junit.Before;
import org.junit.Test;

import toadmess.explosives.messymocks.NullLogger;
import toadmess.explosives.messymocks.PredicatableNumGen;

public class ExplodingConfTest {
	private Configuration conf;
	
	@Before
	public void setup() {
		conf = new Configuration(null);
		
		EntityConf.hasConflictWithMiningTNT = false;
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
		final EntityConf ec = new EntityConf(conf, confPath, new NullLogger());
		
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

		// Check the next item damage multiplier defaults to 1.0 even though none was specified in the config
		checkItemDmgMultiplier(ec, 1.0F, false);
		checkItemDmgMultiplier(ec, 1.0F, false);
		
		checkTNTFuseMultiplier(ec, 1.0F, false);
		checkTNTFuseMultiplier(ec, 1.0F, false);
		
		checkPreventTerrainDamage(ec, false, false);
		
		checkYield(ec, 0.3F, false);
		
		assertTrue(ec.isEmptyConfig());
	}
	
	@Test
	public void testFire_False() { testFire(false); }
	
	@Test
	public void testFire_True() { testFire(true); }
	
	private void testFire(final boolean confHasFireSetToTrue) {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_FIRE, confHasFireSetToTrue);
		checkFire(new EntityConf(conf, "someentity", new NullLogger()), confHasFireSetToTrue, true);
	}
	
	@Test
	public void testYield() {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_YIELD, 0.42D);
		checkYield(new EntityConf(conf, "someentity", new NullLogger()), 0.42F, true);
	}
	
	@Test
	public void testYield_Integer() {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_YIELD, 1);
		checkYield(new EntityConf(conf, "someentity", new NullLogger()), 1.0F, true);
	}
	
	@Test
	public void testYield_Float() {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_YIELD, 0.42F);
		checkYield(new EntityConf(conf, "someentity", new NullLogger()), 0.42F, true);
	}
	
	@Test
	public void testYield_WithMiningTNTConflict() {
		// Check that the default yield is 1.0 if the MiningTNT plugin was detected.
		EntityConf.hasConflictWithMiningTNT = true;
		checkYield(new EntityConf(conf, "someentity", new NullLogger()), 1.0F, true);
		
		// But check that the yield is as per the configuration if there is a configuration set. 
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_YIELD, 0.16F);
		checkYield(new EntityConf(conf, "someentity", new NullLogger()), 0.16F, true);
	}
	
	@Test
	public void testPreventTerrainDamage_False() { testPreventTerrainDamage(false); }
	
	@Test
	public void testPreventTerrainDamage_True() { testPreventTerrainDamage(true); }
	
	private void testPreventTerrainDamage(final boolean confHasPreventTerrainDamageSetToTrue) {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE, confHasPreventTerrainDamageSetToTrue);
		checkPreventTerrainDamage(new EntityConf(conf, "someentity", new NullLogger()), confHasPreventTerrainDamageSetToTrue, true);
	}
	
	@Test
	public void testRadiusMultiplierSingle() {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_RADIUSMULT, 0.23F);
		final EntityConf ec = new EntityConf(conf, "someentity", new NullLogger());
		checkRadiusMultiplier(ec, 0.23F, true);
		checkRadiusMultiplier(ec, 0.23F, true);
		checkRadiusMultiplier(ec, 0.23F, true);
	}
	
	@Test
	public void testRadiusMultiplierSingle_Int() {
		// Test int config
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_RADIUSMULT, 23);
		final EntityConf ec = new EntityConf(conf, "someentity", new NullLogger());
		checkRadiusMultiplier(ec, 23.0F, true);
	}
	
	@Test
	public void testRadiusMultiplierSingle_Double() {
		// Test Double config
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_RADIUSMULT, 42D);
		final EntityConf ec = new EntityConf(conf, "someentity", new NullLogger());
		checkRadiusMultiplier(ec, 42.0F, true);
	}
	
	@Test
	public void testRadiusMultiplierMulti() {
		checkMultipliers(eMultiplierType.RADIUS);
	}

	private enum eMultiplierType { 
		RADIUS(HEMain.CONF_ENTITY_RADIUSMULT), 
		PLAYER_DMG(HEMain.CONF_ENTITY_PLAYER_DAMAGEMULT), 
		CREATURE_DMG(HEMain.CONF_ENTITY_CREATURE_DAMAGEMULT),
		FUSE(HEMain.CONF_ENTITY_TNT_FUSEMULT),
		ITEM_DMG(HEMain.CONF_ENTITY_ITEM_DAMAGEMULT);
		private final String confKey;
		private eMultiplierType(final String confKey) { this.confKey = confKey;}
		protected String getConfKey() { return this.confKey; }
	};
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
		
		conf.setProperty(confPrefix + "." + multiplerType.getConfKey(), listOMultipliers);
		
		final PredicatableNumGen rng = new PredicatableNumGen(0.01D, 1.0D);
		final EntityConf ec = new EntityConf(conf, confPrefix, new NullLogger(), rng);
		
		for(int i = 0; i < 500; i++) { // Make our fake RNG Cycle round a few times
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
			case FUSE:
				checkTNTFuseMultiplier(ec, expectedMultiplier, true);
				break;
			}
		}
		
		assertToStringThrowsNothing(ec);
	}
	
	@Test
	public void testPlayerDmgMultiplierSingle() {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_PLAYER_DAMAGEMULT, 0.23F);
		final EntityConf ec = new EntityConf(conf, "someentity", new NullLogger());
		checkPlayerDmgMultiplier(ec, 0.23F, true);
		checkPlayerDmgMultiplier(ec, 0.23F, true);
		checkPlayerDmgMultiplier(ec, 0.23F, true);
	}

	@Test
	public void testPlayerDmgMultiplierMulti() {
		checkMultipliers(eMultiplierType.PLAYER_DMG);
	}

	@Test
	public void testCreatureDmgMultiplierSingle() {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_CREATURE_DAMAGEMULT, 0.23F);
		final EntityConf ec = new EntityConf(conf, "someentity", new NullLogger());
		checkCreatureDmgMultiplier(ec, 0.23F, true);
		checkCreatureDmgMultiplier(ec, 0.23F, true);
		checkCreatureDmgMultiplier(ec, 0.23F, true);
	}

	@Test
	public void testCreatureDmgMultiplierMulti() {
		checkMultipliers(eMultiplierType.CREATURE_DMG);
	}

	@Test
	public void testItemDmgMultiplierSingle() {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_ITEM_DAMAGEMULT, 0.23F);
		final EntityConf ec = new EntityConf(conf, "someentity", new NullLogger());
		checkItemDmgMultiplier(ec, 0.23F, true);
		checkItemDmgMultiplier(ec, 0.23F, true);
		checkItemDmgMultiplier(ec, 0.23F, true);
	}

	@Test
	public void testItemDmgMultiplierMulti() {
		checkMultipliers(eMultiplierType.ITEM_DMG);
	}

	@Test
	public void testFuseMultiplierSingle() {
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_TNT_FUSEMULT, 0.23F);
		final EntityConf ec = new EntityConf(conf, "someentity", new NullLogger());
		checkTNTFuseMultiplier(ec, 0.23F, true);
		checkTNTFuseMultiplier(ec, 0.23F, true);
		checkTNTFuseMultiplier(ec, 0.23F, true);
	}

	@Test
	public void testFuseMultiplierMulti() {
		checkMultipliers(eMultiplierType.FUSE);
	}
	
	private void checkRadiusMultiplier(final EntityConf ec, final float expectMultiplier, final boolean expectRadiusConfig) {
		assertEquals((Float) expectMultiplier, (Float) ec.getNextRadiusMultiplier());
		assertEquals(expectRadiusConfig, ec.hasRadiusConfig());
		if(expectRadiusConfig) assertFalse(ec.isEmptyConfig());
	}
	
	private void checkPlayerDmgMultiplier(final EntityConf ec, final float expectMultiplier, final boolean expectPlayerDmgConfig) {
		assertEquals((Float) expectMultiplier, (Float) ec.getNextPlayerDamageMultiplier());
		assertEquals(expectPlayerDmgConfig, ec.hasPlayerDamageConfig());
		if(expectPlayerDmgConfig) assertFalse(ec.isEmptyConfig());
	}
	
	private void checkCreatureDmgMultiplier(final EntityConf ec, final float expectMultiplier, final boolean expectCreatureDmgConfig) {
		assertEquals((Float) expectMultiplier, (Float) ec.getNextCreatureDamageMultiplier());
		assertEquals(expectCreatureDmgConfig, ec.hasCreatureDamageConfig());
		if(expectCreatureDmgConfig) assertFalse(ec.isEmptyConfig());
	}
	
	private void checkItemDmgMultiplier(final EntityConf ec, final float expectMultiplier, final boolean expectItemDmgConfig) {
		assertEquals((Float) expectMultiplier, (Float) ec.getNextItemDamageMultiplier());
		assertEquals(expectItemDmgConfig, ec.hasItemDamageConfig());
		if(expectItemDmgConfig) assertFalse(ec.isEmptyConfig());
	}
	
	private void checkTNTFuseMultiplier(final EntityConf ec, final float expectMultiplier, final boolean expectFuseConfig) {
		assertEquals((Float) expectMultiplier, (Float) ec.getNextTNTFuseMultiplier());
		assertEquals(expectFuseConfig, ec.hasTNTFuseConfig());
		if(expectFuseConfig) assertFalse(ec.isEmptyConfig());
	}

	private void checkFire(final EntityConf ec, final boolean expectFire, final boolean expectFireConfig) {
		assertEquals(expectFire, ec.getFire());
		assertEquals(expectFireConfig, ec.hasFireConfig());
		if(expectFireConfig) assertFalse(ec.isEmptyConfig());
	}
	
	private void checkPreventTerrainDamage(final EntityConf ec, final boolean expectTerrainDamage, final boolean expectTerrainDamageConfig) {
		assertEquals(expectTerrainDamage, ec.getPreventTerrainDamage());
		assertEquals(expectTerrainDamageConfig, ec.hasPreventTerrainDamageConfig());
		if(expectTerrainDamageConfig) assertFalse(ec.isEmptyConfig());
	}
	
	private void checkYield(final EntityConf ec, final float expectYield, final boolean expectYieldConfig) {
		assertEquals((Float) expectYield, (Float) ec.getYield());
		assertEquals(expectYieldConfig, ec.hasYieldConfig());
		if(expectYieldConfig) assertFalse(ec.isEmptyConfig());
	}

	private void checkBoundsAreDefault(final EntityConf ec) {
		assertNull(ec.getActiveBounds().getMaxX());
		assertNull(ec.getActiveBounds().getMaxY());
		assertNull(ec.getActiveBounds().getMaxZ());
		assertNull(ec.getActiveBounds().getMaxX());
		assertNull(ec.getActiveBounds().getMaxY());
		assertNull(ec.getActiveBounds().getMaxZ());
	}
	
	@Test
	public void testToString() {
		assertToStringThrowsNothing(new EntityConf(conf, "someentity", new NullLogger()));
		
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_PLAYER_DAMAGEMULT, 0.23D);
		assertToStringThrowsNothing(new EntityConf(conf, "someentity", new NullLogger()));
		
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_CREATURE_DAMAGEMULT, 0.23D);
		assertToStringThrowsNothing(new EntityConf(conf, "someentity", new NullLogger()));
		
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_ITEM_DAMAGEMULT, 0.23D);
		assertToStringThrowsNothing(new EntityConf(conf, "someentity", new NullLogger()));
		
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_TNT_FUSEMULT, 0.23D);
		assertToStringThrowsNothing(new EntityConf(conf, "someentity", new NullLogger()));
		
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_RADIUSMULT, 0.23D);
		assertToStringThrowsNothing(new EntityConf(conf, "someentity", new NullLogger()));
		
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_FIRE, true);
		assertToStringThrowsNothing(new EntityConf(conf, "someentity", new NullLogger()));
		
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_YIELD, 0.23D);
		assertToStringThrowsNothing(new EntityConf(conf, "someentity", new NullLogger()));
		
		conf.setProperty("someentity." + HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE, true);
		assertToStringThrowsNothing(new EntityConf(conf, "someentity", new NullLogger()));
	}
	
	private void assertToStringThrowsNothing(final EntityConf ec) {
		try {
			ec.toString();
		} catch (final Throwable t) {
			fail("toString() threw " + t);
		}
	}
}
