package toadmess.explosives.events;

import static org.junit.Assert.assertEquals;
import net.minecraft.server.EntityChicken;
import net.minecraft.server.EntityCreeper;
import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityTNTPrimed;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.junit.Before;
import org.junit.Test;

import toadmess.explosives.messymocks.MockBlock;
import toadmess.explosives.messymocks.MockEntity.MockChickenEntity;
import toadmess.explosives.messymocks.MockEntity.MockCreeperEntity;
import toadmess.explosives.messymocks.MockEntity.MockFireballEntity;
import toadmess.explosives.messymocks.MockEntity.MockTNTPrimedEntity;

public class HEEventTest {
	private Creeper someCreeper;
	private TNTPrimed someTNT;
	private Fireball someFireball;
	
	@Before
	public void setup() {
		someCreeper = new MockCreeperEntity(new EntityCreeper(null));
		someTNT = new MockTNTPrimedEntity(new EntityTNTPrimed(null));
		someFireball = new MockFireballEntity(new EntityFireball(null));
	}
	
	@Test
	public void test_getConfigEntityClass_Creeper() {
		final HEEvent ev = new HEEvent(TippingPoint.CAN_CHANGE_EXPLOSION_RADIUS, new EntityEvent(Event.Type.EXPLOSION_PRIME, someCreeper), null);
		assertEquals(Creeper.class, ev.getConfigEntityClass());
	}
	
	@Test
	public void test_getConfigEntityClass_TNTPrimed() {
		final HEEvent ev = new HEEvent(TippingPoint.CAN_CHANGE_EXPLOSION_RADIUS, new EntityEvent(Event.Type.EXPLOSION_PRIME, someTNT), null);
		assertEquals(TNTPrimed.class, ev.getConfigEntityClass());
	}
	
	@Test
	public void test_getConfigEntityClass_Fireball() {
		final HEEvent ev = new HEEvent(TippingPoint.CAN_CHANGE_EXPLOSION_RADIUS, new EntityEvent(Event.Type.EXPLOSION_PRIME, someFireball), null);
		assertEquals(Fireball.class, ev.getConfigEntityClass());
	}
	
	@Test
	public void test_getConfigEntityClass_other_events() {
		final EntityEvent someExplosionEvent = new EntityEvent(Event.Type.EXPLOSION_PRIME, someCreeper);
		final EntityDamageByEntityEvent someDmgEventWithCreeperAsDamager = new EntityDamageByEntityEvent(someCreeper, new MockChickenEntity(new EntityChicken(null)), null, 1);
		
		
		
		final HEEvent[] events = new HEEvent[] {
			new HEEvent(TippingPoint.CAN_CHANGE_EXPLOSION_FIRE_FLAG, someExplosionEvent, null),
			new HEEvent(TippingPoint.CAN_CHANGE_EXPLOSION_YIELD, someExplosionEvent, null),
			new HEEvent(TippingPoint.CAN_CHANGE_EXPLOSION_RADIUS, someExplosionEvent, null),
			new HEEvent(TippingPoint.CAN_PREVENT_TERRAIN_DAMAGE, someExplosionEvent, null),
			new HEEvent(TippingPoint.CAN_CHANGE_PLAYER_DAMAGE, someDmgEventWithCreeperAsDamager, null),
			new HEEvent(TippingPoint.CAN_CHANGE_CREATURE_DAMAGE, someDmgEventWithCreeperAsDamager, null),
			new HEEvent(TippingPoint.CAN_CHANGE_ITEM_DAMAGE, someDmgEventWithCreeperAsDamager, null),
			
			new HEEvent(TippingPoint.TNT_PRIMED_BY_EXPLOSION, null, null),
			new HEEvent(TippingPoint.TNT_PRIMED_BY_FIRE, new BlockBurnEvent(new MockBlock()), null),
			new HEEvent(TippingPoint.TNT_PRIMED_BY_PLAYER, new BlockDamageEvent(null, new MockBlock(), null, true), null),
			new HEEvent(TippingPoint.TNT_PRIMED_BY_REDSTONE, new BlockPhysicsEvent(new MockBlock(), 0), null)
		};
		
		// Just check we haven't missed a particular event type from this test
		assertEquals(events.length, TippingPoint.values().length);

		for(final HEEvent event : events) {
			if(event.type.name().startsWith("TNT_PRIMED")) {
				// TNT_PRIMED tipping point events are always related to TNTPrimed entity configurations.
				assertEquals(TNTPrimed.class, event.getConfigEntityClass());
			} else {
				// All other's should use the creeper config
				assertEquals(Creeper.class, event.getConfigEntityClass());
			}
		}
	}
}
