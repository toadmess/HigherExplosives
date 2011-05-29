package toadmess.explosives.messymocks;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

/**
 * Massively messy mock Entity
 */
public class MockEntity implements Entity {
	final net.minecraft.server.Entity minecraftEntity;
	
	private World world;
	private Location loc;
	
	public MockEntity(final net.minecraft.server.Entity minecraftEntity) {
		this.minecraftEntity = minecraftEntity;
	}

	public void setLocation(final Location toThisLoc) { this.loc = toThisLoc; }
	
	public void setWorld(final World toThisWorld) { this.world = toThisWorld; }
		
	public static class MockCreeperEntity extends MockEntity implements Creeper {
		public MockCreeperEntity(net.minecraft.server.Entity minecraftEntity) {
			super(minecraftEntity);
		}

		@Override
		public LivingEntity getTarget() {
			return null;
		}

		@Override
		public void setTarget(LivingEntity arg0) {
		}

		@Override
		public double getEyeHeight() {
			return 0;
		}

		@Override
		public double getEyeHeight(boolean arg0) {
			return 0;
		}

		@Override
		public int getHealth() {
			return 0;
		}

		@Override
		public List<Block> getLastTwoTargetBlocks(HashSet<Byte> arg0, int arg1) {
			return null;
		}

		@Override
		public List<Block> getLineOfSight(HashSet<Byte> arg0, int arg1) {
			return null;
		}

		@Override
		public int getMaximumAir() {
			return 0;
		}

		@Override
		public int getRemainingAir() {
			return 0;
		}

		@Override
		public Block getTargetBlock(HashSet<Byte> arg0, int arg1) {
			return null;
		}

		@Override
		public Vehicle getVehicle() {
			return null;
		}

		@Override
		public boolean isInsideVehicle() {
			return false;
		}

		@Override
		public boolean leaveVehicle() {
			return false;
		}

		@Override
		public void setHealth(int arg0) {
		}

		@Override
		public void setMaximumAir(int arg0) {
		}

		@Override
		public void setRemainingAir(int arg0) {
		}

		@Override
		public Arrow shootArrow() {
			return null;
		}

		@Override
		public Egg throwEgg() {
			return null;
		}

		@Override
		public Snowball throwSnowball() {
			return null;
		}

		@Override
		public void damage(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void damage(int arg0, Entity arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Location getEyeLocation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getLastDamage() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getMaximumNoDamageTicks() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getNoDamageTicks() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setLastDamage(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setMaximumNoDamageTicks(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setNoDamageTicks(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean eject() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Entity getPassenger() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean setPassenger(Entity arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean teleport(Location arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean teleport(Entity arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isPowered() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setPowered(boolean arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static class MockTNTPrimedEntity extends MockEntity implements TNTPrimed {
		public MockTNTPrimedEntity(net.minecraft.server.Entity minecraftEntity) {
			super(minecraftEntity);
		}
	}
	
	public static class MockFireballEntity extends MockEntity implements Fireball {
		public MockFireballEntity(net.minecraft.server.Entity minecraftEntity) {
			super(minecraftEntity);
		}
	}
	
	public static class MockChickenEntity extends MockEntity implements Chicken {
		public MockChickenEntity(net.minecraft.server.Entity minecraftEntity) {
			super(minecraftEntity);
		}

		@Override
		public LivingEntity getTarget() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setTarget(LivingEntity arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void damage(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void damage(int arg0, Entity arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public double getEyeHeight() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getEyeHeight(boolean arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Location getEyeLocation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getHealth() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getLastDamage() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public List<Block> getLastTwoTargetBlocks(HashSet<Byte> arg0, int arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Block> getLineOfSight(HashSet<Byte> arg0, int arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getMaximumAir() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getMaximumNoDamageTicks() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getNoDamageTicks() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getRemainingAir() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Block getTargetBlock(HashSet<Byte> arg0, int arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Vehicle getVehicle() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isInsideVehicle() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean leaveVehicle() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setHealth(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setLastDamage(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setMaximumAir(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setMaximumNoDamageTicks(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setNoDamageTicks(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setRemainingAir(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Arrow shootArrow() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Egg throwEgg() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Snowball throwSnowball() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	@Override
	public Vector getVelocity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVelocity(Vector arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean eject() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Entity getPassenger() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setPassenger(Entity arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean teleport(Location arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean teleport(Entity arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getFallDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDead() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFallDistance(float arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int getEntityId() { return 0; }

	@Override
	public int getFireTicks() { return 0; }
	
	@Override
	public Location getLocation() { return this.loc; }

	@Override
	public int getMaxFireTicks() { return 0; }

	@Override
	public Server getServer() { return null; }
	
	@Override
	public World getWorld() { return this.world; }

	@Override
	public void remove() {}

	@Override
	public void setFireTicks(int arg0) {}

	@Override
	public void teleportTo(Location arg0) {}

	@Override
	public void teleportTo(Entity arg0) {}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLastDamageCause(EntityDamageEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
