package toadmess.explosives;

import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;

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
			System.err.println("HigherExplosives: Failed to play explosion sound (minecraft been updated?). Disabling explosion sounds.");
		}
	}
}
