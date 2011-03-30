package toadmess.explosives;

import junit.framework.Assert;
import net.minecraft.server.EntityFish;
import net.minecraft.server.EntityTNTPrimed;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftFish;
import org.bukkit.craftbukkit.entity.CraftTNTPrimed;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.config.Configuration;
import org.junit.Before;
import org.junit.Test;

public class ExplodedListenerTest {
	private static final double acceptableDelta = 0.0000001;
	
	private Configuration conf;
	
	private Entity entityTNTPrimed;
	private Entity entityFish;
	
	private World worldDefault;
	private World worldNether;

	@Before
	public void setup() {
		conf = new Configuration(null);
		
		entityTNTPrimed = new CraftTNTPrimed(null, new EntityTNTPrimed(null));
		entityFish = new CraftFish(null, new EntityFish(null));
		
		worldDefault = new MockBukkitWorld("world");
		worldNether = new MockBukkitWorld("netherworld");
	}
	
	@Test
	public void testSingleWorld_withYield() {
		conf.setProperty("everyExplosion.yield", 0.23D);
		
		final ExplodedListener el = new ExplodedListener(conf);
		
		Assert.assertEquals(0.23D, explodeSomething(worldDefault, el).getYield(), acceptableDelta);
	}

	@Test
	public void testSingleWorld_withoutYield() {
		final ExplodedListener el = new ExplodedListener(conf);
		
		Assert.assertEquals(0.3D, explodeSomething(worldDefault, el).getYield(), acceptableDelta);
	}

	@Test
	public void testSingleWorld_withinBounds() {
		conf.setProperty("everyExplosion.yield", 0.23D);
		conf.setProperty("everyExplosion.yieldChangeAllowedBounds.max.x", 1);
		
		final ExplodedListener el = new ExplodedListener(conf);
		
		Assert.assertEquals(0.23D, explodeSomething(worldDefault, el).getYield(), acceptableDelta);
	}

	@Test
	public void testSingleWorld_notWithinBounds() {
		conf.setProperty("everyExplosion.yield", 0.23D);
		conf.setProperty("everyExplosion.yieldChangeActiveBounds.max.x", -1);
		
		final ExplodedListener el = new ExplodedListener(conf);
		
		Assert.assertEquals(0.3D, explodeSomething(worldDefault, el).getYield(), acceptableDelta);
	}
	
	@Test
	public void testMultiWorld_withoutWorldConfig() {
		conf.setProperty("everyExplosion.yield", 0.23D);

		final ExplodedListener el = new ExplodedListener(conf);
		
		Assert.assertEquals(0.23D, explodeSomething(worldNether, el).getYield(), acceptableDelta);
	}
	
	@Test
	public void testMultiWorld_withYieldConfig() {
		conf.setProperty("everyExplosion.yield", 0.23D);
		conf.setProperty("worlds.netherworld.everyExplosion.yield", 0.42D);
		
		final ExplodedListener el = new ExplodedListener(conf);
		
		Assert.assertEquals(0.23D, explodeSomething(worldDefault, el).getYield(), acceptableDelta);

		Assert.assertEquals(0.42D, explodeSomething(worldNether, el).getYield(), acceptableDelta);
	}

	@Test
	public void testMultiWorld_withinBounds() {
		conf.setProperty("everyExplosion.yield", 0.23D);
		conf.setProperty("everyExplosion.yieldChangeActiveBounds.max.x", 10);
		conf.setProperty("worlds.netherworld.everyExplosion.yield", 0.42D);
		conf.setProperty("worlds.netherworld.everyExplosion.yieldChangeActiveBounds.max.x", 10);
		
		final ExplodedListener el = new ExplodedListener(conf);
		
		Assert.assertEquals(0.23D, explodeSomething(worldDefault, el).getYield(), acceptableDelta);
		Assert.assertEquals(0.42D, explodeSomething(worldNether, el).getYield(), acceptableDelta);
	}

	@Test
	public void testMultiWorld_notWithinBounds() {
		conf.setProperty("everyExplosion.yield", 0.23D);
		conf.setProperty("everyExplosion.yieldChangeActiveBounds.max.x", 10);
		conf.setProperty("worlds.netherworld.everyExplosion.yield", 0.42D);
		conf.setProperty("worlds.netherworld.everyExplosion.yieldChangeActiveBounds.max.x", -10);
		
		final ExplodedListener el = new ExplodedListener(conf);
		
		Assert.assertEquals(0.23D, explodeSomething(worldDefault, el).getYield(), acceptableDelta);
		Assert.assertEquals(0.3D, explodeSomething(worldNether, el).getYield(), acceptableDelta);
	}
		
	@Test
	public void testWrongEntity() {
		final ExplodedListener el = new ExplodedListener(conf);
		
		testItIsLeftUntouched(el, new EntityExplodeEvent(entityFish, new Location(worldDefault, 0,0,0), null));
	}
	
	private void testItIsLeftUntouched(final ExplodedListener el, final EntityExplodeEvent event) {
		final float origYield = event.getYield();
		
		el.onEntityExplode(event);
		
		Assert.assertEquals(origYield, event.getYield());
	}
	
	private EntityExplodeEvent explodeSomething(final World inThisWorld, final ExplodedListener listener) {
		final EntityExplodeEvent ev = new EntityExplodeEvent(entityTNTPrimed, new Location(inThisWorld, 0,0,0), null);
		listener.onEntityExplode(ev);
		return ev;
	}
}
