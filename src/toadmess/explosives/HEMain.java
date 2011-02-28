package toadmess.explosives;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class HEMain extends JavaPlugin {
	protected static final String CONF_VERSION = "version";
	
	protected static final String CONF_EVERY = "everyExplosion";
	protected static final String CONF_EVERY_YIELD = "yield";
	protected static final String CONF_EVERY_YIELD_BOUNDS = "yieldChangeActiveBounds";
	
	protected static final String CONF_ENTITIES = "entities";
	protected static final String CONF_ENTITY_RADIUS_MULT = "radiusMultiplier";
	protected static final String CONF_FIRE = "fire";
	
	protected static final String CONF_BOUNDS = "activeBounds";
	
	protected static final String CONF_BOUNDS_MAX = "max";
	protected static final String CONF_BOUNDS_MIN = "min";

	@Override
	public void onEnable() {
		final PluginManager pm = getServer().getPluginManager();
		
		regExplodingListener(pm, TNTPrimed.class);
		regExplodingListener(pm, Creeper.class);
		regExplodingListener(pm, Fireball.class);
			
		final ExplodedListener explodedlistener = new ExplodedListener(this.getConfiguration());
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, explodedlistener, Event.Priority.Normal, this);
		
		System.out.println(pluginDescription() + " primed and ready");
	}

	@Override
	public void onDisable() {
		System.out.println(pluginDescription() + " defused and disabled");
	}
	
	private void regExplodingListener(final PluginManager pm, final Class<? extends Entity> entityClass) {
		final ExplodingListener primedListener = new ExplodingListener(this.getConfiguration(), entityClass);
		pm.registerEvent(Event.Type.EXPLOSION_PRIMED, primedListener, Event.Priority.Normal, this);
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
			System.out.println(pluginDescription() + " found no configuration file. Creating a fresh default one.");
			
			conf.setProperty(CONF_VERSION, pluginVersion);
			conf.setProperty(CONF_EVERY + "." + CONF_EVERY_YIELD, 0.3f);
			
			conf.setProperty(CONF_ENTITIES + ".TNTPrimed." + CONF_ENTITY_RADIUS_MULT, 2.0f);
			conf.setProperty(CONF_ENTITIES + ".TNTPrimed." + CONF_FIRE, false);

			conf.setProperty(CONF_ENTITIES + ".Creeper." + CONF_ENTITY_RADIUS_MULT, 1.0f);
			conf.setProperty(CONF_ENTITIES + ".Creeper." + CONF_FIRE, false);

			conf.setProperty(CONF_ENTITIES + ".Fireball." + CONF_ENTITY_RADIUS_MULT, 1.0f);
			conf.setProperty(CONF_ENTITIES + ".Fireball." + CONF_FIRE, false);
			
			conf.save();
		} else if(!configVersion.equalsIgnoreCase(pluginVersion)) {
			System.out.println(pluginDescription() + ": Warning! Found a configuration file for a different version ("+configVersion+"). Going to try using it anyway.");

			upgradeOldYieldConfig(conf);
		} 

		return conf;
	}
	
	private void upgradeOldYieldConfig(final Configuration conf) {
		// Versions prior to 1.2 used to have the "everyExplosionYield" property, but this has now changed to
		// be "everyExplosion.yield". Update this automagically.
		final String oldYieldConfKey = "everyExplosionYield";
		if(null != conf.getProperty(oldYieldConfKey)) {
			final String newYieldConfKey = CONF_EVERY+"."+CONF_EVERY_YIELD;
			System.out.println(pluginDescription() + ": Warning. Found old \""+oldYieldConfKey+"\" configuration key.");
			
			if(null != conf.getProperty(newYieldConfKey)) {
				System.out.println(pluginDescription() + ": You've got the new yield configuration in there already, so I'll use that.");
			} else {
				final double yieldValue = conf.getDouble(oldYieldConfKey, 0.3F);
				System.out.println(pluginDescription() + ": Creating new \""+newYieldConfKey+"\" configuration with value of " + yieldValue);
				conf.setProperty(newYieldConfKey, yieldValue);
			}
			
			System.out.println(pluginDescription() + ": Removing old \""+oldYieldConfKey+"\" configuration key.");
			conf.removeProperty(oldYieldConfKey);
			
			System.out.println(pluginDescription() + ": Updating configuration version to "+getDescription().getVersion());
			conf.setProperty(CONF_VERSION, getDescription().getVersion());

			conf.save();
		}
	}
}
