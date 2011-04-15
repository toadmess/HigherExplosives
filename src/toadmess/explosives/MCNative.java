package toadmess.explosives;

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
	private static boolean failedSoundExplosion = false;
	protected static void playSoundExplosion(final Location epicentre) {
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
}
