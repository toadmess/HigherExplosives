package toadmess.explosives;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.server.EntityCreeper;
import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityFish;
import net.minecraft.server.EntityTNTPrimed;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftFish;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.junit.Before;
import org.junit.Test;

import toadmess.explosives.MockEntity.MockTNTPrimedEntity;

public class ExplosionListenerTest {
	private static final double acceptableDelta = 0.0000001;
	
	private Configuration conf;
	
	private MockEntity.MockCreeperEntity entityCreeperInDefWorld;
	private MockEntity.MockTNTPrimedEntity entityTNTPrimedInDefWorld;
	private MockEntity.MockFireballEntity entityFireballInDefWorld;

	private MockEntity.MockCreeperEntity entityCreeperInNetherWorld;
	private MockEntity.MockTNTPrimedEntity entityTNTPrimedInNetherWorld;
	private MockEntity.MockFireballEntity entityFireballInNetherWorld;

	private Entity entityFishInDefWorld;
	
	private World worldDefault;
	private World worldNether;

	private ExplosionListener creeperListener;
	private ExplosionListener tntListener;
	private ExplosionListener fireballListener;
	private ExplosionListener netherCreeperListener;
	private ExplosionListener netherTntListener;
	private ExplosionListener netherFireballListener;

	private ExplosionListener[] allListeners;
	private ExplosionListener[] justDefaultListeners;
	private ExplosionListener[] justNetherListeners;
	
	private void resetConfiguration() {
		conf = new Configuration(null);
		
		// Just set the debug flag to true so we always exercise that code even though it's going to /dev/null.
		conf.setProperty(HEMain.CONF_DEBUGCONFIG, true);
	}
	
	@Before
	public void setup() {
		resetConfiguration();
		
		worldDefault = new MockBukkitWorld("world");
		worldNether = new MockBukkitWorld("netherworld");
		
		entityCreeperInDefWorld = new MockEntity.MockCreeperEntity(new EntityCreeper(null));
		entityTNTPrimedInDefWorld = new MockEntity.MockTNTPrimedEntity(new EntityTNTPrimed(null));
		entityFireballInDefWorld = new MockEntity.MockFireballEntity(new EntityFireball(null));
		
		entityCreeperInNetherWorld = new MockEntity.MockCreeperEntity(new EntityCreeper(null));
		entityTNTPrimedInNetherWorld = new MockEntity.MockTNTPrimedEntity(new EntityTNTPrimed(null));
		entityFireballInNetherWorld = new MockEntity.MockFireballEntity(new EntityFireball(null));
		
		entityFishInDefWorld = new CraftFish(null, new EntityFish(null));
		
		for(final MockEntity e : new MockEntity[]{entityCreeperInDefWorld, entityTNTPrimedInDefWorld, entityFireballInDefWorld}) {
			e.setWorld(worldDefault);
			e.setLocation(new Location(worldDefault, 0D, 0D, 0D));
		}
		
		for(final MockEntity e : new MockEntity[]{entityCreeperInNetherWorld, entityTNTPrimedInNetherWorld, entityFireballInNetherWorld}) {
			e.setWorld(worldNether);
			e.setLocation(new Location(worldNether, 0D, 0D, 0D));
		}
		
		createListeners();
	}

	private void createListeners() {
		creeperListener = new ExplosionListener(conf, new NullLogger(), Creeper.class);
		tntListener = new ExplosionListener(conf, new NullLogger(), TNTPrimed.class);
		fireballListener = new ExplosionListener(conf, new NullLogger(), Fireball.class);
		
		netherCreeperListener = new ExplosionListener(conf, new NullLogger(), Creeper.class);
		netherTntListener = new ExplosionListener(conf, new NullLogger(), TNTPrimed.class);
		netherFireballListener = new ExplosionListener(conf, new NullLogger(), Fireball.class);
		
		justDefaultListeners = new ExplosionListener[] {
			creeperListener, tntListener, fireballListener	
		};
		
		justNetherListeners = new ExplosionListener[] {
			netherCreeperListener, netherTntListener, netherFireballListener				
		};
		
		allListeners = new ExplosionListener[] {
			creeperListener, tntListener, fireballListener,
			netherCreeperListener, netherTntListener, netherFireballListener
		};
	}

