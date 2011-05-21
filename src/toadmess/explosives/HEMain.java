package toadmess.explosives;

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

import toadmess.explosives.events.EventRouter;

public class HEMain extends JavaPlugin {	
	protected static final String CONF_VERSION = "version";
	protected static final String CONF_DEBUGCONFIG = "debugConfig";
	
	protected static final String CONF_ENTITIES = "entities";
	
	protected static final String CONF_ENTITY_FIRE = "fire";
	protected static final String CONF_ENTITY_RADIUSMULT = "radiusMultiplier";
	protected static final String CONF_ENTITY_PLAYER_DAMAGEMULT = "playerDamageMultiplier";
	protected static final String CONF_ENTITY_CREATURE_DAMAGEMULT = "creatureDamageMultiplier";
	protected static final String CONF_ENTITY_ITEM_DAMAGEMULT = "itemDamageMultiplier";
	protected static final String CONF_ENTITY_YIELD = "yield";
	protected static final String CONF_ENTITY_PREVENT_TERRAIN_DAMAGE = "preventTerrainDamage";
	protected static final String CONF_ENTITY_TNT_FUSEMULT = "trialTNTFuseMultiplier";
	
	protected static final String CONF_MULTIPLIER_CHANCE = "chance";
	protected static final String CONF_MULTIPLIER_VALUE = "value";
	
	protected static final String CONF_BOUNDS = "activeBounds";
	protected static final String CONF_BOUNDS_MAX = "max";
	protected static final String CONF_BOUNDS_MIN = "min";

	protected static final String CONF_WORLDS = "worlds";
	
	/** True if we should print out some debugging of the configuration */
	public boolean IS_DEBUG_CONF;
	
	protected Logger log;
	
	private final List<ExplosionListener> entityListeners = new ArrayList<ExplosionListener>();
	
	@Override
	public void onEnable() {
		this.log = getServer().getLogger();
		
		final PluginManager pm = getServer().getPluginManager();
		
		configureWorkarounds(pm);
		
		IS_DEBUG_CONF = this.getConfiguration().getBoolean(HEMain.CONF_DEBUGCONFIG, false);
		
		final MultiWorldConfStore confStore = new MultiWorldConfStore();
		
		final EventRouter defHandler = new EventRouter(this.log);
		defHandler.setConfStore(confStore);
		
		this.entityListeners.add(new ExplosionListener(this, this.log, defHandler, confStore, TNTPrimed.class));
		this.entityListeners.add(new ExplosionListener(this, this.log, defHandler, confStore, Creeper.class));
		this.entityListeners.add(new ExplosionListener(this, this.log, defHandler, confStore, Fireball.class));
		
		registerNeededEvents(pm);
		
		this.log.info(pluginDescription() + " primed and ready");
	}

	private void registerNeededEvents(final PluginManager pm) {
		for(final ExplosionListener listener : this.entityListeners) {
			listener.registerNeededEvents(pm, this);
		}
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
		final String configVersion = conf.getString(CONF_VERSION, "");
		
		if(null == conf || "".equals(configVersion)) {
			this.log.info(pluginDescription() + " found no configuration file. Creating a fresh default one.");
			
			conf.setProperty(CONF_VERSION, pluginVersion);

			conf.setProperty(CONF_ENTITIES + ".TNTPrimed." + CONF_ENTITY_RADIUSMULT, 2.0f);
			conf.setProperty(CONF_ENTITIES + ".TNTPrimed." + CONF_ENTITY_YIELD, 0.15f);
			
			conf.save();
		} else if(!configVersion.equalsIgnoreCase(pluginVersion)) {
			this.log.warning(pluginDescription() + ": Found a configuration file for a different version ("+configVersion+"). Going to try using it anyway.");

			// Put any automatic configuration upgrades, such as renames, in here..
			
			this.log.warning(pluginDescription() + ": Updating configuration version to "+getDescription().getVersion());
			conf.setProperty(CONF_VERSION, getDescription().getVersion());
			
			conf.save();
		}

		return conf;
	}
}
