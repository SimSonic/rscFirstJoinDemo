package ru.simsonic.rscFirstJoinDemo;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.simsonic.rscFirstJoinDemo.API.Settings;
import ru.simsonic.rscFirstJoinDemo.API.Trajectory;
import ru.simsonic.rscFirstJoinDemo.Bukkit.TrajectoryPlayState;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitCommands;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitListener;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitSettings;
import ru.simsonic.rscMinecraftLibrary.AutoUpdater.AutoUpdater;
import ru.simsonic.rscMinecraftLibrary.AutoUpdater.Latest;
import ru.simsonic.rscMinecraftLibrary.Bukkit.CommandAnswerException;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public final class BukkitPluginMain extends JavaPlugin
{
	public final static Logger  consoleLog = Bukkit.getLogger();
	
	public final BukkitSettings settings = new BukkitSettings(this);
	public final BukkitListener listener = new BukkitListener(this);
	public final BukkitCommands commands = new BukkitCommands(this);
	public final TrajectoryMngr trajMngr = new TrajectoryMngr(this);
	public final TrajectoryPlayer trajectoryPlayer = new TrajectoryPlayer(this);
	public final HashMap<Player, TrajectoryPlayState> playStates    = new HashMap<>();
	public final HashMap<Player, Trajectory>          playerBuffers = new HashMap<>();
	private final AutoUpdater updater = new AutoUpdater(this, Settings.updaterURL);
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
		final String firstJoinTrajectory = settings.getFirstJoinTrajectory();
		trajMngr.setFirstJoinTrajectoryCaption(firstJoinTrajectory);
		// Create directory for player playerBuffers
		new File(getDataFolder(), "buffers").mkdirs();
		// Register event's dispatcher
		getServer().getPluginManager().registerEvents(listener, this);
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
		consoleLog.info("[rscfjd] rscFirstJoinDemo has been disabled.");
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
