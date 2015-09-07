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
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitCommands;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitListener;
import ru.simsonic.rscMinecraftLibrary.Bukkit.CommandAnswerException;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public final class BukkitPluginMain extends JavaPlugin
{
	public final static Logger consoleLog = Bukkit.getLogger();
	public final BukkitListener listener = new BukkitListener(this);
	public final BukkitCommands commands = new BukkitCommands(this);
	public final TrajectoryMngr trajMngr = new TrajectoryMngr(this);
	public final TrajectoryPlayer trajectoryPlayer = new TrajectoryPlayer(this);
	public final HashMap<Player, TrajectoryPlayState> playStates = new HashMap<>();
	public final HashMap<Player, Trajectory> buffers = new HashMap<>();
	@Override
	public void onLoad()
	{
		saveDefaultConfig();
		switch(getConfig().getInt("internal.version", 0))
		{
			case 0:
				// EMPTY (CLEARED) CONFIG?
				consoleLog.info("Filling config.yml with default values...");
				getConfig().set("settings.trajectory", Settings.defaultTrajectory);
				getConfig().set("settings.signs.note", "{GOLD}Полёт по демо!");
				getConfig().set("internal.version", 1);
			case 1:
				consoleLog.info("Updating config.yml version (v1 -> v2).");
				getConfig().set("settings.turn-into-spectator", null);
				getConfig().set("settings.signs", null);
				getConfig().set("internal.version", 2);
				saveConfig();
			case 2:
				// NEWEST VERSION
				break;
			default:
				// UNSUPPORTED VERSION?
				break;
		}
		consoleLog.log(Level.INFO, "[rscfjd] rscFirstJoinDemo has been loaded.");
	}
	@Override
	public void onEnable()
	{
		// Read settings
		reloadConfig();
		final String firstJoinTrajectory = getConfig().getString("settings.trajectory", Settings.defaultTrajectory);
		getConfig().set("settings.trajectory", firstJoinTrajectory);
		trajMngr.setFirstJoinTrajectory(firstJoinTrajectory);
		saveConfig();
		// Create directory for player buffers
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
		saveConfig();
		buffers.clear();
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
		if(buffers.containsKey(player))
			return buffers.get(player);
		final Trajectory result = new Trajectory();
		buffers.put(player, result);
		return result;
	}
	public void setBufferedTrajectory(Player player, Trajectory buffer)
	{
		buffers.put(player, buffer);
	}
}
