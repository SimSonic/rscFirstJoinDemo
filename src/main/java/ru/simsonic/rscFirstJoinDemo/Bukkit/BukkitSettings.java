package ru.simsonic.rscFirstJoinDemo.Bukkit;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.simsonic.rscFirstJoinDemo.API.Settings;
import ru.simsonic.rscFirstJoinDemo.API.TranslationProvider;
import ru.simsonic.rscFirstJoinDemo.BukkitPluginMain;

public class BukkitSettings implements Settings
{
	private final BukkitPluginMain plugin;
	public BukkitSettings(BukkitPluginMain plugin)
	{
		this.plugin = plugin;
	}
	@Override
	public void onLoad()
	{
		plugin.saveDefaultConfig();
	}
	@Override
	public void onEnable()
	{
		final FileConfiguration config = plugin.getConfig();
		switch(config.getInt("internal.version", 0))
		{
			case 0:
				// EMPTY (CLEARED) CONFIG?
				BukkitPluginMain.consoleLog.info("[rscfjd] Filling config.yml with default values...");
				// FILL WITH DEFAULT VALUES
				config.set("settings.trajectory", Settings.DEFAULT_TRAJECTORY);
				// FINISH
				config.set("internal.version", 1);
			case 1:
				BukkitPluginMain.consoleLog.info("[rscfjd] Updating config.yml version (v1 -> v2).");
				// REMOVE NODES
				config.set("settings.turn-into-spectator", null);
				config.set("settings.signs", null);
				// FINISH
				config.set("internal.version", 2);
				plugin.saveConfig();
			case 2:
				BukkitPluginMain.consoleLog.info("[rscfjd] Updating config.yml version (v2 -> v3).");
				// RENAME NODE trajectory -> first-join-trajectory
				config.set("settings.first-join-trajectory", config.getString("settings.trajectory", DEFAULT_TRAJECTORY));
				config.set("settings.trajectory", null);
				// NEW NODES
				config.set("settings.first-join-enable", true);
				config.set("settings.first-join-delay", 20);
				config.set("settings.require-permissions-for-signs", false);
				config.set("settings.logging.player-start-stop", true);
				config.set("settings.logging.player-point-reached", false);
				// FINISH
				config.set("internal.version", 3);
				plugin.saveConfig();
			case 3:
				BukkitPluginMain.consoleLog.info("[rscfjd] Updating config.yml version (v3 -> v4).");
				// FIX MY OLD MISTAKE ('logging' node was named 'log')
				config.set("settings.log", null);
				// NEW NODE
				config.set("settings.language", "english");
				// FINISH
				config.set("internal.version", 4);
			case 4:
				// NEWEST VERSION
				break;
			default:
				// UNSUPPORTED VERSION?
				break;
		}
		plugin.saveConfig();
		plugin.reloadConfig();
	}
	@Override
	public String getFirstJoinTrajectory()
	{
		final FileConfiguration config = plugin.getConfig();
		final String result = config.getString("settings.first-join-trajectory", DEFAULT_TRAJECTORY);
		config.set("settings.first-join-trajectory", result);
		plugin.saveConfig();
		return result;
	}
	@Override
	public boolean getFirstJoinEnabled()
	{
		final FileConfiguration config = plugin.getConfig();
		final boolean result = config.getBoolean("settings.first-join-enable", true);
		config.set("settings.first-join-enable", result);
		plugin.saveConfig();
		return result;
	}
	@Override
	public int getFirstJoinDelay()
	{
		final FileConfiguration config = plugin.getConfig();
		final int result = config.getInt("settings.first-join-delay", 20);
		config.set("settings.first-join-delay", result);
		plugin.saveConfig();
		return result;
	}
	@Override
	public boolean getRequireSignPerms()
	{
		final FileConfiguration config = plugin.getConfig();
		final boolean result = config.getBoolean("settings.require-permissions-for-signs", false);
		config.set("settings.require-permissions-for-signs", result);
		plugin.saveConfig();
		return result;
	}
	@Override
	public boolean getLogStartStop()
	{
		final FileConfiguration config = plugin.getConfig();
		final boolean result = config.getBoolean("settings.logging.player-start-stop", true);
		config.set("settings.logging.player-start-stop", result);
		plugin.saveConfig();
		return result;
	}
	@Override
	public boolean getLogPointReached()
	{
		final FileConfiguration config = plugin.getConfig();
		final boolean result = config.getBoolean("settings.logging.player-point-reached", false);
		config.set("settings.logging.player-point-reached", result);
		plugin.saveConfig();
		return result;
	}
	@Override
	public String getLanguage()
	{
		final FileConfiguration config = plugin.getConfig();
		final String result = config.getString("settings.language", "english");
		config.set("settings.language", result);
		plugin.saveConfig();
		return result;
	}
	@Override
	public TranslationProvider getTranslationProvider()
	{
		final File langFile = new File(plugin.getDataFolder(), getLanguage() + ".yml");
		final YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
		return new TranslationProvider()
		{
			@Override
			public String getString(String path)
			{
				return langConfig.getString(path, path);
			}
		};
	}
}
