package ru.simsonic.rscFirstJoinDemo.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.simsonic.rscFirstJoinDemo.API.Trajectory;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;
import ru.simsonic.rscFirstJoinDemo.BukkitPluginMain;
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
					final Trajectory buffer = (args[0] != null && !"".equals(args[0]))
						? plugin.trajMngr.loadTrajectory(args[0])
						: plugin.trajMngr.loadBufferTrajectory(player);
					plugin.setBufferedTrajectory(player, buffer);
					if(buffer.points.length > 0)
					{
						final ArrayList<String> answer = setSelectedPoint(player, buffer, buffer.points.length - 1);
						answer.add(0, "Loaded trajectory: {RESET}" + buffer.points.length + " points.");
						throw new CommandAnswerException(answer);
					}
					throw new CommandAnswerException("Loaded empty trajectory (0 points)");
				}
				break;
			case "save":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final Trajectory trajectory = plugin.getBufferedTrajectory(player);
					if(args[0] != null && !"".equals(args[0]))
					{
						plugin.trajMngr.saveTrajectory(trajectory, args[0]);
						throw new CommandAnswerException("Saved {_LC}" + args[0] + ".json{_LG}.");
					}
					plugin.trajMngr.saveBufferTrajectory(trajectory, player);
					throw new CommandAnswerException("Buffer successfully saved.");
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
					int pointID = buffer.getSelected();
					try
					{
						pointID = Integer.parseInt(args[0]);
					} catch(ArrayIndexOutOfBoundsException | NullPointerException ex) {
						throw new CommandAnswerException("{_LR}Not enough args.");
					} catch(NumberFormatException ex) {
						if(args[0] != null)
							throw new CommandAnswerException("{_LR}Not a number: {_LS}" + args[0]);
					}
					if(pointID >= 0 && pointID < buffer.points.length)
						throw new CommandAnswerException(setSelectedPoint(player, buffer, pointID));
					throw new CommandAnswerException("{_LR}Out of range (0..." + (buffer.points.length - 1) + ").");
				}
				break;
			case "next":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final Trajectory buffer = plugin.getBufferedTrajectory(player);
					if(buffer.points.length == 0)
						throw new CommandAnswerException("{_LR}Your buffer is empty! Add some points first!");
					int pointID = buffer.getSelected() + 1;
					if(pointID == buffer.points.length)
						throw new CommandAnswerException("{_LR}This is the last point in your buffer!");
					throw new CommandAnswerException(setSelectedPoint(player, buffer, pointID));
				}
				break;
			case "prev":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final Trajectory buffer = plugin.getBufferedTrajectory(player);
					if(buffer.points.length == 0)
						throw new CommandAnswerException("{_LR}Your buffer is empty! Add some points first!");
					int pointID = buffer.getSelected();
					if(pointID == 0)
						throw new CommandAnswerException("{_LR}This is the first point in your buffer!");
					throw new CommandAnswerException(setSelectedPoint(player, buffer, pointID - 1));
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
			case "time":
				if(checkAdminOnly(sender))
				{
					final TrajectoryPoint point = getSelectedPoint(sender);
					if(args[0] != null && !"".equals(args[0]))
					{
						switch(args[0].toLowerCase())
						{
							case "reset":
								point.timeReset  = true;
								point.timeUpdate = false;
								break;
							case "lock":
								try
								{
									if(args[1].equals("now"))
										point.timeUpdateValue = ((Player)sender).getWorld().getTime();
									else
										point.timeUpdateValue = Long.parseLong(args[1]);
									point.timeUpdate      = true;
									point.timeUpdateLock  = true;
								} catch(NumberFormatException ex) {
									throw new CommandAnswerException("{_LR}Wrong command. Read help, please.");
								}
								break;
							case "unlock":
								try
								{
									if(args[1].equals("now"))
										point.timeUpdateValue = ((Player)sender).getWorld().getTime();
									else
										point.timeUpdateValue = Long.parseLong(args[1]);
									point.timeUpdate      = true;
									point.timeUpdateLock  = false;
								} catch(NumberFormatException ex) {
									throw new CommandAnswerException("{_LR}Wrong command. Read help, please.");
								}
								break;
							default:
								throw new CommandAnswerException("{_LR}Wrong command. Read help, please.");
						}
						throw new CommandAnswerException("{_LG}Done.");
					}
					throw new CommandAnswerException("{_LR}Wrong command. Read help, please.");
				}
				break;
			case "weather":
				if(checkAdminOnly(sender))
				{
					final TrajectoryPoint point = getSelectedPoint(sender);
					if(args[0] != null && !"".equals(args[0]))
					{
						switch(args[0].toLowerCase())
						{
							case "reset":
								point.weatherReset  = true;
								point.weatherUpdate = false;
								break;
							case "sunny":
								point.weatherReset        = false;
								point.weatherUpdate       = true;
								point.weatherUpdateStormy = false;
								break;
							case "stormy":
								point.weatherReset        = false;
								point.weatherUpdate       = true;
								point.weatherUpdateStormy = true;
								break;
							default:
								throw new CommandAnswerException("{_LR}Wrong command. Read help, please.");
						}
						throw new CommandAnswerException("{_LG}Done.");
					}
					throw new CommandAnswerException("{_LR}Wrong command. Read help, please.");
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
			case "permission":
				if(checkAdminOnly(sender))
				{
					final Player player = checkPlayerOnly(sender);
					final Trajectory buffer = plugin.getBufferedTrajectory(player);
					if(args[0] != null && !"".equals(args[0]))
					{
						buffer.requiredPermission = args[0];
						throw new CommandAnswerException("{_LG}New trajectory-specific permission: {_R}" + buffer.requiredPermission);
					}
					buffer.requiredPermission = null;
					throw new CommandAnswerException("{_YL}Removed trajectory-specific permission.");
				}
				break;
			case "info":
				if(checkAdminOnly(sender))
				{
					final ArrayList<String> answer = new ArrayList<>();
					Trajectory trajectory = null;
					if(args[0] != null && !"".equals(args[0]))
					{
						trajectory = plugin.trajMngr.loadTrajectory(args[0]);
						if(trajectory == null)
							answer.add("{_LR}There is no trajectory with this name: " + args[0]);
					}
					final String  firstJoinCaption = plugin.settings.getFirstJoinTrajectory();
					final boolean firstJoinLoaded  = plugin.trajMngr.contains(firstJoinCaption);
					answer.add("{_DP}Server configuration:");
					answer.add("first-join-trajectory: {_R}" + firstJoinCaption);
					answer.add("first-join-trajectory is " + (firstJoinLoaded ? "{_DG}already loaded" : "{_DR}not loaded yet"));
					if(firstJoinLoaded)
						answer.add("first-join-trajectory contains points: {_R}" + plugin.trajMngr.get(firstJoinCaption).points.length);
					if(trajectory == null)
					{
						if(sender instanceof Player)
						{
							trajectory = plugin.playerBuffers.get((Player)sender);
							if(trajectory != null)
								answer.add("{_DP}Your's buffer state:");
							else
								answer.add("Your have no trajectory points in your buffer");
						} else
							trajectory = plugin.trajMngr.getFirstJoinTrajectory();
					}
					if(trajectory != null && trajectory.points.length > 0)
						answer.addAll(getTrajectoryProps(sender, trajectory));
					throw new CommandAnswerException(answer);
				}
				break;
			case "play":
				if(checkAdminOnly(sender))
				{
					Player     target;
					Trajectory trajectory = null;
					if(args[0] != null && !"".equals(args[0]))
					{
						target = plugin.getServer().getPlayer(args[0]);
						if(target == null)
							throw new CommandAnswerException("{_LR}Cannot find such player.");
						if(args[1] != null && !"".equals(args[1]))
							trajectory = plugin.trajMngr.loadTrajectory(args[1]);
					} else {
						target = checkPlayerOnly(sender);
					}
					if(target == null && sender instanceof Player)
					{
						final Player player = (Player)sender;
						target = player;
						if(plugin.playerBuffers.containsKey(player))
							trajectory = plugin.playerBuffers.get(player);
					}
					if(trajectory == null)
						trajectory = plugin.trajMngr.getFirstJoinTrajectory();
					if(trajectory != null)
					{
						plugin.trajectoryPlayer.beginDemo(target, trajectory);
						throw new CommandAnswerException("{_LG}Demo " + trajectory.caption + " has been started.");
					}
					throw new CommandAnswerException("{_LR}{_B}Internal error.");
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
						"{YELLOW}/rscfjd play [<player> [caption]] {_LS}- start specific/first-join demo playing for you/player.",
						"{YELLOW}/rscfjd stop [player] {_LS}- cancel demo playing for you/player.",
						"{YELLOW}/rscfjd add <freezeTicks> <speedAfter> [text] {_LS}- add new point after current and select it.",
						"{YELLOW}/rscfjd select [#] {_LS}- [re]select point by id for editing and teleport you there.",
						"{YELLOW}/rscfjd next {_LS}- select next point in your buffer.",
						"{YELLOW}/rscfjd prev {_LS}- select previous point in your buffer.",
						"{YELLOW}/rscfjd position {_LS}- update selected point with your location.",
						"{YELLOW}/rscfjd freeze <ticks> {_LS}- update freezeTicks when reach selected point.",
						"{YELLOW}/rscfjd speed <blocksPerSec> {_LS}- update speed after selected point. Zero means teleport.",
						"{YELLOW}/rscfjd text [text] {_LS}- update messageOnReach of selected point.",
						"{YELLOW}/rscfjd titletime <ticks> {_LS}- update showTitleTicks of selected point.",
						"{YELLOW}/rscfjd title [text] {_LS}- update showTitle of selected point.",
						"{YELLOW}/rscfjd subtitle [text] {_LS}- update showSubtitle of selected point.",
						"{YELLOW}/rscfjd time <reset|lock <value|now>|unlock <value|now>> {_LS}- setup point's time.",
						"{YELLOW}/rscfjd weather <reset|sunny|stormy> {_LS}- setup point's weather.",
						"{YELLOW}/rscfjd merge <caption> {_LS}- add another trajectory to the end of your buffer.",
						"{YELLOW}/rscfjd delete {_LS}- remove selected point.",
						"{YELLOW}/rscfjd info {_LS}- show info about server settings, your buffer and selected point.",
						// "{YELLOW}/rscfjd draw {_LS}- toggle showing of buffered trajectory as a 3D line.",
						"{YELLOW}/rscfjd clear {_LS}- clear your buffer",
						"{YELLOW}/rscfjd permission [permission] {_LS}- set/clear permission required to use it by sign.",
						"{YELLOW}/rscfjd save [caption] {_LS}- save your buffer into file.",
						"{YELLOW}/rscfjd load [caption] {_LS}- load file into your buffer.",
						"{YELLOW}/rscfjd help {_LS}- show this help page.",
						"{YELLOW}/rscfjd reload {_LS}- restart this plugin and reread configuration.",
						"{YELLOW}/rscfjd update {_LS}- download and install new version.",
					});
				break;
			case "reload":
				if(checkAdminOnly(sender))
				{
					plugin.reloadConfig();
					plugin.getPluginLoader().disablePlugin(plugin);
					plugin.getPluginLoader().enablePlugin(plugin);
					plugin.getServer().getConsoleSender().sendMessage("[rscfjd] rscFirstJoinDemo has been reloaded.");
					if(sender instanceof Player)
						throw new CommandAnswerException("{_LG}Plugin has been reloaded.");
					return;
				}
				break;
			case "update":
				if(checkAdminOnly(sender))
				{
					plugin.updating.onDoUpdate(sender);
					if(sender instanceof Player)
						throw new CommandAnswerException("{_LG}Done.");
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
	public ArrayList<String> setSelectedPoint(Player player, Trajectory buffer, int pointID)
	{
		return setSelectedPoint(player, buffer, pointID, true);
	}
	public ArrayList<String> setSelectedPoint(Player player, Trajectory buffer, int pointID, boolean teleport)
	{
		buffer.setSelected(pointID);
		final ArrayList<String> result = new ArrayList<>();
		if(teleport)
		{
			player.setAllowFlight(true);
			player.setFlying(true);
			player.teleport(buffer.points[pointID].location);
			player.setAllowFlight(true);
			player.setFlying(true);
			result.add("Selected #" + pointID + " and teleported to it.");
		} else
			result.add("Selected #" + pointID + ".");
		result.add("{_DP}Selected point info:");
		result.addAll(getPointProps(buffer.getSelectedPoint()));
		return result;
	}
	private TrajectoryPoint getSelectedPoint(CommandSender sender) throws CommandAnswerException
	{
		final Player player = checkPlayerOnly(sender);
		final Trajectory buffer = plugin.playerBuffers.get(player);
		final TrajectoryPoint result = buffer != null ? buffer.getSelectedPoint() : null;
		if(result == null)
			throw new CommandAnswerException("{_LR}Your buffer is empty! Add some points first!");
		return result;
	}
	private ArrayList<String> getTrajectoryProps(CommandSender sender, Trajectory trajectory) throws CommandAnswerException
	{
		final ArrayList<String> result = new ArrayList<>();
		result.add("Trajectory caption: " + trajectory.caption);
		if(trajectory.requiredPermission != null && !"".equals(trajectory.requiredPermission))
			result.add("Trajectory-specific permission: " + trajectory.requiredPermission);
		result.add("Trajectory points: {RESET}" + trajectory.points.length);
		if(sender instanceof Player)
		{
			result.add("Selected point ID is #" + trajectory.getSelected() + " (in range 0..." + (trajectory.points.length - 1) + ")");
			result.add("{_DP}Selected point info:");
			final TrajectoryPoint point = getSelectedPoint(sender);
			result.addAll(getPointProps(point));
		}
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
