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
	protected static final String CONF_YIELD = "everyExplosionYield";
	protected static final String CONF_ENTITIES = "entities";
	protected static final String CONF_RADIUS_MULT = "radiusMultiplier";
	protected static final String CONF_FIRE = "fire";

	@Override
	public void onEnable() {
		final PluginManager pm = getServer().getPluginManager();
		
		for(final Class<? extends Entity> entityClass : new Class[]{TNTPrimed.class, Creeper.class, Fireball.class}) {
			final ExplodingListener primedListener = new ExplodingListener(this.getConfiguration(), entityClass);
			pm.registerEvent(Event.Type.EXPLOSION_PRIMED, primedListener, Event.Priority.Normal, this);
		}
	
		final ExplodedListener explodedlistener = new ExplodedListener(this.getConfiguration());
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, explodedlistener, Event.Priority.Normal, this);
		
		System.out.println(pluginDescription() + " enabled");
	}

	@Override
	public void onDisable() {
		System.out.println(pluginDescription() + " disabled");
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
			conf.setProperty(CONF_YIELD, 0.3f);
			
			conf.setProperty(CONF_ENTITIES + ".TNTPrimed." + CONF_RADIUS_MULT, 2.0f);
			conf.setProperty(CONF_ENTITIES + ".TNTPrimed." + CONF_FIRE, false);

			conf.setProperty(CONF_ENTITIES + ".Creeper." + CONF_RADIUS_MULT, 1.0f);
			conf.setProperty(CONF_ENTITIES + ".Creeper." + CONF_FIRE, false);

			conf.setProperty(CONF_ENTITIES + ".Fireball." + CONF_RADIUS_MULT, 1.0f);
			conf.setProperty(CONF_ENTITIES + ".Fireball." + CONF_FIRE, false);
			
			conf.save();
		} else if(!configVersion.equalsIgnoreCase(pluginVersion)) {
			System.out.println(pluginDescription() + " WARNING! Found a configuration file for a different version ("+configVersion+"). Going to try using it anyway.");
		} 

		return conf;
	}
}
