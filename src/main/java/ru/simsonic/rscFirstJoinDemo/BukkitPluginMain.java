package ru.simsonic.rscFirstJoinDemo;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.simsonic.rscUtilityLibrary.Bukkit.Commands.CommandAnswerException;
import ru.simsonic.rscUtilityLibrary.TextProcessing.GenericChatCodes;

public final class BukkitPluginMain extends JavaPlugin
{
	protected static final String chatPrefix        = "{DARKGREEN}[rscfjd] {LIGHTGREEN}";
	protected static final String defaultTrajectory = "trajectory";
	protected static final Logger consoleLog        = Bukkit.getLogger();
	protected final BukkitPlayerListener listener = new BukkitPlayerListener(this);
	protected final TrajectoryPlayer trajectoryPlayer = new TrajectoryPlayer(this);
	protected final HashMap<String, Trajectory> trajectories = new HashMap<>();
	protected final HashMap<Player, TrajectoryPlayState> playing = new HashMap<>();
	protected final HashMap<Player, Trajectory> buffers = new HashMap<>();
	protected String firstJoinTrajectory;
	@Override
	public void onLoad()
	{
		saveDefaultConfig();
		switch(getConfig().getInt("internal.version", 0))
		{
			case 0:
				// EMPTY (CLEARED) CONFIG?
				consoleLog.info("Filling config.yml with default values...");
				getConfig().set("settings.trajectory", defaultTrajectory);
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
		firstJoinTrajectory = getConfig().getString("settings.trajectory", defaultTrajectory);
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
			switch(label.toLowerCase())
			{
				case "rscfjd":
					execute(sender, args);
					break;
			}
		} catch(CommandAnswerException ex) {
			for(String answer : ex.getMessageArray())
				sender.sendMessage(GenericChatCodes.processStringStatic(chatPrefix + answer));
		}
		return true;
	}
	protected boolean lazyFirstJoinTrajectoryLoading()
	{
		// Lazy trajectory loading
		if(trajectories.containsKey(firstJoinTrajectory) == false)
			loadTrajectory(firstJoinTrajectory);
		return trajectories.containsKey(firstJoinTrajectory);
	}
	protected Trajectory loadTrajectory(String caption)
	{
		Trajectory result;
		caption = caption.toLowerCase();
		// Load default currentTrajectory
		try(FileInputStream fis = new FileInputStream(new File(getDataFolder(), caption + ".json")))
		{
			final JsonReader jr = new JsonReader(new InputStreamReader(fis, "UTF-8"));
			result = new Gson().fromJson(jr, Trajectory.class);
		} catch(IOException | JsonParseException ex) {
			result = null;
			consoleLog.log(Level.WARNING, "[rscfjd] Error reading {0}.json: {1}", new Object[] { caption, ex });
		}
		if(result == null)
			result = new Trajectory();
		if(result.points == null)
			result.points = new TrajectoryPoint[] {};
		for(TrajectoryPoint tp : result.points)
			tp.location = locationForTrajectoryPoint(tp);
		consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} has been loaded ({1})", new Object[] { caption, result.points.length });
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
	protected void saveTrajectory(Trajectory trajectory, String caption)
	{
		if(trajectory == null)
			return;
		caption = caption.toLowerCase();
		try(JsonWriter jw = new JsonWriter(
			new OutputStreamWriter(new FileOutputStream(
				new File(getDataFolder(), caption + ".json")), "UTF-8")))
		{
			jw.setIndent(" ");
			jw.setSerializeNulls(false);
			new Gson().toJson(trajectory, Trajectory.class, jw);
			consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} has been saved ({1})", new Object[] { caption, trajectory.points.length });
		} catch(IOException | JsonParseException ex) {
			consoleLog.log(Level.WARNING, "[rscfjd] Error writing {0}.json: {1}", new Object[] { caption, ex });
		}
	}
	private Trajectory getBufferedTrajectory(Player player)
	{
		if(buffers.containsKey(player))
			return buffers.get(player);
		final Trajectory result = new Trajectory();
		buffers.put(player, result);
		return result;
	}
	private void setBufferedTrajectory(Player player, Trajectory buffer)
	{
		buffers.put(player, buffer);
	}
	private void execute(CommandSender sender, String[] args) throws CommandAnswerException
	{
		if(args.length == 0)
			throw new CommandAnswerException("{MAGENTA}rscFirstJoinDemo {_LS}" + getDescription().getVersion() + "{MAGENTA} by SimSonic.");
		final String command = args[0].toLowerCase();
		args = Arrays.copyOfRange(args, 1, (args.length >= 4) ? args.length : 4);
		switch(command)
		{
		case "load":
			if(sender.hasPermission("rscfjd.admin"))
			{
				if(sender instanceof Player)
				{
					final String filename = (args[0] != null && !"".equals(args[0])) ? args[0] : defaultTrajectory;
					final Trajectory buffer = loadTrajectory(filename);
					setBufferedTrajectory((Player)sender, buffer);
					throw new CommandAnswerException("Loaded (" + buffer.points.length + " nodes)");
				}
				throw new CommandAnswerException("This command cannot be run from console.");
			}
			break;
		case "save":
			if(sender.hasPermission("rscfjd.admin"))
			{
				if(sender instanceof Player)
				{
					final String filename = (args[0] != null && !"".equals(args[0])) ? args[0] : defaultTrajectory;
					saveTrajectory(getBufferedTrajectory((Player)sender), filename);
					throw new CommandAnswerException("Saved {_LC}" + filename + ".json{_LG}.");
				}
				throw new CommandAnswerException("This command cannot be run from console.");
			}
			break;
		case "clear":
			if(sender.hasPermission("rscfjd.admin"))
			{
				if(sender instanceof Player)
				{
					final Trajectory buffer = new Trajectory();
					buffer.points = new TrajectoryPoint[] {};
					setBufferedTrajectory((Player)sender, buffer);
					throw new CommandAnswerException("{_LG}Buffer cleared.");
				}
				throw new CommandAnswerException("This command cannot be run from console.");
			}
			break;
		case "play":
			if(sender.hasPermission("rscfjd.admin"))
			{
				Player play_player;
				if(args[0] != null && !"".equals(args[0]))
					play_player = getServer().getPlayer(args[0]);
				else if(sender instanceof Player)
					play_player = (Player)sender;
				else
					throw new CommandAnswerException("Player-only command.");
				if(play_player == null)
					throw new CommandAnswerException("Cannot find such player.");
				if(buffers.containsKey(play_player))
					trajectoryPlayer.beginDemo(play_player, buffers.get(play_player));
				else
					if(lazyFirstJoinTrajectoryLoading())
						trajectoryPlayer.beginDemo(play_player, trajectories.get(firstJoinTrajectory));
				return;
			}
			break;
		case "stop":
			if(sender.hasPermission("rscfjd.admin"))
			{
				Player stop_player;
				if(args[0] != null && !"".equals(args[0]))
					stop_player = getServer().getPlayer(args[0]);
				else if(sender instanceof Player)
					stop_player = (Player)sender;
				else
					throw new CommandAnswerException("Player-only command.");
				if(stop_player == null)
					throw new CommandAnswerException("Cannot find such player.");
				trajectoryPlayer.finishDemo(stop_player);
				return;
			}
			break;
		// addpoint <freeze ticks> <set speed> [message]
		case "addpoint":
			if(!(sender instanceof Player))
				throw new CommandAnswerException("Player-only command.");
			if(sender.hasPermission("rscfjd.admin"))
			{
				final Trajectory buffer = getBufferedTrajectory((Player)sender);
				final Player me = (Player)sender;
				final ArrayList<TrajectoryPoint> pointList = new ArrayList<>();
				if(buffer.points != null)
					pointList.addAll(Arrays.asList(buffer.points));
				final TrajectoryPoint tp = new TrajectoryPoint(me.getLocation());
				try
				{
					tp.freezeTicks = Integer.parseInt(args[0]);
				} catch(NumberFormatException ex) {
					tp.freezeTicks = 0;
				}
				try
				{
					tp.speedAfter = Float.parseFloat(args[1]);
				} catch(NumberFormatException ex) {
					tp.speedAfter = 1.0f;
				}
				tp.messageOnReach = GenericChatCodes.glue(Arrays.copyOfRange(args, 2, args.length), " ");
				tp.fly = true;
				pointList.add(tp);
				buffer.points = pointList.toArray(new TrajectoryPoint[pointList.size()]);
				throw new CommandAnswerException("Done #" + pointList.size());
			}
			break;
		case "tp":
			if(!(sender instanceof Player))
				throw new CommandAnswerException("Player-only command.");
			if(sender.hasPermission("rscfjd.admin"))
			{
				final Player me = (Player)sender;
				final Trajectory buffer = getBufferedTrajectory(me);
				int teleport_id;
				try
				{
					teleport_id = Integer.parseInt(args[0]);
				} catch(NumberFormatException ex) {
					teleport_id = buffer.points.length - 1;
				}
				if(teleport_id >= 0 && teleport_id < buffer.points.length)
				{
					me.teleport(buffer.points[teleport_id].location);
					throw new CommandAnswerException("Teleported to #" + teleport_id);
				}
				throw new CommandAnswerException("{_LR}Out of range (0..." + (buffer.points.length - 1) + ").");
			}
			break;
		case "info":
			if(sender.hasPermission("rscfjd.admin"))
			{
				final ArrayList<String> answer = new ArrayList<>();
				answer.add("Current configuration:");
				answer.add("firstJoinTrajectory: {_YL}" + firstJoinTrajectory);
				answer.add("firstJoinTrajectory is " + (trajectories.containsKey(firstJoinTrajectory)
					? "{_DR}not loaded yet"
					: "{_DG}already loaded"));
				if(trajectories.containsKey(firstJoinTrajectory))
					answer.add("firstJoinTrajectory contains points: {WHITE}"
						+ trajectories.get(firstJoinTrajectory).points.length);
				if(buffers.containsKey((Player)sender))
					answer.add("Your have some trajectory points in your buffer: {WHITE}"
					+ buffers.get((Player)sender).points.length);
				else
					answer.add("Your have no trajectory points in your buffer");
				throw new CommandAnswerException(answer);
			}
			break;
		case "help":
			if(sender.hasPermission("rscfjd.admin"))
				throw new CommandAnswerException(new String[]
				{
					"Usage:",
					"{YELLOW}/rscfjd play [player name]",
					"{YELLOW}/rscfjd stop [player name]",
					"{YELLOW}/rscfjd addpoint <freeze ticks> <speed after (bps)> [text w/formatting]",
					"{YELLOW}/rscfjd save [caption] {_LS}- save your buffer into file",
					"{YELLOW}/rscfjd load [caption] {_LS}- load file into your buffer",
					"{YELLOW}/rscfjd tp <#> {_LS}- teleport on your buffer's point #",
					"{YELLOW}/rscfjd clear {_LS}- clears your buffer",
					"{YELLOW}/rscfjd reload",
				});
			break;
		case "reload":
			if(sender.hasPermission("rscfjd.admin"))
			{
				reloadConfig();
				getPluginLoader().disablePlugin(this);
				getPluginLoader().enablePlugin(this);
				getServer().getConsoleSender().sendMessage("[rscfjd] rscFirstJoinDemo has been reloaded.");
				return;
			}
			break;
		}
		throw new CommandAnswerException("{_LR}Not enough permissions.");
	}
}
