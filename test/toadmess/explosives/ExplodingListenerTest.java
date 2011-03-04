package toadmess.explosives;

import junit.framework.Assert;
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
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.ExplosionPrimedEvent;
import org.bukkit.util.config.Configuration;
import org.junit.Before;
import org.junit.Test;

public class ExplodingListenerTest {
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

	private ExplodingListener creeperListener;
	private ExplodingListener tntListener;
	private ExplodingListener fireballListener;
	private ExplodingListener netherCreeperListener;
	private ExplodingListener netherTntListener;
	private ExplodingListener netherFireballListener;
	
	@Before
	public void setup() {
		conf = new Configuration(null);
		
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
	}
	
	private void initListeners() {
		creeperListener = new ExplodingListener(conf, Creeper.class);
		tntListener = new ExplodingListener(conf, TNTPrimed.class);
		fireballListener = new ExplodingListener(conf, Fireball.class);
		
		netherCreeperListener = new ExplodingListener(conf, Creeper.class);
		netherTntListener = new ExplodingListener(conf, TNTPrimed.class);
		netherFireballListener = new ExplodingListener(conf, Fireball.class);
	}
	
	@Test
	public void testSingleWorld_defaults() {
		initListeners();

		ExplosionPrimedEvent ev = prime(entityCreeperInDefWorld, 0.23F, true, creeperListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());

		ev = prime(entityTNTPrimedInDefWorld, 0.23F, true, tntListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());

		ev = prime(entityFireballInDefWorld, 0.23F, true, fireballListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());
	}

	private ExplosionPrimedEvent prime(final Entity e, final float radius, final boolean fire, final ExplodingListener l) {
		final ExplosionPrimedEvent ev = new ExplosionPrimedEvent(Type.EXPLOSION_PRIMED, e, radius, fire);
		l.onExplosionPrimed(ev);
		return ev;
	}

	@Test
	public void testSingleWorld_withinBounds() {
		conf.setProperty("entities.Creeper.radiusMultiplier", 2.0);
		conf.setProperty("entities.Creeper.activeBounds.max.x", 1);

		conf.setProperty("entities.TNTPrimed.fire", true);

		initListeners();
		
		ExplosionPrimedEvent ev = prime(entityCreeperInDefWorld, 0.23F, true, creeperListener);
		Assert.assertEquals(0.46D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());

		ev = prime(entityTNTPrimedInDefWorld, 0.23F, true, tntListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(true, ev.getFire());
		
		ev = prime(entityFireballInDefWorld, 0.23F, true, fireballListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());
	}

	@Test
	public void testSingleWorld_notWithinBounds() {
		conf.setProperty("entities.Creeper.radiusMultiplier", 2.0);
		conf.setProperty("entities.Creeper.fire", true);
		conf.setProperty("entities.Creeper.activeBounds.max.x", -1);

		initListeners();
		
		ExplosionPrimedEvent ev = prime(entityCreeperInDefWorld, 0.23F, true, creeperListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(true, ev.getFire());

		ev = prime(entityTNTPrimedInDefWorld, 0.23F, true, tntListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());
		
		ev = prime(entityFireballInDefWorld, 0.23F, true, fireballListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());
	}
		
	@Test
	public void testMultiWorld_withinBounds() {
		conf.setProperty("entities.Creeper.radiusMultiplier", 2.0);
		conf.setProperty("entities.Creeper.fire", true);
		conf.setProperty("entities.Creeper.activeBounds.max.x", -1);
		
		conf.setProperty("worlds.netherworld.entities.Creeper.radiusMultiplier", 0.5D);
		conf.setProperty("worlds.netherworld.entities.Creeper.activeBounds.max.x", 1);
		conf.setProperty("entities.Creeper.fire", false);

		initListeners();
		
		ExplosionPrimedEvent ev = prime(entityCreeperInDefWorld, 0.23F, true, creeperListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(true, ev.getFire());

		ev = prime(entityTNTPrimedInDefWorld, 0.23F, true, tntListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());
		
		ev = prime(entityFireballInDefWorld, 0.23F, true, fireballListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());

		
		ev = prime(entityCreeperInNetherWorld, 0.23F, true, netherCreeperListener);
		Assert.assertEquals(0.115D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());

		ev = prime(entityTNTPrimedInNetherWorld, 0.23F, true, netherTntListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());
		
		ev = prime(entityFireballInNetherWorld, 0.23F, true, netherFireballListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());
	}

	@Test
	public void testMultiWorld_notWithinBounds() {
		conf.setProperty("entities.Creeper.radiusMultiplier", 2.0);
		conf.setProperty("entities.Creeper.fire", true);
		conf.setProperty("entities.Creeper.activeBounds.max.x", 1);
		
		conf.setProperty("worlds.netherworld.entities.Creeper.radiusMultiplier", 0.5D);
		conf.setProperty("worlds.netherworld.entities.Creeper.activeBounds.max.x", -1);
		conf.setProperty("worlds.netherworld.entities.Creeper.fire", false);

		initListeners();
		
		ExplosionPrimedEvent ev = prime(entityCreeperInDefWorld, 0.23F, true, creeperListener);
		Assert.assertEquals(0.46D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(true, ev.getFire());

		ev = prime(entityTNTPrimedInDefWorld, 0.23F, true, tntListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());
		
		ev = prime(entityFireballInDefWorld, 0.23F, true, fireballListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());

		
		ev = prime(entityCreeperInNetherWorld, 0.23F, true, netherCreeperListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(true, ev.getFire());

		ev = prime(entityTNTPrimedInNetherWorld, 0.23F, true, netherTntListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());
		
		ev = prime(entityFireballInNetherWorld, 0.23F, true, netherFireballListener);
		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(false, ev.getFire());
	}

	@Test
	public void testWrongEntity() {
		creeperListener = new ExplodingListener(conf, Creeper.class);
		
		final ExplosionPrimedEvent ev = new ExplosionPrimedEvent(Type.ENTITY_COMBUST, entityFishInDefWorld, 0.23F, true);
		
		creeperListener.onExplosionPrimed(ev);

		Assert.assertEquals(0.23D, ev.getRadius(), acceptableDelta);
		Assert.assertEquals(true, ev.getFire());
	}
}
