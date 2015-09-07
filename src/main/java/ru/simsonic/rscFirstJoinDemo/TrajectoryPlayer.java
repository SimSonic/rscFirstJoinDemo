package ru.simsonic.rscFirstJoinDemo;

import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public class TrajectoryPlayer
{
	private final BukkitPluginMain plugin;
	TrajectoryPlayer(BukkitPluginMain plugin)
	{
		this.plugin = plugin;
	}
	public void beginDemo(final Player player, final Trajectory trajectory)
	{
		// Cancel old demo if it was
		finishDemo(player);
		// Hide player from all
		for(Player online : plugin.getServer().getOnlinePlayers())
			online.hidePlayer(player);
		try
		{
			// Start flight
			final TrajectoryPlayState tps = trajectory.newPlayState();
			if(tps.trajectory.points.length <= 0)
			{
				BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Cannot run demo for {0}, it is empty.", player.getName());
				return;
			}
			// Does this server support SPECTATOR mode?
			tps.supportSpectatorMode = false;
			for(GameMode gm : GameMode.values())
				if(gm.toString().equalsIgnoreCase("SPECTATOR"))
					tps.supportSpectatorMode = true;
			// Integrate with PlaceholderAPI
			final Plugin  placeholder = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
			tps.foundPlaceholderAPI = (placeholder != null && placeholder.isEnabled());
			if(tps.foundPlaceholderAPI)
				BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] I will use PlaceholderAPI for pre-processing text/titles");
			// Integrate with ProtocolLib 3.6.4+ to show titles!
			final Plugin protocolLib = plugin.getServer().getPluginManager().getPlugin("ProtocolLib");
			tps.foundProtocolLib = (protocolLib != null && protocolLib.isEnabled());
			BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] ProtocolLib is {0}",
				tps.foundProtocolLib ? "found." : "not found. Titles are disabled.");
			// Other setup
			tps.currentPoint = -1;
			tps.originalFlightAllow = player.getAllowFlight();
			tps.originalFlightState = player.isFlying();
			tps.originalGameMode = player.getGameMode();
			player.setAllowFlight(true);
			player.setFlying(true);
			player.setPlayerWeather(WeatherType.CLEAR);
			if(tps.supportSpectatorMode)
				player.setGameMode(GameMode.SPECTATOR);
			plugin.playStates.put(player, tps);
			tps.scheduledTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					tps.localTick += 1;
					processDemoStep(player, tps);
				}
			}, 1, 1);
			BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Starting playing demo '{0}' to {1}",
				new Object[] { tps.trajectory.caption, player.getName() });
		} catch(RuntimeException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Demo starting error: {0}", ex);
		}
	}
	public void finishDemo(Player player)
	{
		try
		{
			final TrajectoryPlayState tps = plugin.playStates.get(player);
			if(tps != null)
			{
				player.setGameMode(tps.originalGameMode);
				player.setAllowFlight(tps.originalFlightAllow);
				player.setFlying(tps.originalFlightState);
				player.setFallDistance(0.0f);
				player.setVelocity(new Vector());
				player.resetPlayerWeather();
				player.resetPlayerTime();
				plugin.getServer().getScheduler().cancelTask(tps.scheduledTaskId);
				final int points = tps.trajectory.points.length;
				if(points > 0)
				{
					player.teleport(tps.trajectory.points[points - 1].location);
					player.saveData();
				}
				plugin.playStates.remove(player);
				BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Finished playing demo to {0}", player.getName());
			}
			for(Player online : plugin.getServer().getOnlinePlayers())
				online.showPlayer(player);
		} catch(RuntimeException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Demo stopping error: {0}", ex);
		}
	}
	private void processDemoStep(final Player player, final TrajectoryPlayState tps)
	{
		if(!player.isOnline())
		{
			finishDemo(player);
			return;
		}
		try
		{
			// Are we in the beginning?
			if(tps.started == false)
			{
				tps.currentPointStartTick = tps.localTick;
				tps.started = true;
			}
			// Is current segment finished?
			if(tps.localTick - tps.currentPointStartTick >= tps.currentSegmentFlightTime)
				onCurrentSegmentFinished(player, tps);
			// Process next step on the current point
			if(tps.currentPoint < tps.trajectory.points.length)
				onMakingNextStep(player, tps);
		} catch(RuntimeException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Demo processing error: {0}", ex);
			finishDemo(player);
		}
	}
	private void onCurrentSegmentFinished(final Player player, final TrajectoryPlayState tps)
	{
		// I should look for next point with location != null
		for(tps.currentPoint += 1; tps.currentPoint < tps.trajectory.points.length; tps.currentPoint += 1)
			if(tps.trajectory.points[tps.currentPoint].location != null)
				break;
		// Was it the last segment?
		if(tps.currentPoint >= tps.trajectory.points.length)
		{
			finishDemo(player);
			return;
		}
		tps.currentPointStartTick = tps.localTick;
		// Isn't it the last point?
		final int nextPossiblePoint = tps.currentPoint + 1;
		final TrajectoryPoint tp1 = tps.trajectory.points[tps.currentPoint];
		final TrajectoryPoint tp2 = (nextPossiblePoint < tps.trajectory.points.length)
			? tps.trajectory.points[nextPossiblePoint]
			: null;
		tps.currentSegmentFlightTime = calculateFlightTime(tp1, tp2);
		tps.currentSegmentDeltaYaw = calculateYawDelta(tp1, tp2);
		// Log into console about this event
		BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Player {0} has reached {1} #{2}", new Object[]
		{
			player.getName(),
			tps.trajectory.caption != null ? tps.trajectory.caption : "his buffer",
			tps.currentPoint,
		});
		// Message on point reach
		if(tp1.messageOnReach != null && !"".equals(tp1.messageOnReach))
		{
			String text = GenericChatCodes.processStringStatic(tp1.messageOnReach);
			if(tps.foundPlaceholderAPI)
				text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
			player.sendMessage(text);
		}
		// Time and weather tricks
		if(tp1.timeReset)
			player.resetPlayerTime();
		else
			if(tp1.timeUpdate)
				player.setPlayerTime(tp1.timeUpdateValue, !tp1.timeUpdateLock);
		if(tp1.weatherReset)
			player.resetPlayerWeather();
		else
			if(tp1.weatherUpdate)
				player.setPlayerWeather(tp1.weatherUpdateStormy ? WeatherType.DOWNFALL : WeatherType.CLEAR);
		// Title/subtitle on point reach
		if(tps.foundProtocolLib)
		{
			String title = tp1.showTitle != null && !"".equals(tp1.showTitle)
				? GenericChatCodes.processStringStatic(tp1.showTitle)
				: "";
			String subtitle = tp1.showSubtitle != null && !"".equals(tp1.showSubtitle)
				? GenericChatCodes.processStringStatic(tp1.showSubtitle)
				: "";
			if(tps.foundPlaceholderAPI)
			{
				title = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, title);
				subtitle = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, subtitle);
			}
			try
			{
				if(!"".equals(title) || !"".equals(subtitle))
					sendTitle(player, title, subtitle, 20, tp1.showTitleTicks, 20);
			} catch(Exception ex) {
				BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] ProtocolLib error, disabling titles.\n{0}", ex);
				tps.foundProtocolLib = false;
			}
		}
	}
	public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) throws Exception
	{
		final com.comphenix.protocol.ProtocolManager protocolMan
			= com.comphenix.protocol.ProtocolLibrary.getProtocolManager();
		final com.comphenix.protocol.PacketType packetType
			= com.comphenix.protocol.PacketType.Play.Server.TITLE;
		// Send timings
		final com.comphenix.protocol.events.PacketContainer pTimeTitle
			= protocolMan.createPacket(packetType);
		if(stay <= 0)
			stay = 20;
		pTimeTitle.getIntegers().
			write(0, fadeIn).
			write(1, stay).
			write(2, fadeOut);
		pTimeTitle.getTitleActions().write(0,
			com.comphenix.protocol.wrappers.EnumWrappers.TitleAction.TIMES);
		protocolMan.sendServerPacket(player, pTimeTitle);
		// Prepare and send subtitle
		if("".equals(subtitle))
			subtitle = ChatColor.RESET.toString();
		final com.comphenix.protocol.events.PacketContainer pSubTitle
			= protocolMan.createPacket(packetType);
		pSubTitle.getChatComponents().write(0,
			com.comphenix.protocol.wrappers.WrappedChatComponent.fromText(subtitle));
		pSubTitle.getTitleActions().write(0,
			com.comphenix.protocol.wrappers.EnumWrappers.TitleAction.SUBTITLE);
		protocolMan.sendServerPacket(player, pSubTitle);
		// Prepare and send title
		if("".equals(title))
			title = ChatColor.RESET.toString();
		final com.comphenix.protocol.events.PacketContainer pTitle
			= protocolMan.createPacket(packetType);
		pTitle.getChatComponents().write(0,
			com.comphenix.protocol.wrappers.WrappedChatComponent.fromText(title));
		pTitle.getTitleActions().write(0,
			com.comphenix.protocol.wrappers.EnumWrappers.TitleAction.TITLE);
		protocolMan.sendServerPacket(player, pTitle);
	}
	private void onMakingNextStep(final Player player, final TrajectoryPlayState tps)
	{
		final TrajectoryPoint tp1 = tps.trajectory.points[tps.currentPoint];
		long currentSegmentTimeSpent = tps.localTick - tps.currentPointStartTick;
		// Teleport player to the next position
		if(currentSegmentTimeSpent >= tp1.freezeTicks && (tps.currentPoint + 1) < tps.trajectory.points.length)
		{
			currentSegmentTimeSpent -= tp1.freezeTicks;
			final TrajectoryPoint tp2 = tps.trajectory.points[tps.currentPoint + 1];
			final double percent = (tps.currentSegmentFlightTime != 0)
				? currentSegmentTimeSpent * 1.0 / tps.currentSegmentFlightTime
				: 1.0;
			final Location target = tp2.location.clone();
			final World w1 = tp1.location.getWorld();
			final World w2 = tp2.location.getWorld();
			if(w1 != null && w2 != null && w1.equals(w2))
			{
				// Find position
				target.subtract(tp1.location).multiply(percent).add(tp1.location);
				// Find rotation
				final float fp1 = tp1.location.getPitch(), fy1 = tp1.location.getYaw();
				target.setPitch((float)(fp1 + percent * (tp2.location.getPitch() - fp1)));
				target.setYaw((float)(fy1 + percent * tps.currentSegmentDeltaYaw));
			}
			// Teleport
			player.teleport(target, TeleportCause.PLUGIN);
		} else {
			player.teleport(tp1.location, TeleportCause.PLUGIN);
			player.setAllowFlight(true);
			player.setFlying(true);
			if(tps.supportSpectatorMode)
				player.setGameMode(GameMode.SPECTATOR);
		}
	}
	private long calculateFlightTime(TrajectoryPoint tp1, TrajectoryPoint tp2)
	{
		long result = tp1.freezeTicks;
		if(tp1.speedAfter > 0.005f && tp2 != null)
		{
			final Location l1 = tp1.location;
			final Location l2 = tp2.location;
			if(l1 != null && l2 != null)
			{
				final World w1 = l1.getWorld();
				final World w2 = l2.getWorld();
				if(w1 != null && w2 != null && w1.equals(w2))
					result += Math.floor(l1.distance(l2) * 20.0 / tp1.speedAfter) + 2;
			}
		}
		return result;
	}
	private float calculateYawDelta(TrajectoryPoint tp1, TrajectoryPoint tp2)
	{
		if(tp2 == null)
			return 0.0f;
		float result = tp2.location.getYaw() - tp1.location.getYaw();
		if(result >= 180.0f)
			result -= 360.0f;
		if(result <= -180.0f)
			result += 360.0f;
		return result;
	}
}
