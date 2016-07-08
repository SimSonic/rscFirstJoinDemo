package ru.simsonic.rscFirstJoinDemo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;
import ru.simsonic.rscFirstJoinDemo.API.Settings;
import ru.simsonic.rscFirstJoinDemo.API.Trajectory;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitCommands;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitListener;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitSettings;
import ru.simsonic.rscFirstJoinDemo.Bukkit.IntegrationMan;
import ru.simsonic.rscFirstJoinDemo.Bukkit.TrajectoryPlayState;
import ru.simsonic.rscMinecraftLibrary.AutoUpdater.BukkitUpdater;
import ru.simsonic.rscMinecraftLibrary.Bukkit.CommandAnswerException;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;
import ru.simsonic.rscMinecraftLibrary.Bukkit.Tools;

public final class BukkitPluginMain extends JavaPlugin
{
	public final static Logger    consoleLog = Bukkit.getLogger();
	public final BukkitUpdater    updating = new BukkitUpdater(this, Settings.UPDATER_URL, Settings.CHAT_PREFIX, Settings.UPDATE_CMD);
	public final BukkitSettings   settings = new BukkitSettings(this);
	public final BukkitListener   listener = new BukkitListener(this);
	public final BukkitCommands   commands = new BukkitCommands(this);
	public final TrajectoryMngr   trajMngr = new TrajectoryMngr(this);
	public final IntegrationMan   intergts = new IntegrationMan(this);
	public final TrajectoryPlayer demoMngr = new TrajectoryPlayer(this);
	public final HashMap<Player, TrajectoryPlayState> playStates    = new HashMap<>();
	public final HashMap<Player, Trajectory>          playerBuffers = new HashMap<>();
	private MetricsLite metrics;
	@Override
	public void onLoad()
	{
		settings.onLoad();
		Phrases.extractTranslations(getDataFolder());
		// Create directories for public and personal buffers
		new File(getDataFolder(), Settings.DIR_PERSONAL).mkdirs();
		new File(getDataFolder(), Settings.DIR_TRAJECTORIES).mkdirs();
		consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + "rscFirstJoinDemo has been loaded.");
	}
	@Override
	public void onEnable()
	{
		// Initiate objects
		settings.onEnable();
		updating.onEnable();
		Phrases.applyTranslation(settings.getTranslationProvider());
		// Restore all online data
		for(Player online : Tools.getOnlinePlayers())
			if(online.hasPermission("rscfjd.admin"))
			{
				trajMngr.restorePlayerBuffer(online);
				updating.onAdminJoin(online, false);
			}
		// Register event's dispatcher
		getServer().getPluginManager().registerEvents(listener, this);
		// mcstats.org
		try
		{
			metrics = new MetricsLite(this);
			metrics.start();
			consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + "{0}", Phrases.PLUGIN_METRICS);
		} catch(IOException ex) {
			consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + "Exception in Metrics:\n{0}", ex);
		}
		// Done
		consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + "{0}", Phrases.PLUGIN_ENABLED);
	}
	@Override
	public void onDisable()
	{
		// Cancel all tasks and playing demos
		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
		for(Player demo : playStates.keySet())
			demoMngr.finishDemo(demo);
		playStates.clear();
		// Save personal buffers
		for(Map.Entry<Player, Trajectory> entry : playerBuffers.entrySet())
			trajMngr.saveBufferTrajectory(entry.getKey(), entry.getValue());
		playerBuffers.clear();
		// Final cleaning
		trajMngr.onDisable();
		metrics = null;
		consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + "{0}", Phrases.PLUGIN_DISABLED);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		try
		{
			switch(command.getName().toLowerCase())
			{
				case "rscfjd":
					commands.execute(sender, args);
					return true;
			}
			return false;
		} catch(CommandAnswerException ex) {
			for(String answer : ex.getMessageArray())
				if(answer != null)
					sender.sendMessage(GenericChatCodes.processStringStatic(Settings.CHAT_PREFIX + answer));
		}
		return true;
	}
	public Trajectory getBufferedTrajectory(Player player)
	{
		if(playerBuffers.containsKey(player))
			return playerBuffers.get(player);
		final Trajectory result = new Trajectory();
		playerBuffers.put(player, result);
		return result;
	}
	public void setBufferedTrajectory(Player player, Trajectory buffer)
	{
		playerBuffers.put(player, buffer);
	}
}
