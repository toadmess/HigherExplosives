package toadmess.explosives;

import static org.junit.Assert.fail;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BoundsTest {
	private final Location locAllZero = new Location(new MockBukkitWorld(""), 0D, 0D, 0D);
	private final Location locPositive = new Location(new MockBukkitWorld(""), 10D, 10D, 10D);
	private final Location locNegative = new Location(new MockBukkitWorld(""), -10D, -10D, -10D);
	private final Location locFarAway = new Location(new MockBukkitWorld(""), 100D, 0D, 0D);
	
	private Configuration conf;
	
	@Before
	public void setup() {
		conf = new Configuration(null);
	}
	
	@Test
	public void testEmpty() {
		final Bounds b = new Bounds(conf, "");

		Assert.assertNull(b.getMaxX());
		Assert.assertNull(b.getMinX());
		Assert.assertNull(b.getMaxY());
		Assert.assertNull(b.getMinY());
		Assert.assertNull(b.getMaxZ());
		Assert.assertNull(b.getMinZ());
		
		// Any old bounds are allowed
		Assert.assertTrue(b.isWithinBounds(locAllZero));
		Assert.assertTrue(b.isWithinBounds(locPositive));
		Assert.assertTrue(b.isWithinBounds(locNegative));
		Assert.assertTrue(b.isWithinBounds(locFarAway));
		
		checkToString_DoesNotThrowUp(b);
	}
	
	@Test
	public void testConfPath_DefaultBounds() {
		testConfPath(HEMain.CONF_ENTITIES+".Creeper");
	}
	
	private void testConfPath(final String path) {
		conf.setProperty(path+"."+HEMain.CONF_BOUNDS+"."+HEMain.CONF_BOUNDS_MAX+".x", 23D);
		Bounds b = new Bounds(conf, path);
		
		Assert.assertTrue(23D == b.getMaxX());
		Assert.assertNull(b.getMinX());
		Assert.assertNull(b.getMaxY());
		Assert.assertNull(b.getMinY());
		Assert.assertNull(b.getMaxZ());
		Assert.assertNull(b.getMinZ());
		Assert.assertTrue(b.isWithinBounds(new Location(new MockBukkitWorld(""), 22D, 0, 0)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, 24D, 0, 0)));
		checkToString_DoesNotThrowUp(b);
		
		conf.setProperty(path+"."+HEMain.CONF_BOUNDS+"."+HEMain.CONF_BOUNDS_MAX+".y", 24D);
		b = new Bounds(conf, path);

		Assert.assertTrue(23D == b.getMaxX());
		Assert.assertNull(b.getMinX());
		Assert.assertTrue(24D == b.getMaxY());
		Assert.assertNull(b.getMinY());
		Assert.assertNull(b.getMaxZ());
		Assert.assertNull(b.getMinZ());
		Assert.assertTrue(b.isWithinBounds(new Location(null, 0, 23D, 0)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, 0, 25D, 0)));
		checkToString_DoesNotThrowUp(b);
		
		conf.setProperty(path+"."+HEMain.CONF_BOUNDS+"."+HEMain.CONF_BOUNDS_MAX+".z", 25D);
		b = new Bounds(conf, path);
		
		Assert.assertTrue(23D == b.getMaxX());
		Assert.assertNull(b.getMinX());
		Assert.assertTrue(24D == b.getMaxY());
		Assert.assertNull(b.getMinY());
		Assert.assertTrue(25D == b.getMaxZ());
		Assert.assertNull(b.getMinZ());
		Assert.assertTrue(b.isWithinBounds(new Location(null, 0, 0, 24D)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, 0, 0, 26D)));
		checkToString_DoesNotThrowUp(b);
		
		conf.setProperty(path+"."+HEMain.CONF_BOUNDS+"."+HEMain.CONF_BOUNDS_MIN+".x", -26D);
		b = new Bounds(conf, path);
		
		Assert.assertTrue(23D == b.getMaxX());
		Assert.assertTrue(-26D == b.getMinX());
		Assert.assertTrue(24D == b.getMaxY());
		Assert.assertNull(b.getMinY());
		Assert.assertTrue(25D == b.getMaxZ());
		Assert.assertNull(b.getMinZ());
		Assert.assertTrue(b.isWithinBounds(new Location(null, -25D, 23D, 24D)));
		Assert.assertTrue(b.isWithinBounds(new Location(null, -25D, 23D, 24D)));
		Assert.assertTrue(b.isWithinBounds(new Location(null, -25D, -200D, 24D)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, -27D, -200D, 24D)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, -27D, 0, 200D)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, -27D, 0, 0D)));
		checkToString_DoesNotThrowUp(b);
		
		conf.setProperty(path+"."+HEMain.CONF_BOUNDS+"."+HEMain.CONF_BOUNDS_MIN+".y", -27D);
		b = new Bounds(conf, path);
		
		Assert.assertTrue(23D == b.getMaxX());
		Assert.assertTrue(-26.0 == b.getMinX());
		Assert.assertTrue(24D == b.getMaxY());
		Assert.assertTrue(-27D == b.getMinY());
		Assert.assertTrue(25D == b.getMaxZ());
		Assert.assertNull(b.getMinZ());
		Assert.assertTrue(b.isWithinBounds(new Location(null, 0, -26D, 0)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, 0, -28D, 0)));
		checkToString_DoesNotThrowUp(b);
		
		conf.setProperty(path+"."+HEMain.CONF_BOUNDS+"."+HEMain.CONF_BOUNDS_MIN+".z", -28D);
		b = new Bounds(conf, path);
		
		Assert.assertTrue(23D == b.getMaxX());
		Assert.assertTrue(-26D == b.getMinX());
		Assert.assertTrue(24D == b.getMaxY());
		Assert.assertTrue(-27D == b.getMinY());
		Assert.assertTrue(25D == b.getMaxZ());
		Assert.assertTrue(-28D == b.getMinZ());
		Assert.assertTrue(b.isWithinBounds(new Location(null, 0, 0, 0)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, 0, 0, -100)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, 0, 0, 100)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, 0, -100, 0)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, 0, 100, 0)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, -100, 0, 0)));
		Assert.assertFalse(b.isWithinBounds(new Location(null, 100, 0, 0)));
		checkToString_DoesNotThrowUp(b);
	}
	
	public void checkToString_DoesNotThrowUp(final Bounds b) {
		try {
			b.toString();
		} catch(final Throwable t) {
			fail("toString threw " + t);
		}
	}
	
	@Test
	public void checkBounds_NullLocation() {
		final Bounds b = new Bounds(conf, "");
		Assert.assertFalse(b.isWithinBounds(null));
	}
}
