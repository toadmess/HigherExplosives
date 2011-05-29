package toadmess.explosives.messymocks;

import java.io.File;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;

public class StubPluginManager implements PluginManager {
	@Override
	public void callEvent(Event arg0) {}

	@Override
	public void clearPlugins() {}

	@Override
	public void disablePlugin(Plugin arg0) {}

	@Override
	public void disablePlugins() {}

	@Override
	public void enablePlugin(Plugin arg0) {}

	@Override
	public Plugin getPlugin(String arg0) {
		return null;
	}

	@Override
	public Plugin[] getPlugins() {
		return null;
	}

	@Override
	public boolean isPluginEnabled(String arg0) {
		return false;
	}

	@Override
	public boolean isPluginEnabled(Plugin arg0) {
		return false;
	}

	@Override
	public Plugin loadPlugin(File arg0) throws InvalidPluginException,
			InvalidDescriptionException, UnknownDependencyException {
		return null;
	}

	@Override
	public Plugin[] loadPlugins(File arg0) {
		return null;
	}

	@Override
	public void registerEvent(Type arg0, Listener arg1, Priority arg2, Plugin arg3) {}

	@Override
	public void registerEvent(Type arg0, Listener arg1, EventExecutor arg2,
			Priority arg3, Plugin arg4) {}

	@Override
	public void registerInterface(Class<? extends PluginLoader> arg0)
			throws IllegalArgumentException {}
}
