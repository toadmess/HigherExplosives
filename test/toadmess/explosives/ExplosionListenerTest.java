package toadmess.explosives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.EntityChicken;
import net.minecraft.server.EntityCreeper;
import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityTNTPrimed;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.junit.Before;
import org.junit.Test;

import toadmess.explosives.events.handlers.EventRouter;
import toadmess.explosives.messymocks.MockBukkitWorld;
import toadmess.explosives.messymocks.MockEntity;
import toadmess.explosives.messymocks.MockPlugin;
import toadmess.explosives.messymocks.NullLogger;
import toadmess.explosives.messymocks.StubPluginManager;

/**
 * Unmaintainable unit tests. Yuck.
 */
public class ExplosionListenerTest {
	private static final double acceptableDelta = 0.0000001;

	private static final boolean DEF_PREVENT_TERRAIN_DAMAGE = false;
	private static final float DEF_YIELD = 0.3F;
	
	private static final boolean DEF_FIRE = false;
	private static final float DEF_RADIUS = 1.0F;

	private static final float DEF_DAMAGEMULT = 1.0F;
	
	private final EntityExplodeExpectations ENTITY_EXPLODE_DEFAULTS;
	private final ExplosionPrimeExpectations EXPLOSION_PRIME_DEFAULTS;
	private final EntityDamageExpectations ENTITY_DAMAGE_DEFAULTS;

	private MockPlugin plugin;
	private Configuration conf;
	
	private MockEntity.MockCreeperEntity entityCreeperInDefWorld;
	private MockEntity.MockTNTPrimedEntity entityTNTPrimedInDefWorld;
	private MockEntity.MockFireballEntity entityFireballInDefWorld;

	private MockEntity.MockCreeperEntity entityCreeperInNetherWorld;
	private MockEntity.MockTNTPrimedEntity entityTNTPrimedInNetherWorld;
	private MockEntity.MockFireballEntity entityFireballInNetherWorld;

	private MockEntity.MockChickenEntity entityChickenInDefWorld;
	
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
	
	public ExplosionListenerTest() {
		this.ENTITY_EXPLODE_DEFAULTS = new EntityExplodeExpectations(DEF_YIELD, DEF_PREVENT_TERRAIN_DAMAGE);
		this.EXPLOSION_PRIME_DEFAULTS = new ExplosionPrimeExpectations(DEF_RADIUS, DEF_FIRE);
		this.ENTITY_DAMAGE_DEFAULTS = new EntityDamageExpectations((int) (DEF_DAMAGEMULT * EntityDamageChecker.STANDARD_EVENT_DAMAGE));
	}
	
	private void resetConfiguration() {
		conf = new Configuration(null);
		
		// Just set the debug flag to true so we always exercise that code even though it's going to /dev/null.
		conf.setProperty(HEMain.CONF_DEBUGCONFIG, true);
		
		plugin.setConfiguration(conf);
	}
	
