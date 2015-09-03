package ru.simsonic.rscFirstJoinDemo.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.ChatColor;
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
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final String filename = (args[0] != null && !"".equals(args[0])) ? args[0] : Settings.defaultTrajectory;
					final Trajectory buffer = plugin.trajMngr.loadTrajectory(filename);
					plugin.setBufferedTrajectory(player, buffer);
					throw new CommandAnswerException("Loaded (" + buffer.points.length + " nodes)");
				}
				break;
			case "save":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final String filename = (args[0] != null && !"".equals(args[0])) ? args[0] : Settings.defaultTrajectory;
					plugin.trajMngr.saveTrajectory(plugin.getBufferedTrajectory(player), filename);
					throw new CommandAnswerException("Saved {_LC}" + filename + ".json{_LG}.");
				}
				break;
			case "clear":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final Trajectory buffer = new Trajectory();
					buffer.points = new TrajectoryPoint[] {};
					plugin.setBufferedTrajectory(player, buffer);
					throw new CommandAnswerException("{_LG}Buffer cleared.");
				}
				break;
			case "add":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final Trajectory buffer = plugin.getBufferedTrajectory(player);
					final ArrayList<TrajectoryPoint> pointList = new ArrayList<>();
					if(buffer.points != null)
						pointList.addAll(Arrays.asList(buffer.points));
					final TrajectoryPoint tp = new TrajectoryPoint(player.getLocation());
					try
					{
						tp.freezeTicks = Integer.parseInt(args[0]);
					} catch(ArrayIndexOutOfBoundsException | NullPointerException ex) {
						throw new CommandAnswerException("{_LR}Not enough args.");
					} catch(NumberFormatException ex) {
						throw new CommandAnswerException("{_LR}Not a number: {_LS}" + args[0]);
					}
					try
					{
						tp.speedAfter = Float.parseFloat(args[1]);
					} catch(ArrayIndexOutOfBoundsException | NullPointerException ex) {
						throw new CommandAnswerException("{_LR}Not enough args.");
					} catch(NumberFormatException ex) {
						throw new CommandAnswerException("{_LR}Not a number: {_LS}" + args[1]);
					}
					tp.messageOnReach = GenericChatCodes.glue(Arrays.copyOfRange(args, 2, args.length), " ");
					pointList.add(tp);
					buffer.points = pointList.toArray(new TrajectoryPoint[pointList.size()]);
					buffer.setSelected(buffer.points.length - 1);
					throw new CommandAnswerException("Added! Selected point ID is #" + buffer.getSelected() + " (0..." + (buffer.points.length - 1) + ")");
				}
				break;
			case "position":
				if(checkAdminOnly(sender))
				{
					final TrajectoryPoint point = getSelectedPoint(sender);
					throw new CommandAnswerException("{_LG}Point has been edited. Don't forget to save your buffer.");
				}
				break;
			case "freeze":
				if(checkAdminOnly(sender))
				{
					final TrajectoryPoint point = getSelectedPoint(sender);
					try
					{
						point.freezeTicks = Integer.parseInt(args[0]);
					} catch(ArrayIndexOutOfBoundsException | NullPointerException ex) {
						throw new CommandAnswerException("{_LR}Not enough args.");
					} catch(NumberFormatException ex) {
						throw new CommandAnswerException("{_LR}Not a number: {_LS}" + args[0]);
					}
					throw new CommandAnswerException("{_LG}Point has been edited. Don't forget to save your buffer.");
				}
				break;
			case "speed":
				if(checkAdminOnly(sender))
				{
					final TrajectoryPoint point = getSelectedPoint(sender);
					try
					{
						point.speedAfter = Float.parseFloat(args[0]);
					} catch(ArrayIndexOutOfBoundsException | NullPointerException ex) {
						throw new CommandAnswerException("{_LR}Not enough args.");
					} catch(NumberFormatException ex) {
						throw new CommandAnswerException("{_LR}Not a number: {_LS}" + args[1]);
					}
					throw new CommandAnswerException("{_LG}Point has been edited. Don't forget to save your buffer.");
				}
				break;
			case "text":
				if(checkAdminOnly(sender))
				{
					final TrajectoryPoint point = getSelectedPoint(sender);
					point.messageOnReach = ChatColor.translateAlternateColorCodes('&', GenericChatCodes.glue(args, " "));
					throw new CommandAnswerException("{_LG}Point has been edited. Don't forget to save your buffer.");
				}
				break;
			case "titletime":
				if(checkAdminOnly(sender))
				{
					final TrajectoryPoint point = getSelectedPoint(sender);
					try
					{
						point.showTitleTicks = Integer.parseInt(args[0]);
					} catch(ArrayIndexOutOfBoundsException | NullPointerException ex) {
						throw new CommandAnswerException("{_LR}Not enough args.");
					} catch(NumberFormatException ex) {
						throw new CommandAnswerException("{_LR}Not a number: {_LS}" + args[0]);
					}
					throw new CommandAnswerException("{_LG}Point has been edited. Don't forget to save your buffer.");
				}
				break;
			case "title":
				if(checkAdminOnly(sender))
				{
					final TrajectoryPoint point = getSelectedPoint(sender);
					point.showTitle = ChatColor.translateAlternateColorCodes('&', GenericChatCodes.glue(args, " "));
					throw new CommandAnswerException("{_LG}Point has been edited. Don't forget to save your buffer.");
				}
				break;
			case "subtitle":
				if(checkAdminOnly(sender))
				{
					final TrajectoryPoint point = getSelectedPoint(sender);
					point.showSubtitle = ChatColor.translateAlternateColorCodes('&', GenericChatCodes.glue(args, " "));
					throw new CommandAnswerException("{_LG}Point has been edited. Don't forget to save your buffer.");
				}
				break;
			case "select":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final Trajectory buffer = plugin.getBufferedTrajectory(player);
					int pointID;
					try
					{
						pointID = Integer.parseInt(args[0]);
					} catch(ArrayIndexOutOfBoundsException | NullPointerException ex) {
						throw new CommandAnswerException("{_LR}Not enough args.");
					} catch(NumberFormatException ex) {
						throw new CommandAnswerException("{_LR}Not a number: {_LS}" + args[0]);
					}
					if(pointID >= 0 && pointID < buffer.points.length)
					{
						buffer.setSelected(pointID);
						player.setFlying(true);
						player.teleport(buffer.points[pointID].location);
						throw new CommandAnswerException("Selected #" + pointID + " and teleported to it");
					}
					throw new CommandAnswerException("{_LR}Out of range (0..." + (buffer.points.length - 1) + ").");
				}
				break;
			case "draw":
				throw new CommandAnswerException("{_LR}Still not supported.");
			case "time":
				throw new CommandAnswerException("{_LR}Still not supported.");
			case "weather":
				throw new CommandAnswerException("{_LR}Still not supported.");
			case "info":
				if(checkAdminOnly(sender))
				{
					final String  firstJoinCaption = plugin.trajMngr.getFirstJoinCaption();
					final boolean firstJoinLoaded  = plugin.trajMngr.contains(firstJoinCaption);
					final ArrayList<String> answer = new ArrayList<>();
					answer.add("Current configuration:");
					answer.add("firstJoinTrajectory: {_YL}" + firstJoinCaption);
					answer.add("firstJoinTrajectory is " + (firstJoinLoaded ? "{_DG}already loaded" : "{_DR}not loaded yet"));
					if(firstJoinLoaded)
						answer.add("firstJoinTrajectory contains points: {WHITE}" + plugin.trajMngr.get(firstJoinCaption).points.length);
					if(sender instanceof Player)
					{
						final Trajectory buffer = plugin.buffers.get((Player)sender);
						if(buffer != null && buffer.points.length > 0)
						{
							answer.add("Your have some trajectory points in your buffer: {WHITE}" + buffer.points.length);
							answer.add("Selected point ID is #" + buffer.getSelected() + " (in range 0..." + (buffer.points.length - 1) + ")");
						} else
							answer.add("Your have no trajectory points in your buffer");
					}
					throw new CommandAnswerException(answer);
				}
				break;
			case "play":
				if(checkAdminOnly(sender))
				{
					final Player player = (args[0] != null && !"".equals(args[0]))
						? plugin.getServer().getPlayer(args[0])
						: checkPlayerOnly(sender);
					if(player == null)
						throw new CommandAnswerException("{_LR}Cannot find such player.");
					if(plugin.buffers.containsKey(player))
						plugin.trajectoryPlayer.beginDemo(player, plugin.buffers.get(player));
					else
						if(plugin.trajMngr.lazyFirstJoinTrajectoryLoading())
							plugin.trajectoryPlayer.beginDemo(player, plugin.trajMngr.getFirstJoin());
					return;
				}
				break;
			case "stop":
				if(checkAdminOnly(sender))
				{
					final Player player = (args[0] != null && !"".equals(args[0]))
						? plugin.getServer().getPlayer(args[0])
						: checkPlayerOnly(sender);
					if(player == null)
						throw new CommandAnswerException("{_LR}Cannot find such player.");
					plugin.trajectoryPlayer.finishDemo(player);
					return;
				}
				break;
			case "help":
				if(checkAdminOnly(sender))
					throw new CommandAnswerException(new String[]
					{
						"Usage:",
						"{YELLOW}/rscfjd play [player name]",
						"{YELLOW}/rscfjd stop [player name]",
						"{YELLOW}/rscfjd add <freeze ticks> <speed after (bps)> [text w/formatting]",
						"{YELLOW}/rscfjd save [caption] {_LS}- save your buffer into file",
						"{YELLOW}/rscfjd load [caption] {_LS}- load file into your buffer",
						"{YELLOW}/rscfjd select <#> {_LS}- select point by id for editing and teleport you there.",
						"{YELLOW}/rscfjd position {_LS}- update selected point with your location.",
						"{YELLOW}/rscfjd freeze <ticks> {_LS}- update freezeTicks when reach selected point.",
						"{YELLOW}/rscfjd speed <blocksPerSec> {_LS}- update speed after selected point. Zero means teleport.",
						"{YELLOW}/rscfjd text [text] {_LS}- update messageOnReach of selected point.",
						"{YELLOW}/rscfjd title [text] {_LS}- update showTitle of selected point.",
						"{YELLOW}/rscfjd subtitle [text] {_LS}- update showSubtitle of selected point.",
						"{YELLOW}/rscfjd clear {_LS}- clears your buffer",
						"{YELLOW}/rscfjd reload",
					});
				break;
			case "reload":
				if(checkAdminOnly(sender))
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
	private boolean checkAdminOnly(CommandSender sender) throws CommandAnswerException
	{
		if(!sender.hasPermission("rscfjd.admin"))
			throw new CommandAnswerException("{_LR}Not enough permissions.");
		return true;
	}
	private Player checkPlayerOnly(CommandSender sender) throws CommandAnswerException
	{
		if(!(sender instanceof Player))
			throw new CommandAnswerException("{_LR}This command cannot be run from console.");
		return (Player)sender;
	}
	private TrajectoryPoint getSelectedPoint(CommandSender sender) throws CommandAnswerException
	{
		final Player player = checkPlayerOnly(sender);
		final Trajectory buffer = plugin.buffers.get(player);
		TrajectoryPoint result = buffer != null ? buffer.getSelectedPoint() : null;
		if(result == null)
				throw new CommandAnswerException("{_LR}Your buffer is empty! Add some points!");
		return result;
	}
}
