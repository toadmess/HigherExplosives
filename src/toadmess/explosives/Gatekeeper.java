package toadmess.explosives;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Gatekeeper {
	private static PermissionHandler permissionsHandler = null;
	private static Logger log = null;
	
	public static void onEnable(final PluginManager pm, final Logger logger) {
		Gatekeeper.log = logger;
		
		try {
			final Plugin p = pm.getPlugin("Permissions");
			
			if(p != null) {
				Gatekeeper.permissionsHandler = ((Permissions) p).getHandler();
			}
		
			if(Gatekeeper.permissionsHandler != null) {
				log.info("HigherExplosives: Using Permissions plugin that looks like version " + p.getDescription().getVersion());
			} else {
				log.info("HigherExplosives: Not using Permissions.");
			}
		} catch (final Throwable t) {
			log.info("HigherExplosives: Couldn't catch hold of the Permissions plugin (because of "+ t.getClass().getName() +")");
			Gatekeeper.permissionsHandler = null;
		}
	}
	
	public static boolean hasPermission(final Player p, final String permission) {
		if(Gatekeeper.permissionsHandler == null) {
			return false;
		}
		
		return Gatekeeper.permissionsHandler.has(p, permission);
	}
	
	public static boolean inGroup(final Player p, final String group) {
		if(Gatekeeper.permissionsHandler == null) {
			return false;
		}
		
		return Gatekeeper.permissionsHandler.inGroup(p.getLocation().getWorld().getName(), p.getName(), group);
	}
}