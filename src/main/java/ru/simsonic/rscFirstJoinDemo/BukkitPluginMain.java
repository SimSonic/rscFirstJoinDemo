package ru.simsonic.rscFirstJoinDemo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import ru.simsonic.rscCommonsLibrary.HashAndCipherUtilities;
import ru.simsonic.rscFirstJoinDemo.API.Settings;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitCommands;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitListener;
import ru.simsonic.rscMinecraftLibrary.Bukkit.CommandAnswerException;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public final class BukkitPluginMain extends JavaPlugin
{
	public final static Logger consoleLog = Bukkit.getLogger();
	public final BukkitListener listener = new BukkitListener(this);
	public final TrajectoryPlayer trajectoryPlayer = new TrajectoryPlayer(this);
	public final HashMap<String, Trajectory> trajectories = new HashMap<>();
	public final HashMap<Player, TrajectoryPlayState> playing = new HashMap<>();
	public final HashMap<Player, Trajectory> buffers = new HashMap<>();
	public final BukkitCommands commands = new BukkitCommands(this);
	public String firstJoinTrajectory = Settings.defaultTrajectory;
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
				getConfig().set("settings.turn-into-spectator", getConfig().getBoolean("settings.turn-into-spectator", true));
				saveConfig();
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
		firstJoinTrajectory = getConfig().getString("settings.trajectory", Settings.defaultTrajectory);
		getConfig().set("settings.trajectory", firstJoinTrajectory);
		saveConfig();
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
		for(Player demo : playing.keySet())
			trajectoryPlayer.finishDemo(demo);
		saveConfig();
		buffers.clear();
		playing.clear();
		trajectories.clear();
		consoleLog.info("[rscfjd] rscFirstJoinDemo has been disabled.");
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		try
		{
			if(args.length == 0)
			{
				final PluginDescriptionFile desc = this.getDescription();
				throw new CommandAnswerException(new String[]
				{
					"{_LP}" + desc.getName() + " {_LS}" + desc.getVersion() + "{_LP} © " + desc.getAuthors().get(0),
					"{_LP}Website: {GOLD}" + desc.getWebsite(),
				});
			}
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
	public boolean lazyFirstJoinTrajectoryLoading()
	{
		// Lazy trajectory loading
		if(trajectories.containsKey(firstJoinTrajectory) == false)
			loadTrajectory(firstJoinTrajectory);
		return trajectories.containsKey(firstJoinTrajectory);
	}
	public Trajectory loadTrajectory(String caption)
	{
		Trajectory result;
		caption = caption.toLowerCase();
		// Load default currentTrajectory
		try
		{
			result = HashAndCipherUtilities.loadObject(new File(getDataFolder(), caption + ".json"), Trajectory.class);
		} catch(IOException ex) {
			consoleLog.log(Level.WARNING, "[rscfjd] Error reading {0}.json: {1}", new Object[] { caption, ex });
			result = new Trajectory();
		}
		if(result.points == null)
			result.points = new TrajectoryPoint[] {};
		for(TrajectoryPoint tp : result.points)
			tp.location = locationForTrajectoryPoint(tp);
		consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} contains ({1} points)", new Object[] { caption, result.points.length });
		result.caption = caption;
		trajectories.put(caption, result);
		return result;
	}
	private Location locationForTrajectoryPoint(TrajectoryPoint tp)
	{
		final World world = getServer().getWorld(tp.world);
		if(world != null)
			return new Location(world, tp.x, tp.y, tp.z, tp.yaw, tp.pitch);
		BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] World not found: {0}", tp.world);
		return null;
	}
	public void saveTrajectory(Trajectory trajectory, String caption)
	{
		if(trajectory == null)
			return;
		caption = caption.toLowerCase();
		try
		{
			HashAndCipherUtilities.saveObject(new File(getDataFolder(), caption + ".json"), trajectory, Trajectory.class);
			consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} has been saved ({1})", new Object[] { caption, trajectory.points.length });
		} catch(IOException ex) {
			consoleLog.log(Level.WARNING, "[rscfjd] Error writing {0}.json: {1}", new Object[] { caption, ex });
		}
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
