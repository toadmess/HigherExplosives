package toadmess.explosives;
import static toadmess.explosives.config.ConfProps.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import toadmess.explosives.config.ConfProps;
import toadmess.explosives.config.EntityConf;
import toadmess.explosives.config.MultiWorldConfStore;
import toadmess.explosives.events.handlers.EventRouter;

public class HEMain extends JavaPlugin {		
	/** True if we should print out some debugging of the configuration */
	public boolean IS_DEBUG_CONF;
	
	protected Logger log;
	
	private final List<BukkitListeners> entityListeners = new ArrayList<BukkitListeners>();
	
	@Override
	public void onEnable() {
		this.log = getServer().getLogger();
		
		final PluginManager pm = getServer().getPluginManager();
		
		Gatekeeper.onEnable(pm, this.log);
		
		configureWorkarounds(pm);
		
		IS_DEBUG_CONF = this.getConfiguration().getBoolean(CONF_DEBUGCONFIG.toString(), false);
		
		final MultiWorldConfStore confStore = new MultiWorldConfStore(this.log);
		confStore.readConfsForEntity(Creeper.class, this.getConfiguration());
		confStore.readConfsForEntity(TNTPrimed.class, this.getConfiguration());
		confStore.readConfsForEntity(Fireball.class, this.getConfiguration());
		
		final EventRouter router = new EventRouter(this.log);
		confStore.addNeededHandlers(this, router);
		
		final BukkitListeners el = new BukkitListeners(this, router, confStore);
		el.registerNeededEvents(pm, this);
		
		this.log.info(pluginDescription() + " primed and ready");
	}

	@Override
	public void onDisable() {
		this.entityListeners.clear();
		
		this.log.info(pluginDescription() + " defused and disabled");
	}
	
	private void configureWorkarounds(final PluginManager pm) {
		if(null != pm.getPlugin("Mining TNT") || null != pm.getPlugin("MiningTNT")) {
			this.log.info(pluginDescription() + " detected MiningTNT. Default yield value will be 1.0 instead of 0.3 (unless a value is given in config.yml)");
			EntityConf.hasConflictWithMiningTNT = true;
		} else {
			EntityConf.hasConflictWithMiningTNT = false;
		}
	}
	
	private String pluginDescription() {
		final PluginDescriptionFile desc = getDescription();
		return desc.getName() + " " + desc.getVersion();
	}
	
	@Override
	public Configuration getConfiguration() {
		final Configuration conf = super.getConfiguration();
		final String pluginVersion = getDescription().getVersion();
		final String configVersion = conf.getString(CONF_VERSION.toString(), "");
		
		if(null == conf || "".equals(configVersion)) {
			this.log.info(pluginDescription() + " found no configuration file. Creating a fresh default one.");
			
			conf.setProperty(CONF_VERSION.toString(), pluginVersion);

			conf.setProperty(CONF_ENTITIES + ".TNTPrimed." + CONF_ENTITY_RADIUSMULT, 2.0f);
			conf.setProperty(CONF_ENTITIES + ".TNTPrimed." + CONF_ENTITY_YIELD, 0.15f);
			
			conf.save();
		} else if(!configVersion.equalsIgnoreCase(pluginVersion)) {
			this.log.warning(pluginDescription() + ": Found a configuration file for a different version ("+configVersion+"). Going to try using it anyway.");
		}

		return conf;
	}	
}
