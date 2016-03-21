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
import ru.simsonic.rscFirstJoinDemo.Updater.BukkitUpdater;
import ru.simsonic.rscMinecraftLibrary.Bukkit.CommandAnswerException;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;
import ru.simsonic.rscMinecraftLibrary.Bukkit.Tools;

public final class BukkitPluginMain extends JavaPlugin
{
	public final static Logger  consoleLog = Bukkit.getLogger();
	public final BukkitUpdater  updating = new BukkitUpdater(this, Settings.updaterURL);
	public final BukkitSettings settings = new BukkitSettings(this);
	public final BukkitListener listener = new BukkitListener(this);
	public final BukkitCommands commands = new BukkitCommands(this);
	public final TrajectoryMngr trajMngr = new TrajectoryMngr(this);
	public final IntegrationMan intergts = new IntegrationMan(this);
	public final TrajectoryPlayer trajectoryPlayer = new TrajectoryPlayer(this);
	public final HashMap<Player, TrajectoryPlayState> playStates    = new HashMap<>();
	public final HashMap<Player, Trajectory>          playerBuffers = new HashMap<>();
	private MetricsLite metrics;
	@Override
	public void onLoad()
	{
		Phrases.extractTranslations(getDataFolder());
		settings.onLoad();
		consoleLog.log(Level.INFO, Settings.chatPrefix + "rscFirstJoinDemo has been loaded.");
	}
	@Override
	public void onEnable()
	{
		// Create directory for player playerBuffers
		new File(getDataFolder(), "buffers").mkdirs();
		// Initiate objects
		settings.onEnable();
		updating.onEnable();
		Phrases.applyTranslation(settings.getTranslationProvider());
		// Restore all online data
		for(Player online : Tools.getOnlinePlayers())
			if(online.hasPermission("rscfjd.admin"))
			{
				restorePlayerBuffer(online);
				updating.onAdminJoin(online, false);
			}
		// Register event's dispatcher
		getServer().getPluginManager().registerEvents(listener, this);
		// mcstats.org
		try
		{
			metrics = new MetricsLite(this);
			metrics.start();
			consoleLog.log(Level.INFO, Settings.chatPrefix + Phrases.PLUGIN_METRICS);
		} catch(IOException ex) {
			consoleLog.log(Level.INFO, Settings.chatPrefix + "Exception in Metrics:\n{0}", ex);
		}
		// Done
		consoleLog.log(Level.INFO, Settings.chatPrefix + Phrases.PLUGIN_ENABLED);
	}
	@Override
	public void onDisable()
	{
		// Cancel all tasks and playing demos
		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
		for(Player demo : playStates.keySet())
			trajectoryPlayer.finishDemo(demo);
		playStates.clear();
		// Save personal buffers
		for(Map.Entry<Player, Trajectory> entry : playerBuffers.entrySet())
			trajMngr.saveBufferTrajectory(entry.getKey(), entry.getValue());
		playerBuffers.clear();
		// Final cleaning
		trajMngr.onDisable();
		metrics = null;
		consoleLog.log(Level.INFO, Settings.chatPrefix + Phrases.PLUGIN_DISABLED);
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
					sender.sendMessage(GenericChatCodes.processStringStatic(Settings.chatPrefix + answer));
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
	public void restorePlayerBuffer(Player player)
	{
		final Trajectory buffer = trajMngr.loadBufferTrajectory(player);
		if(buffer.points.length > 0)
		{
			setBufferedTrajectory(player, buffer);
			commands.setSelectedPoint(player, buffer, buffer.points.length - 1, false);
			player.sendMessage(GenericChatCodes.processStringStatic(Settings.chatPrefix
				+ "Your buffer has been restored, selected last point of " + buffer.points.length + " total."));
		}
	}
}
