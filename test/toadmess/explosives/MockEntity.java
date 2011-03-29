package toadmess.explosives;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

public class MockEntity implements Entity {
	final net.minecraft.server.Entity minecraftEntity;
	
	private World world;
	private Location loc;
	
	public MockEntity(final net.minecraft.server.Entity minecraftEntity) {
		this.minecraftEntity = minecraftEntity;
	}
	
	@Override
	public int getEntityId() { return 0; }

	@Override
	public int getFireTicks() { return 0; }

	public void setLocation(final Location toThisLoc) { this.loc = toThisLoc; }
	
	@Override
	public Location getLocation() { return this.loc; }

	@Override
	public int getMaxFireTicks() { return 0; }

	@Override
	public Server getServer() { return null; }

	public void setWorld(final World toThisWorld) { this.world = toThisWorld; }
	
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

	@Override
	public Vector getVelocity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVelocity(Vector arg0) {
		// TODO Auto-generated method stub
		
	}
}
