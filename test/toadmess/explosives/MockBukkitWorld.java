package toadmess.explosives;

import java.util.List;

import org.bukkit.BlockChangeDelegate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class MockBukkitWorld implements World {
	private String worldName;

	public MockBukkitWorld(String string) {
		worldName = string;
	}

	@Override
	public String getName() { return worldName; }
	
	@Override
	public Item dropItem(Location arg0, ItemStack arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item dropItemNaturally(Location arg0, ItemStack arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean generateTree(Location arg0, TreeType arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean generateTree(Location arg0, TreeType arg1,
			BlockChangeDelegate arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Block getBlockAt(Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Block getBlockAt(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getBlockTypeIdAt(Location arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBlockTypeIdAt(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Chunk getChunkAt(Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Chunk getChunkAt(Block arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Chunk getChunkAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Entity> getEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Environment getEnvironment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getFullTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHighestBlockYAt(Location arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHighestBlockYAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<LivingEntity> getLivingEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Chunk[] getLoadedChunks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Location getSpawnLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isChunkLoaded(Chunk arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChunkLoaded(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void loadChunk(Chunk arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadChunk(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean loadChunk(int arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFullTime(long arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTime(long arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Arrow spawnArrow(Location arg0, Vector arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boat spawnBoat(Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Creature spawnCreature(Location arg0, CreatureType arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Minecart spawnMinecart(Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PoweredMinecart spawnPoweredMinecart(Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageMinecart spawnStorageMinecart(Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean unloadChunk(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unloadChunk(int arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unloadChunk(int arg0, int arg1, boolean arg2, boolean arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unloadChunkRequest(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unloadChunkRequest(int arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Player> getPlayers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setSpawnLocation(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean refreshChunk(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean regenerateChunk(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
