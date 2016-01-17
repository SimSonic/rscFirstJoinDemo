package ru.simsonic.rscFirstJoinDemo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.clip.placeholderapi.metricslite.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.simsonic.rscFirstJoinDemo.API.Settings;
import ru.simsonic.rscFirstJoinDemo.API.Trajectory;
import ru.simsonic.rscFirstJoinDemo.Bukkit.TrajectoryPlayState;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitCommands;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitListener;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitSettings;
import ru.simsonic.rscMinecraftLibrary.AutoUpdater.BukkitUpdater;
import ru.simsonic.rscMinecraftLibrary.AutoUpdater.Latest;
import ru.simsonic.rscMinecraftLibrary.AutoUpdater.UpdaterTarget;
import ru.simsonic.rscMinecraftLibrary.Bukkit.CommandAnswerException;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;
import ru.simsonic.rscMinecraftLibrary.Bukkit.Tools;

public final class BukkitPluginMain extends JavaPlugin implements UpdaterTarget
{
	public final static Logger  consoleLog = Bukkit.getLogger();
	public final BukkitUpdater  updating = new BukkitUpdater(this, Settings.updaterURL);
	public final BukkitSettings settings = new BukkitSettings(this);
	public final BukkitListener listener = new BukkitListener(this);
	public final BukkitCommands commands = new BukkitCommands(this);
	public final TrajectoryMngr trajMngr = new TrajectoryMngr(this);
	public final TrajectoryPlayer trajectoryPlayer = new TrajectoryPlayer(this);
	public final HashMap<Player, TrajectoryPlayState> playStates    = new HashMap<>();
	public final HashMap<Player, Trajectory>          playerBuffers = new HashMap<>();
	private MetricsLite metrics;
	@Override
	public void onLoad()
	{
		saveDefaultConfig();
		settings.onLoad();
		consoleLog.log(Level.INFO, "[rscfjd] rscFirstJoinDemo has been loaded.");
	}
	@Override
	public void onEnable()
	{
		settings.onEnable();
		updating.onEnable();
		for(Player online : Tools.getOnlinePlayers())
			if(online.hasPermission("rscfjd.admin"))
				updating.onAdminJoin(online);
		// Create directory for player playerBuffers
		new File(getDataFolder(), "buffers").mkdirs();
		trajMngr.setFirstJoinTrajectoryCaption(settings.getFirstJoinTrajectory());
		// Register event's dispatcher
		getServer().getPluginManager().registerEvents(listener, this);
		// mcstats.org
		try
		{
			metrics = new MetricsLite(this);
			metrics.start();
		} catch(IOException ex) {
			consoleLog.log(Level.INFO, "[rscfjd] Exception in Metrics:\n{0}", ex);
		}
		// Done
		consoleLog.log(Level.INFO, "[rscfjd] rscFirstJoinDemo has been successfully enabled.");
	}
	@Override
	public void onDisable()
	{
		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
		for(Player demo : playStates.keySet())
			trajectoryPlayer.finishDemo(demo);
		playerBuffers.clear();
		playStates.clear();
		trajMngr.clear();
		metrics = null;
		consoleLog.info("[rscfjd] rscFirstJoinDemo has been disabled.");
	}
	@Override
	public void informAboutUpdate(Set<Player> players, Latest latest)
	{
		// CONSOLE
		final ConsoleCommandSender console = getServer().getConsoleSender();
		for(String message : latest.notes)
			console.sendMessage(GenericChatCodes.processStringStatic(Settings.chatPrefix + message));
		// ONLINE ADMINS
		for(Player player : players)
			for(String message : latest.notes)
				player.sendMessage(GenericChatCodes.processStringStatic(Settings.chatPrefix + message));
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
}
