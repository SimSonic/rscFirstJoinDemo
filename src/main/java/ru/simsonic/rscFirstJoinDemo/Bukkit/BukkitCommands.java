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
import ru.simsonic.rscMinecraftLibrary.Bukkit.Tools;

public class BukkitCommands
{
	private final BukkitPluginMain plugin;
	public BukkitCommands(BukkitPluginMain plugin)
	{
		this.plugin = plugin;
	}
	public void execute(CommandSender sender, String[] args) throws CommandAnswerException
	{
		if(args.length == 0)
			throw new CommandAnswerException(Tools.getPluginWelcome(plugin, null));
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
					int selected = buffer.getSelected();
					if(selected < pointList.size() - 1)
					{
						pointList.add(selected + 1, tp);
						buffer.points = pointList.toArray(new TrajectoryPoint[pointList.size()]);
						buffer.setSelected(selected + 1);
					} else {
						pointList.add(tp);
						buffer.points = pointList.toArray(new TrajectoryPoint[pointList.size()]);
						buffer.setSelected(buffer.points.length - 1);
					}
					throw new CommandAnswerException("Added! Selected point ID is #" + buffer.getSelected() + " (0..." + (buffer.points.length - 1) + ")");
				}
				break;
			case "position":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final TrajectoryPoint point = getSelectedPoint(sender);
					point.updateLocation(player.getLocation());
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
						player.setAllowFlight(true);
						player.setFlying(true);
						player.teleport(buffer.points[pointID].location);
						player.setAllowFlight(true);
						player.setFlying(true);
						final ArrayList<String> answer = new ArrayList<>();
						answer.add("Selected #" + pointID + " and teleported to it.");
						answer.add("{_DP}Selected point info:");
						answer.addAll(getPointProps(buffer.getSelectedPoint()));
						throw new CommandAnswerException(answer);
					}
					throw new CommandAnswerException("{_LR}Out of range (0..." + (buffer.points.length - 1) + ").");
				}
				break;
			case "delete":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final Trajectory buffer = plugin.getBufferedTrajectory(player);
					final TrajectoryPoint point = getSelectedPoint(sender);
					final int selected = buffer.getSelected();
					final int before = selected;
					final int after = buffer.points.length - selected - 1;
					final ArrayList<TrajectoryPoint> newPoints = new ArrayList<>();
					if(before > 0)
						newPoints.addAll(Arrays.asList(Arrays.copyOfRange(buffer.points, 0, selected)));
					if(after > 0)
						newPoints.addAll(Arrays.asList(Arrays.copyOfRange(buffer.points, selected + 1, buffer.points.length)));
					buffer.points = newPoints.toArray(new TrajectoryPoint[newPoints.size()]);
					buffer.setSelected(after == 0 ? selected - 1 : selected);
					throw new CommandAnswerException(new String[]
					{
						"{_LG}Point #" + selected + " has been deleted.",
						buffer.points.length > 0
							? "Now you have " + buffer.points.length + " points in your buffer."
							: "Your buffer is empty now.",
						buffer.points.length > 0
							? "New selected point ID is #" + buffer.getSelected() + " (in range 0..." + (buffer.points.length - 1) + ")"
							: null,
					});
				}
				break;
			case "merge":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final Trajectory buffer = plugin.getBufferedTrajectory(player);
					if(args[0] != null && !"".equals(args[0]))
					{
						final Trajectory merged = plugin.trajMngr.loadTrajectory(args[0]);
						final ArrayList<TrajectoryPoint> newPoints = new ArrayList<>();
						newPoints.addAll(Arrays.asList(buffer.points));
						newPoints.addAll(Arrays.asList(merged.points));
						buffer.points = newPoints.toArray(new TrajectoryPoint[newPoints.size()]);
						throw new CommandAnswerException("{_LG}Complete! Length of your buffer now is " + buffer.points.length + " points.");
					}
					throw new CommandAnswerException("{_LR}Require merging trajectory caption.");
				}
				break;
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
					answer.add("{_DP}Server configuration:");
					answer.add("firstJoinTrajectory: {RESET}" + firstJoinCaption);
					answer.add("firstJoinTrajectory is " + (firstJoinLoaded ? "{_DG}already loaded" : "{_DR}not loaded yet"));
					if(firstJoinLoaded)
						answer.add("firstJoinTrajectory contains points: {RESET}" + plugin.trajMngr.get(firstJoinCaption).points.length);
					if(sender instanceof Player)
					{
						final Trajectory buffer = plugin.buffers.get((Player)sender);
						if(buffer != null && buffer.points.length > 0)
						{
							answer.add("{_DP}Your buffer state:");
							answer.add("Your have some trajectory points in your buffer: {RESET}" + buffer.points.length);
							answer.add("Selected point ID is #" + buffer.getSelected() + " (in range 0..." + (buffer.points.length - 1) + ")");
							answer.add("{_DP}Selected point info:");
							final TrajectoryPoint point = getSelectedPoint(sender);
							answer.addAll(getPointProps(point));
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
					{
						plugin.trajectoryPlayer.beginDemo(player, plugin.buffers.get(player));
						throw new CommandAnswerException("{_LG}Done (using player buffer).");
					} else
						if(plugin.trajMngr.lazyFirstJoinTrajectoryLoading())
						{
							plugin.trajectoryPlayer.beginDemo(player, plugin.trajMngr.getFirstJoin());
							throw new CommandAnswerException("{_LG}Done (using first-join trajectory).");
						}
					throw new CommandAnswerException("{_LR}There is nothing in demo to play.");
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
			case "draw":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final Trajectory buffer = plugin.getBufferedTrajectory(player);
					if(buffer.drawSketch)
					{
						buffer.drawSketch = false;
						// Disable sketch drawing -- TO DO HERE
						throw new CommandAnswerException("{_LR}Still not supported.");
					} else {
						buffer.drawSketch = true;
						// Enable sketch drawing -- TO DO HERE
						throw new CommandAnswerException("{_LR}Still not supported.");
					}
				}
				break;
			case "help":
				if(checkAdminOnly(sender))
					throw new CommandAnswerException(new String[]
					{
						"Usage:",
						"{YELLOW}/rscfjd play [player name] {_LS}- start buffer (or first-join demo) for player",
						"{YELLOW}/rscfjd stop [player name] {_LS}- cancel any demo playing for you or other player",
						"{YELLOW}/rscfjd add <freeze ticks> <speed after (bps)> [text w/formatting] {_LS}- add new point after current and select it",
						"{YELLOW}/rscfjd save [caption] {_LS}- save your buffer into file",
						"{YELLOW}/rscfjd load [caption] {_LS}- load file into your buffer",
						"{YELLOW}/rscfjd select <#> {_LS}- select point by id for editing and teleport you there.",
						"{YELLOW}/rscfjd position {_LS}- update selected point with your location.",
						"{YELLOW}/rscfjd freeze <ticks> {_LS}- update freezeTicks when reach selected point.",
						"{YELLOW}/rscfjd speed <blocksPerSec> {_LS}- update speed after selected point. Zero means teleport.",
						"{YELLOW}/rscfjd text [text] {_LS}- update messageOnReach of selected point.",
						"{YELLOW}/rscfjd titletime <ticks> {_LS}- update showTitleTicks of selected point.",
						"{YELLOW}/rscfjd title [text] {_LS}- update showTitle of selected point.",
						"{YELLOW}/rscfjd subtitle [text] {_LS}- update showSubtitle of selected point.",
						"{YELLOW}/rscfjd merge <caption>{_LS}- add another trajectory to the end of your buffer.",
						"{YELLOW}/rscfjd delete {_LS}- remove selected point.",
						// "{YELLOW}/rscfjd draw {_LS}- toggle showing of buffered trajectory as a 3D line.",
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
			default:
				throw new CommandAnswerException("{_LR}Unknown subcommand.");
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
		final TrajectoryPoint result = buffer != null ? buffer.getSelectedPoint() : null;
		if(result == null)
			throw new CommandAnswerException("{_LR}Your buffer is empty! Add some points first!");
		return result;
	}
	private ArrayList<String> getPointProps(TrajectoryPoint point)
	{
		final ArrayList<String> result = new ArrayList<>();
		if(point != null)
		{
			result.add("Position: {RESET}[" + point.location.getBlockX() + "; " + point.location.getBlockY() + "; " + point.location.getBlockZ() + "]");
			result.add("FreezeTime (ticks): {RESET}" + point.freezeTicks);
			result.add("SpeedAfter (blocks/sec): {RESET}" + point.speedAfter);
			result.add("MessageOnReach: {RESET}" + point.messageOnReach);
			if(point.showTitle != null && !"".equals(point.showTitle))
				result.add("Title: {RESET}" + point.showTitle);
			if(point.showSubtitle != null && !"".equals(point.showSubtitle))
				result.add("Subtitle: {RESET}" + point.showSubtitle);
			result.add("Title timer (ticks): {RESET}" + point.showTitleTicks);
		}
		return result;
	}
}
