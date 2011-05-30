package toadmess.explosives.messymocks;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

public class MockBlock implements Block {

	@Override
	public Biome getBiome() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getBlockPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBlockPower(BlockFace face) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Chunk getChunk() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getData() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Block getFace(BlockFace face) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockFace getFace(Block block) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Block getFace(BlockFace face, int distance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getLightLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Block getRelative(BlockFace face) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Block getRelative(int modX, int modY, int modZ) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Material getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTypeId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public World getWorld() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isBlockFaceIndirectlyPowered(BlockFace face) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBlockFacePowered(BlockFace face) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBlockIndirectlyPowered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBlockPowered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setData(byte data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setData(byte data, boolean applyPhyiscs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setType(Material type) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setTypeId(int type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setTypeId(int type, boolean applyPhysics) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setTypeIdAndData(int type, byte data, boolean applyPhyiscs) {
		// TODO Auto-generated method stub
		return false;
	}

}