	@Test
	public void registerNeededEvents_NoneNeeded() {
		createListeners();
		
		for(final ExplosionListener l : allListeners) {
			assertEquals(0, collectRegisteredListeners(l).size());
		}
	}
	
	@Test
	public void registerNeededEvents_YieldConfig() {
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_YIELD, 0.23F, Type.ENTITY_EXPLODE);
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_YIELD, 0.23D, Type.ENTITY_EXPLODE);
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_YIELD, 0.0F, Type.ENTITY_EXPLODE);
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_YIELD, 1000F, Type.ENTITY_EXPLODE);
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_YIELD, 1, Type.ENTITY_EXPLODE);
	}
	
	@Test
	public void registerNeededEvents_FireConfig() {
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_FIRE, false, Type.EXPLOSION_PRIME);
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_FIRE, true, Type.EXPLOSION_PRIME);
	}
	
	@Test
	public void registerNeededEvents_PreventTerrainDamageConfig() {
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE, false, Type.ENTITY_EXPLODE);
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE, true, Type.ENTITY_EXPLODE);
	}
	
	@Test
	public void registerNeededEvents_RadiusMultiplier() {
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_RADIUSMULT, 0.23F, Type.EXPLOSION_PRIME);
	}
	
	@Test
	public void registerNeededEvents_PlayerDmgMultiplier() {
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_PLAYER_DAMAGEMULT, 0.23F, Type.ENTITY_DAMAGE);
	}
	
	@Test
	public void registerNeededEvents_CreatureDmgMultiplier() {
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_CREATURE_DAMAGEMULT, 0.23F, Type.ENTITY_DAMAGE);
	}
	
	@Test
	public void onEntityExplode_Yield() {
		checkEntityExplode_AllEntities(HEMain.CONF_ENTITY_YIELD, 0.0F, 0.0F, false);
		checkEntityExplode_AllEntities(HEMain.CONF_ENTITY_YIELD, 0.5F, 0.5F, false);
		checkEntityExplode_AllEntities(HEMain.CONF_ENTITY_YIELD, 1.0F, 1.0F, false);
		checkEntityExplode_AllEntities(HEMain.CONF_ENTITY_YIELD, 200, 200F, false);
	}

	@Test
	public void onEntityExplode_PreventTerrainDamage() {
		checkEntityExplode_AllEntities(HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE, false, 0.3F, false);
		checkEntityExplode_AllEntities(HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE, true, 0.3F, true);
	}
	
	@Test
	public void onEntityExplode_BoundsAreChecked() {
		// With no bounds
		createListenersWithEntityConf("Creeper", false, HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE, true);
		checkEntityExplode(entityCreeperInDefWorld, worldDefault, creeperListener, 0.3F, true);
		
		// Setting restrictive bounds (so the explosion event at 0,0,0 will be out of bounds)
		createListenersWithEntityConf("Creeper", false, HEMain.CONF_BOUNDS + "." + HEMain.CONF_BOUNDS_MAX + ".x", -1F);
		checkEntityExplode(entityCreeperInDefWorld, worldDefault, creeperListener, 0.3F, false);
	}
	
	private void checkEntityExplode_AllEntities(final String confKey, final Object confValue, final float expectedYield, final boolean expectedPreventTerrainDamage) {
		checkEntityExplode_AllEntities(false, confKey, confValue, expectedYield, expectedPreventTerrainDamage);	
		checkEntityExplode_AllEntities(true, confKey, confValue, expectedYield, expectedPreventTerrainDamage);	
	}
	
	private void checkEntityExplode_AllEntities(final boolean isConfigForNetherWorld, final String confKey, final Object confValue, final float expectedYield, final boolean expectedPreventTerrainDamage) {
		resetConfiguration();
		createListenersWithEntityConf("TNTPrimed", isConfigForNetherWorld, confKey, confValue);
		// If only the specific nether world has a configuration, expect the default world to be unaffected
		checkEntityExplod_JustOneAffectsExplosion(justDefaultListeners, tntListener, worldDefault, entityTNTPrimedInDefWorld, isConfigForNetherWorld ? 0.3F : expectedYield, isConfigForNetherWorld ? false : expectedPreventTerrainDamage);			
		checkEntityExplod_JustOneAffectsExplosion(justNetherListeners, netherTntListener, worldNether, entityTNTPrimedInNetherWorld, expectedYield, expectedPreventTerrainDamage);
		
		resetConfiguration();
		createListenersWithEntityConf("Creeper", isConfigForNetherWorld, confKey, confValue);
		checkEntityExplod_JustOneAffectsExplosion(justDefaultListeners, creeperListener, worldDefault, entityCreeperInDefWorld, isConfigForNetherWorld ? 0.3F : expectedYield, isConfigForNetherWorld ? false : expectedPreventTerrainDamage);
		checkEntityExplod_JustOneAffectsExplosion(justNetherListeners, netherCreeperListener, worldNether, entityCreeperInNetherWorld, expectedYield, expectedPreventTerrainDamage);
		
		resetConfiguration();
		createListenersWithEntityConf("Fireball", isConfigForNetherWorld, confKey, confValue);
		checkEntityExplod_JustOneAffectsExplosion(justDefaultListeners, fireballListener, worldDefault, entityFireballInDefWorld, isConfigForNetherWorld ? 0.3F : expectedYield, isConfigForNetherWorld ? false : expectedPreventTerrainDamage);
		checkEntityExplod_JustOneAffectsExplosion(justNetherListeners, netherFireballListener, worldNether, entityFireballInNetherWorld, expectedYield, expectedPreventTerrainDamage);
	}
	
	private void checkEntityExplod_JustOneAffectsExplosion(
			final ExplosionListener[] outOfTheseListeners, final ExplosionListener justThisOneShouldAffectIt,
			final World inThisWorld, final Entity entity, 
			final float expectedYield, final boolean expectedPreventTerrainDamage) {
		for (final ExplosionListener l : outOfTheseListeners) {
			if(l == justThisOneShouldAffectIt) {
				// Check that just this one listener affecs the explosion
				checkEntityExplode(entity, inThisWorld, justThisOneShouldAffectIt, expectedYield, expectedPreventTerrainDamage);				
			} else {
				// Check the explosion is unaffected by all other listeners
				checkEntityExplode(entity, inThisWorld, l, 0.3F, false);
			}
		}
	}

	private void checkEntityExplode(final Entity entity, final World world, final ExplosionListener listener, final float expectedYield, final boolean expectedPreventTerrainDamage) {	
		final EntityExplodeEvent ev = new EntityExplodeEvent(entity, new Location(world, 0,0,0), null);
		listener.onEntityExplode(ev);
		assertEquals((Float) expectedYield, (Float) ev.getYield());
		assertEquals(expectedPreventTerrainDamage, ev.isCancelled());
	}
	
	@Test
	public void onExplosionPrime_Fire() {
//		checkExplosionPrime_AllEntities(HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE, false, 0.3F, false);
//		checkExplosionPrime_AllEntities(HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE, true, 0.3F, true);
		fail("TODO");
	}
	
	@Test
	public void onExplosionPrime_Radius() {
//		checkExplosionPrime_AllEntities(HEMain.CONF_ENTITY_YIELD, 0.0F, 0.0F, false);
//		checkExplosionPrime_AllEntities(HEMain.CONF_ENTITY_YIELD, 0.5F, 0.5F, false);
//		checkExplosionPrime_AllEntities(HEMain.CONF_ENTITY_YIELD, 1.0F, 1.0F, false);
//		checkExplosionPrime_AllEntities(HEMain.CONF_ENTITY_YIELD, 200, 200F, false);
		fail("TODO");
	}
	
	@Test
	public void onExplosionPrime_BoundsAreChecked() {		
		// With no bounds
		createListenersWithEntityConf("Creeper", false, HEMain.CONF_ENTITY_FIRE, true);
		checkExplosionPrime(entityCreeperInDefWorld, worldDefault, creeperListener, 0.3F, true);
		
		// Setting restrictive bounds (so the explosion event at 0,0,0 will be out of bounds)
		createListenersWithEntityConf("Creeper", false, HEMain.CONF_BOUNDS + "." + HEMain.CONF_BOUNDS_MAX + ".x", -1F);
		checkExplosionPrime(entityCreeperInDefWorld, worldDefault, creeperListener, 0.3F, false);
	}
	
	private void checkExplosionPrime(final Entity entity, final World world, final ExplosionListener listener, final float expectedRadius, final boolean expectedFire) {	
		final ExplosionPrimeEvent ev = new ExplosionPrimeEvent(entity, 0.3F, false);
		listener.onExplosionPrime(ev);
		assertEquals((Float) expectedRadius, (Float) ev.getRadius());
		assertEquals(expectedFire, ev.getFire());
	}
	
	@Test
	public void onEntityDamage_PlayerDamageMultiplier() {
		fail("TODO");
	}
	
	@Test
	public void onEntityDamage_CreatureDamageMultiplier() {
		fail("TODO");
	}
	
	@Test
	public void onEntityDamage_BoundsAreChecked() {
		fail("TODO");
	}
	
	private void checkAllListenersRegisterEvent(final String confKey, final Object confValue, final Type expectedEventType) {
		resetConfiguration();
		createListenersWithEntityConf("Creeper", false, confKey, confValue);
		checkOneListenerIsRegistered(expectedEventType, creeperListener);

		resetConfiguration();
		createListenersWithEntityConf("Creeper", true, confKey, confValue);
		checkOneListenerIsRegistered(expectedEventType, netherCreeperListener);
		
		resetConfiguration();
		createListenersWithEntityConf("TNTPrimed", false, confKey, confValue);
		checkOneListenerIsRegistered(expectedEventType, tntListener);

		resetConfiguration();
		createListenersWithEntityConf("TNTPrimed", true, confKey, confValue);
		checkOneListenerIsRegistered(expectedEventType, netherTntListener);
		
		resetConfiguration();
		createListenersWithEntityConf("Fireball", false, confKey, confValue);
		checkOneListenerIsRegistered(expectedEventType, fireballListener);
		
		resetConfiguration();
		createListenersWithEntityConf("Fireball", true, confKey, confValue);
		checkOneListenerIsRegistered(expectedEventType, netherFireballListener);
	}

	/**
	 * Sets one entity's config (at the level immediately below ".Entities") to have a single configuration value set. 
	 * Then it creates all ExplosionListeners with the new config.
	 */
	private void createListenersWithEntityConf(final String entityName, final boolean inNetherWorld, final String confKey, final Object confValue) {
		String confProp = "";
		if(inNetherWorld) {
			confProp += HEMain.CONF_WORLDS + "." + worldNether.getName() + ".";
		}
		confProp += HEMain.CONF_ENTITIES + "." + entityName + "." + confKey;
		
		conf.setProperty(confProp, confValue);
		
		createListeners();
	}
	
	private void checkOneListenerIsRegistered(final Event.Type listenerType, final ExplosionListener listener) {		
		final List<Type> listenerTypes = collectRegisteredListeners(listener);
		
		assertEquals(1, listenerTypes.size());
		assertEquals(listenerType, listenerTypes.get(0));
	}
	
	// Calls Listener.registerNeededEvents and returns a list of the event types of the registered listeners.
	private List<Type> collectRegisteredListeners(final ExplosionListener listener) {
		final List<Type> listenerTypes = new ArrayList<Type>();
		
		final StubPlugin stubbedPlugin = new StubPlugin();
		final PluginManager mockPM = new StubPluginManager() {
			@Override
			public void registerEvent(final Type type, final Listener listener, final Priority priority, final Plugin plugin) {
				assertNotNull(listener);
				assertTrue(listener instanceof ExplosionListener);
				assertEquals(plugin, stubbedPlugin);
				
				listenerTypes.add(type);
			};
		};

		listener.registerNeededEvents(mockPM, stubbedPlugin);
		
		return listenerTypes;
	}
	
	private class StubPlugin extends JavaPlugin {
		public void onDisable() {}
		public void onEnable() {}		
	}
}
