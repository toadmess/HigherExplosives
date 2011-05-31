package toadmess.explosives;

import java.util.Random;

import net.minecraft.server.EntityTNTPrimed;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftTNTPrimed;
import org.bukkit.entity.TNTPrimed;

/**
 * Contains all kinds of unsafe pokings of the minecraft server's internals. Here be dragons.
 */
public class MCNative {
	private static final Random rng = new Random(); 
	
	private static boolean failedSoundExplosion = false;
	public static void playSoundExplosion(final Location epicentre) {
		if(failedSoundExplosion) {
			return;
		}
		
		try {
			final WorldServer mcWorld = ((CraftWorld)epicentre.getWorld()).getHandle();
			mcWorld.a(null, epicentre.getX(), epicentre.getY(), epicentre.getZ(), 0);
		} catch(final Throwable t) {
			System.err.println("HigherExplosives: Failed to play explosion sound (minecraft been updated?). Disabling any further direct explosion sounds.");
			failedSoundExplosion = true;
		}
	}
	
	private static boolean failedTNTFuseDuration = false;
	public static void multiplyTNTFuseDuration(final TNTPrimed primed, final float tickMultiplier) {
		if(failedTNTFuseDuration) {
			return;
		}
		
		try {
			final EntityTNTPrimed mcTNTPrimed = (EntityTNTPrimed) ((CraftTNTPrimed) primed).getHandle();
			final int currentFuseTicksLeft = mcTNTPrimed.a;
			final int newFuseTicksLeft = (int) Math.max(0, currentFuseTicksLeft * tickMultiplier);
			mcTNTPrimed.a = newFuseTicksLeft;
		} catch(final Throwable t) {
			System.err.println("HigherExplosives: Failed to set TNT fuse duration (minecraft been updated?). Disabling any further attempts.");
			failedTNTFuseDuration = true;
		}
	}

	private static boolean failedHighestBlockId = false;
	public static int getHighestBlockId() {
		if(!failedHighestBlockId) {
			try {
				return net.minecraft.server.Block.byId.length - 1;
			} catch(final Throwable t) {
				System.err.println("HigherExplosives: Failed to get the highest block type ID from the minecraft code base (minecraft been updated?). Disabling any further attempts and will assume there are up to 255 different block types.");
				failedHighestBlockId = true;
			}
		}
		return 255;
	}
	
	private static boolean failedBlockDrop = false;
	public static void dropBlockItem(final org.bukkit.block.Block bukkitBlock, final float yield) {
		if(failedBlockDrop) {
			return;
		}
		
		try {
			final net.minecraft.server.Block mcBlock = net.minecraft.server.Block.byId[bukkitBlock.getTypeId()];
			final net.minecraft.server.World world = ((CraftWorld) bukkitBlock.getWorld()).getHandle();
			
			mcBlock.dropNaturally(world, bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ(), bukkitBlock.getData(), yield);
			
			return;
		} catch(final Throwable t) {
			System.err.println("HigherExplosives: Failed to drop a block's item (minecraft been updated?). Disabling any further attempts.");
			failedBlockDrop = true;
			return;
		}
	}
}