	@Before
	public void setup() {
		plugin = new MockPlugin();
		
		resetConfiguration();
		
		worldDefault = new MockBukkitWorld("world");
		worldNether = new MockBukkitWorld("netherworld");
		
		entityCreeperInDefWorld = new MockEntity.MockCreeperEntity(new EntityCreeper(null));
		entityTNTPrimedInDefWorld = new MockEntity.MockTNTPrimedEntity(new EntityTNTPrimed(null));
		entityFireballInDefWorld = new MockEntity.MockFireballEntity(new EntityFireball(null));
		
		entityCreeperInNetherWorld = new MockEntity.MockCreeperEntity(new EntityCreeper(null));
		entityTNTPrimedInNetherWorld = new MockEntity.MockTNTPrimedEntity(new EntityTNTPrimed(null));
		entityFireballInNetherWorld = new MockEntity.MockFireballEntity(new EntityFireball(null));
		
		entityChickenInDefWorld = new MockEntity.MockChickenEntity(new EntityChicken(null));
		
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
		final NullLogger devnull = new NullLogger();
		final EventRouter eventsHandler = new EventRouter(devnull);
		final MultiWorldConfStore confStore = new MultiWorldConfStore();
		eventsHandler.setConfStore(confStore);
		
		creeperListener = new ExplosionListener(plugin, devnull, eventsHandler, confStore, Creeper.class);
		tntListener = new ExplosionListener(plugin, devnull, eventsHandler, confStore, TNTPrimed.class);
		fireballListener = new ExplosionListener(plugin, devnull, eventsHandler, confStore, Fireball.class);
		
		netherCreeperListener = new ExplosionListener(plugin, devnull, eventsHandler, confStore, Creeper.class);
		netherTntListener = new ExplosionListener(plugin, devnull, eventsHandler, confStore, TNTPrimed.class);
		netherFireballListener = new ExplosionListener(plugin, devnull, eventsHandler, confStore, Fireball.class);
		
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
	public void registerNeededEvents_ItemDmgMultiplier() {
		checkAllListenersRegisterEvent(HEMain.CONF_ENTITY_ITEM_DAMAGEMULT, 0.23F, Type.ENTITY_DAMAGE);
	}
	
	@Test
	public void onEntityExplode_Yield() {
		final EntityExplodeChecker ec = new EntityExplodeChecker(ENTITY_EXPLODE_DEFAULTS);
		
		ec.checkAllEntities(HEMain.CONF_ENTITY_YIELD, 0.0F, new EntityExplodeExpectations(0.0F, DEF_PREVENT_TERRAIN_DAMAGE));
		ec.checkAllEntities(HEMain.CONF_ENTITY_YIELD, 0.5F, new EntityExplodeExpectations(0.5F, DEF_PREVENT_TERRAIN_DAMAGE));
		ec.checkAllEntities(HEMain.CONF_ENTITY_YIELD, 1.0F, new EntityExplodeExpectations(1.0F, DEF_PREVENT_TERRAIN_DAMAGE));
		ec.checkAllEntities(HEMain.CONF_ENTITY_YIELD, 200, new EntityExplodeExpectations(200F, DEF_PREVENT_TERRAIN_DAMAGE));
	}

	@Test
	public void onEntityExplode_PreventTerrainDamage() {
		final EntityExplodeChecker ec = new EntityExplodeChecker(ENTITY_EXPLODE_DEFAULTS);
		final String confPath = HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE;
		
		ec.checkAllEntities(confPath, false, new EntityExplodeExpectations(DEF_YIELD, false));
		ec.checkAllEntities(confPath, true, new EntityExplodeExpectations(DEF_YIELD, true));
	}
	
	@Test
	public void onEntityExplode_BoundsAreChecked() {
		final EntityExplodeChecker ec = new EntityExplodeChecker(ENTITY_EXPLODE_DEFAULTS);
		
		final Map<String,Object> config = new HashMap<String,Object>();
		
		// With no bounds, we expect the terrain damage value to be set on whatever entity has it set
		config.put(HEMain.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE, !DEF_PREVENT_TERRAIN_DAMAGE);
		ec.checkAllEntities(config, new EntityExplodeExpectations(DEF_YIELD, !DEF_PREVENT_TERRAIN_DAMAGE));		
		
		// Setting restrictive bounds (so the explosion event at 0,0,0 will be out of bounds)
		// Expect the terrain damage value never to be set because we're out of bounds
		config.put(HEMain.CONF_BOUNDS + "." + HEMain.CONF_BOUNDS_MAX + ".x", -1F);
		ec.checkAllEntities(config, new EntityExplodeExpectations(DEF_YIELD, DEF_PREVENT_TERRAIN_DAMAGE));		
	}
	
	@Test
	public void onExplosionPrime_Fire() {
		final ExplosionPrimeChecker ec = new ExplosionPrimeChecker(EXPLOSION_PRIME_DEFAULTS);
		final String confPath = HEMain.CONF_ENTITY_FIRE;
		
		ec.checkAllEntities(confPath, false, new ExplosionPrimeExpectations(DEF_RADIUS, false));
		ec.checkAllEntities(confPath, true, new ExplosionPrimeExpectations(DEF_RADIUS, true));
	}
	
	@Test
	public void onExplosionPrime_Radius() {
		final ExplosionPrimeChecker ec = new ExplosionPrimeChecker(EXPLOSION_PRIME_DEFAULTS);
		final String confPath = HEMain.CONF_ENTITY_RADIUSMULT;
		
		ec.checkAllEntities(confPath, 0.0F, new ExplosionPrimeExpectations(0.0F, DEF_FIRE));
		ec.checkAllEntities(confPath, 0.5F, new ExplosionPrimeExpectations(0.5F, DEF_FIRE));
		ec.checkAllEntities(confPath, 1.0F, new ExplosionPrimeExpectations(1.0F, DEF_FIRE));
		ec.checkAllEntities(confPath, 200, new ExplosionPrimeExpectations(200F, DEF_FIRE));
	}
	
	@Test
	public void onExplosionPrime_BoundsAreChecked() {
		final ExplosionPrimeChecker ec = new ExplosionPrimeChecker(EXPLOSION_PRIME_DEFAULTS);
		
		final Map<String,Object> config = new HashMap<String,Object>();
		
		// With no bounds, we expect the fire value to be set on whatever entity has it set
		config.put(HEMain.CONF_ENTITY_FIRE, !DEF_FIRE);
		ec.checkAllEntities(config, new ExplosionPrimeExpectations(DEF_RADIUS, !DEF_FIRE));		
		
		// Setting restrictive bounds (so the explosion event at 0,0,0 will be out of bounds)
		// Expect the fire value never to be set because we're out of bounds
		config.put(HEMain.CONF_BOUNDS + "." + HEMain.CONF_BOUNDS_MAX + ".x", -1F);
		ec.checkAllEntities(config, new ExplosionPrimeExpectations(DEF_RADIUS, DEF_FIRE));		
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
	public void onEntityDamage_ItemDamageMultiplier() {
		fail("TODO");
	}
	
	@Test
	public void onBlockPhysics_TNTFuseMultiplier() {
		fail("TODO");
	}
	
	@Test
	public void onBlockDamage_TNTFuseMultiplier() {
		fail("TODO");
	}
	
	@Test
	public void onBlockBurn_TNTFuseMultiplier() {
		fail("TODO");
	}
	
	@Test
	public void onEntityExplode_TNTFuseMultiplier() {
		fail("TODO");
	}
	
	@Test
	public void onEntityDamage_BoundsAreChecked_CreatureDamagee() {
		final EntityDamageChecker ec = new EntityDamageChecker(ENTITY_DAMAGE_DEFAULTS, entityChickenInDefWorld);
		
		final Map<String,Object> config = new HashMap<String,Object>();
		
		// With no bounds, we expect the creature damage to be modified on whatever entity damagee has a multiplier set
		config.put(HEMain.CONF_ENTITY_CREATURE_DAMAGEMULT, 0.5F);
		ec.checkAllEntities(config, new EntityDamageExpectations((int) (0.5F * EntityDamageChecker.STANDARD_EVENT_DAMAGE)));		
		
		// Setting restrictive bounds (so the explosion event at 0,0,0 will be out of bounds)
		// Expect the damage value never to be modified because we're out of bounds
		config.put(HEMain.CONF_BOUNDS + "." + HEMain.CONF_BOUNDS_MAX + ".x", -1F);
		ec.checkAllEntities(config, new EntityDamageExpectations(EntityDamageChecker.STANDARD_EVENT_DAMAGE));		
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
	 * Sets one entity's config (at the level immediately below ".Entities") to have a configuration values set. 
	 * Then it creates all ExplosionListeners with the new config.
	 */
	private void createListenersWithEntityConf(final String entityName, final boolean inNetherWorld, final Map<String,Object> config) {
		for(final String confKey : config.keySet()) {
			String confProp = "";
			if(inNetherWorld) {
				confProp += HEMain.CONF_WORLDS + "." + worldNether.getName() + ".";
			}
			confProp += HEMain.CONF_ENTITIES + "." + entityName + "." + confKey;
			
			conf.setProperty(confProp, config.get(confKey));
		}
		
		createListeners();
	}
	
	private void createListenersWithEntityConf(final String entityName, final boolean inNetherWorld, final String confKey, final Object confValue) {
		final Map<String,Object> config = new HashMap<String,Object>();
		config.put(confKey, confValue);
		
		createListenersWithEntityConf(entityName, inNetherWorld, config);
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
	
	private interface EventExpectations<EventType> {
		public void assertEvent(EventType ev);
	}
	
	private class EntityExplodeExpectations implements EventExpectations<EntityExplodeEvent> {
		public final float expectedYield;
		public final boolean expectedPreventTerrainDamage;
		protected EntityExplodeExpectations(final float expectedYield, final boolean expectedPreventTerrainDamage) {
			this.expectedYield = expectedYield;
			this.expectedPreventTerrainDamage = expectedPreventTerrainDamage;
		}
		@Override
		public void assertEvent(final EntityExplodeEvent ev) {
			assertEquals((Float) this.expectedYield, (Float) ev.getYield());
			assertEquals(this.expectedPreventTerrainDamage, ev.isCancelled());
		}
	}
	
	private class EntityExplodeChecker extends EventChecker<EntityExplodeEvent> {
		public EntityExplodeChecker(final EntityExplodeExpectations defaults) { super(defaults); }

		@Override
		protected EntityExplodeEvent createEvent(final Entity entity, final World world) {
			return new EntityExplodeEvent(entity, new Location(world, 0,0,0), null);
		}
		
		@Override		
		protected void listenToEvent(final ExplosionListener listener, final EntityExplodeEvent ev) {
			listener.getEntityListener().onEntityExplode(ev);
		}
	}

	private class ExplosionPrimeExpectations implements EventExpectations<ExplosionPrimeEvent> {
		public final float expectedRadius;
		public final boolean expectedFire;
		protected ExplosionPrimeExpectations(final float expectedRadius, final boolean expectedFire) {
			this.expectedRadius = expectedRadius;
			this.expectedFire = expectedFire;
		}
		@Override
		public void assertEvent(final ExplosionPrimeEvent ev) {
			assertEquals((Float) this.expectedRadius, (Float) ev.getRadius());
			assertEquals(this.expectedFire, ev.getFire());
		}
	}	
	private class ExplosionPrimeChecker extends EventChecker<ExplosionPrimeEvent> {
		public ExplosionPrimeChecker(final ExplosionPrimeExpectations defaults) { super(defaults); }

		@Override
		protected ExplosionPrimeEvent createEvent(final Entity entity, final World world) {
			return new ExplosionPrimeEvent(entity, DEF_RADIUS, DEF_FIRE);
		}
		
		@Override		
		protected void listenToEvent(final ExplosionListener listener, final ExplosionPrimeEvent ev) {
			listener.getEntityListener().onExplosionPrime(ev);
		}
	}

	private class EntityDamageExpectations implements EventExpectations<EntityDamageByEntityEvent> {
		public final int damage;
		protected EntityDamageExpectations(final int damage) {
			this.damage = damage;
		}
		@Override
		public void assertEvent(final EntityDamageByEntityEvent ev) {
			assertEquals(this.damage, ev.getDamage());
		}
	}
	private class EntityDamageChecker extends EventChecker<EntityDamageByEntityEvent> {
		public static final int STANDARD_EVENT_DAMAGE = 100;
		private final Entity damagee;
		public EntityDamageChecker(final EntityDamageExpectations defaults, final Entity damagee) { 
			super(defaults);
			this.damagee = damagee;
		}

		@Override
		protected EntityDamageByEntityEvent createEvent(final Entity entity, final World world) {
			return new EntityDamageByEntityEvent(entity, this.damagee, DamageCause.ENTITY_EXPLOSION, STANDARD_EVENT_DAMAGE);
		}

		@Override		
		protected void listenToEvent(final ExplosionListener listener, final EntityDamageByEntityEvent ev) {
			listener.getEntityListener().onEntityDamage(ev);
		}
	}
	
	private abstract class EventChecker<EventType> {
		private final EventExpectations<EventType> defaultValue;
		
		public EventChecker(final EventExpectations<EventType> defaults) {
			this.defaultValue = defaults;
		}

		protected void checkAllEntities(final String confKey, final Object confValue, final EventExpectations<EventType> expectedVal) {
			final Map<String,Object> config = new HashMap<String,Object>();
			config.put(confKey, confValue);
			checkAllEntities(config, expectedVal);
		}
		
		protected void checkAllEntities(final Map<String,Object> config, final EventExpectations<EventType> expectedVal) {
			checkAllEntities(false, config, expectedVal);
			checkAllEntities(true, config, expectedVal);	
		}
		
		protected void checkAllEntities(final boolean isConfigForNetherWorld, final Map<String,Object> config, final EventExpectations<EventType> expectedVal) {
			final EventExpectations<EventType> toExpect = isConfigForNetherWorld ? this.defaultValue : expectedVal;
			
			resetConfiguration();
			createListenersWithEntityConf("TNTPrimed", isConfigForNetherWorld, config);
			// If only the specific nether world has a configuration, expect the default world to be unaffected
			checkExplosionIsChangedByJustOneListener(justDefaultListeners, tntListener, worldDefault, entityTNTPrimedInDefWorld, toExpect);			
			checkExplosionIsChangedByJustOneListener(justNetherListeners, netherTntListener, worldNether, entityTNTPrimedInNetherWorld, expectedVal);
			
			resetConfiguration();
			createListenersWithEntityConf("Creeper", isConfigForNetherWorld, config);
			checkExplosionIsChangedByJustOneListener(justDefaultListeners, creeperListener, worldDefault, entityCreeperInDefWorld, toExpect);
			checkExplosionIsChangedByJustOneListener(justNetherListeners, netherCreeperListener, worldNether, entityCreeperInNetherWorld, expectedVal);
			
			resetConfiguration();
			createListenersWithEntityConf("Fireball", isConfigForNetherWorld, config);
			checkExplosionIsChangedByJustOneListener(justDefaultListeners, fireballListener, worldDefault, entityFireballInDefWorld, toExpect);
			checkExplosionIsChangedByJustOneListener(justNetherListeners, netherFireballListener, worldNether, entityFireballInNetherWorld, expectedVal);
		}
		
		protected void checkExplosionIsChangedByJustOneListener (
				final ExplosionListener[] outOfTheseListeners, final ExplosionListener justThisOneShouldAffectIt,
				final World inThisWorld, final Entity entity, 
				final EventExpectations<EventType> expectedValue) {
			for (final ExplosionListener l : outOfTheseListeners) {
				System.out.println("checkExplosionIsChangedByJustOneListener: " + entity + ", " + l + ", " + this.defaultValue);
				if(l == justThisOneShouldAffectIt) {
					// Check that just this one listener affecs the explosion
					checkEventHandledAsExpected(entity, inThisWorld, justThisOneShouldAffectIt, expectedValue);				
				} else {
					// Check the explosion is unaffected by all other listeners
					checkEventHandledAsExpected(entity, inThisWorld, l, this.defaultValue);
				}
			}
		}

		protected void checkEventHandledAsExpected(final Entity entity, final World world, final ExplosionListener listener, final EventExpectations<EventType> expectedValue) {	
			final EventType ev = createEvent(entity, world);
			listenToEvent(listener, ev);
			expectedValue.assertEvent(ev);
		}
		
		protected abstract EventType createEvent(Entity entity, World world);
		protected abstract void listenToEvent(ExplosionListener listener, EventType ev);
	}
}
