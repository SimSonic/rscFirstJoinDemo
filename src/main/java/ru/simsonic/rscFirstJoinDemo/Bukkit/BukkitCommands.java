package ru.simsonic.rscFirstJoinDemo.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.simsonic.rscFirstJoinDemo.API.Settings;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;
import ru.simsonic.rscFirstJoinDemo.BukkitPluginMain;
import ru.simsonic.rscFirstJoinDemo.Trajectory;
import ru.simsonic.rscMinecraftLibrary.Bukkit.CommandAnswerException;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public class BukkitCommands
{
	private final BukkitPluginMain plugin;
	public BukkitCommands(BukkitPluginMain plugin)
	{
		this.plugin = plugin;
	}
	public void execute(CommandSender sender, String[] args) throws CommandAnswerException
	{
		final String command = args[0].toLowerCase();
		args = Arrays.copyOfRange(args, 1, (args.length >= 4) ? args.length : 4);
		switch(command)
		{
			case "load":
				if(sender.hasPermission("rscfjd.admin"))
				{
					if(sender instanceof Player)
					{
						final String filename = (args[0] != null && !"".equals(args[0])) ? args[0] : Settings.defaultTrajectory;
						final Trajectory buffer = plugin.loadTrajectory(filename);
						plugin.setBufferedTrajectory((Player)sender, buffer);
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
						final String filename = (args[0] != null && !"".equals(args[0])) ? args[0] : Settings.defaultTrajectory;
						plugin.saveTrajectory(plugin.getBufferedTrajectory((Player)sender), filename);
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
						buffer.points = new TrajectoryPoint[] {  };
						plugin.setBufferedTrajectory((Player)sender, buffer);
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
						play_player = plugin.getServer().getPlayer(args[0]);
					else if(sender instanceof Player)
						play_player = (Player)sender;
					else
						throw new CommandAnswerException("Player-only command.");
					if(play_player == null)
						throw new CommandAnswerException("Cannot find such player.");
					if(plugin.buffers.containsKey(play_player))
						plugin.trajectoryPlayer.beginDemo(play_player, plugin.buffers.get(play_player));
					else if(plugin.lazyFirstJoinTrajectoryLoading())
						plugin.trajectoryPlayer.beginDemo(play_player, plugin.trajectories.get(plugin.firstJoinTrajectory));
					return;
				}
				break;
			case "stop":
				if(sender.hasPermission("rscfjd.admin"))
				{
					Player stop_player;
					if(args[0] != null && !"".equals(args[0]))
						stop_player = plugin.getServer().getPlayer(args[0]);
					else if(sender instanceof Player)
						stop_player = (Player)sender;
					else
						throw new CommandAnswerException("Player-only command.");
					if(stop_player == null)
						throw new CommandAnswerException("Cannot find such player.");
					plugin.trajectoryPlayer.finishDemo(stop_player);
					return;
				}
				break;
			case "addpoint":
				if(!(sender instanceof Player))
					throw new CommandAnswerException("Player-only command.");
				if(sender.hasPermission("rscfjd.admin"))
				{
					final Trajectory buffer = plugin.getBufferedTrajectory((Player)sender);
					final Player me = (Player)sender;
					final ArrayList<TrajectoryPoint> pointList = new ArrayList<>();
					if(buffer.points != null)
						pointList.addAll(Arrays.asList(buffer.points));
					final TrajectoryPoint tp = new TrajectoryPoint(me.getLocation());
					try
					{
						tp.freezeTicks = Integer.parseInt(args[0]);
					} catch(NumberFormatException ex)
					{
						tp.freezeTicks = 0;
					}
					try
					{
						tp.speedAfter = Float.parseFloat(args[1]);
					} catch(NumberFormatException ex)
					{
						tp.speedAfter = 1.0F;
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
					final Trajectory buffer = plugin.getBufferedTrajectory(me);
					int teleport_id;
					try
					{
						teleport_id = Integer.parseInt(args[0]);
					} catch(NumberFormatException ex)
					{
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
					answer.add("firstJoinTrajectory: {_YL}" + plugin.firstJoinTrajectory);
					answer.add("firstJoinTrajectory is " + (plugin.trajectories.containsKey(plugin.firstJoinTrajectory) ? "{_DR}not loaded yet" : "{_DG}already loaded"));
					if(plugin.trajectories.containsKey(plugin.firstJoinTrajectory))
						answer.add("firstJoinTrajectory contains points: {WHITE}" + plugin.trajectories.get(plugin.firstJoinTrajectory).points.length);
					if(plugin.buffers.containsKey((Player)sender))
						answer.add("Your have some trajectory points in your buffer: {WHITE}" + plugin.buffers.get((Player)sender).points.length);
					else
						answer.add("Your have no trajectory points in your buffer");
					throw new CommandAnswerException(answer);
				}
				break;
			case "help":
				if(sender.hasPermission("rscfjd.admin"))
					throw new CommandAnswerException(new String[] { "Usage:", "{YELLOW}/rscfjd play [player name]", "{YELLOW}/rscfjd stop [player name]", "{YELLOW}/rscfjd addpoint <freeze ticks> <speed after (bps)> [text w/formatting]", "{YELLOW}/rscfjd save [caption] {_LS}- save your buffer into file", "{YELLOW}/rscfjd load [caption] {_LS}- load file into your buffer", "{YELLOW}/rscfjd tp <#> {_LS}- teleport on your buffer's point #", "{YELLOW}/rscfjd clear {_LS}- clears your buffer", "{YELLOW}/rscfjd reload" });
				break;
			case "reload":
				if(sender.hasPermission("rscfjd.admin"))
				{
					plugin.reloadConfig();
					plugin.getPluginLoader().disablePlugin(plugin);
					plugin.getPluginLoader().enablePlugin(plugin);
					plugin.getServer().getConsoleSender().sendMessage("[rscfjd] rscFirstJoinDemo has been reloaded.");
					return;
				}
				break;
		}
		throw new CommandAnswerException("{_LR}Not enough permissions.");
	}
}
