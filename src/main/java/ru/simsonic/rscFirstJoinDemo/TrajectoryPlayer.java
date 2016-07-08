package ru.simsonic.rscFirstJoinDemo;

import java.util.logging.Level;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import ru.simsonic.rscFirstJoinDemo.API.Settings;
import ru.simsonic.rscFirstJoinDemo.API.Trajectory;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;
import ru.simsonic.rscFirstJoinDemo.Bukkit.IntegrationMan;
import ru.simsonic.rscFirstJoinDemo.Bukkit.TrajectoryPlayState;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public class TrajectoryPlayer
{
	private final BukkitPluginMain plugin;
	TrajectoryPlayer(BukkitPluginMain plugin)
	{
		this.plugin = plugin;
	}
	public void resumeDemo(final Player player, final Trajectory trajectory)
	{
		try
		{
			final TrajectoryPlayState tps = prepareDemo(player, trajectory);
			tps.currentPoint = tps.trajectory.selected > 1
				? tps.trajectory.selected - 2
				: -1;
		} catch(RuntimeException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, Settings.CHAT_PREFIX + "Demo resuming error: {0}", ex);
		}
	}
	public void beginDemo(final Player player, final Trajectory trajectory)
	{
		try
		{
			final TrajectoryPlayState tps = prepareDemo(player, trajectory);
			tps.currentPoint = -1;
			if(plugin.settings.getLogStartStop())
				BukkitPluginMain.consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + Phrases.DEMO_STARTING,
					new Object[] { tps.trajectory.caption, player.getName() });
		} catch(RuntimeException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, Settings.CHAT_PREFIX + "Demo starting error: {0}", ex);
		}
	}
	public void suspendDemo(Player player)
	{
		try
		{
			final TrajectoryPlayState tps = plugin.playStates.get(player);
			if(tps != null)
			{
				plugin.getServer().getScheduler().cancelTask(tps.scheduledTaskId);
				player.setGameMode   (tps.originalGameMode);
				player.setAllowFlight(tps.originalFlightAllow);
				player.setFlying     (tps.originalFlightState);
				player.setFallDistance(0.0f);
				player.setVelocity(new Vector());
				player.resetPlayerWeather();
				player.resetPlayerTime();
				plugin.playStates.remove(player);
				if(plugin.settings.getLogStartStop() && tps.foundNoCheatPlus)
					BukkitPluginMain.consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + Phrases.NCP_RESTORE, player.getName());
				plugin.intergts.cancelExemptNCP(player);
			}
			for(Player online : plugin.getServer().getOnlinePlayers())
				online.showPlayer(player);
		} catch(RuntimeException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, Settings.CHAT_PREFIX + "Demo pausing error: {0}", ex);
		}
	}
	public void finishDemo(Player player)
	{
		try
		{
			final TrajectoryPlayState tps = plugin.playStates.get(player);
			if(tps != null)
			{
				suspendDemo(player);
				final int points = tps.trajectory.points.length;
				if(points > 0)
				{
					player.teleport(tps.trajectory.points[points - 1].location);
					player.saveData();
				}
				if(plugin.settings.getLogStartStop())
					BukkitPluginMain.consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + Phrases.DEMO_STOPPING,
						player.getName());
			}
		} catch(RuntimeException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, Settings.CHAT_PREFIX + "Demo stopping error: {0}", ex);
		}
	}
	private TrajectoryPlayState prepareDemo(final Player player, final Trajectory trajectory)
	{
		// Cancel old demo if it was
		finishDemo(player);
		// Hide player from all
		for(Player online : plugin.getServer().getOnlinePlayers())
			online.hidePlayer(player);
		// Start flight
		final TrajectoryPlayState tps = trajectory.newPlayState();
		if(tps.trajectory.points.length <= 0)
		{
			BukkitPluginMain.consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + Phrases.DEMO_EMPTY,
				player.getName());
			throw new RuntimeException("Demo is empty.");
		}
		// Does this server support SPECTATOR mode?
		tps.supportSpectatorMode = false;
		for(GameMode gm : GameMode.values())
			if(gm.name().equalsIgnoreCase("SPECTATOR"))
				tps.supportSpectatorMode = true;
		// Integrate with other plugins
		tps.foundPlaceholderAPI = plugin.intergts.isPlaceholderAPI();
		tps.foundProtocolLib    = plugin.intergts.isProtocolLib();
		tps.foundNoCheatPlus    = plugin.intergts.isNoCheatPlus();
		if(plugin.settings.getLogStartStop() && tps.foundNoCheatPlus)
			BukkitPluginMain.consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + Phrases.NCP_EXEMPT, player.getName());
		plugin.intergts.doExemptNCP(player);
		// Other setup
		tps.originalFlightAllow = player.getAllowFlight();
		tps.originalFlightState = player.isFlying();
		tps.originalGameMode    = player.getGameMode();
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setPlayerWeather(WeatherType.CLEAR);
		if(tps.supportSpectatorMode)
			player.setGameMode(GameMode.SPECTATOR);
		plugin.playStates.put(player, tps);
		// Schedule tick
		tps.scheduledTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				tps.localTick += 1;
				processDemoStep(player, tps);
			}
		}, 1, 1);
		return tps;
	}
	void onTick()
	{
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
			BukkitPluginMain.consoleLog.log(Level.WARNING, Settings.CHAT_PREFIX + "Demo processing error: {0}", ex);
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
		tps.currentSegmentFlightTime = TrajectoryFlightMath.calculateFlightTime(tp1, tp2);
		tps.currentSegmentDeltaYaw = TrajectoryFlightMath.calculateYawDelta(tp1, tp2);
		// Log into console about this event
		if(plugin.settings.getLogPointReached())
			BukkitPluginMain.consoleLog.log(Level.INFO, Settings.CHAT_PREFIX + Phrases.POINT_REACHED, new Object[]
			{
				player.getName(),
				tps.trajectory.caption != null
					? tps.trajectory.caption
					: "<buffer>",
				tps.currentPoint,
			});
		// Message on point reach
		if(tp1.messageOnReach != null && !"".equals(tp1.messageOnReach))
		{
			String text = GenericChatCodes.processStringStatic(tp1.messageOnReach);
			if(tps.foundPlaceholderAPI)
				text = IntegrationMan.processPlaceholders(player, text);
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
				title    = IntegrationMan.processPlaceholders(player, title);
				subtitle = IntegrationMan.processPlaceholders(player, subtitle);
			}
			try
			{
				if(!"".equals(title) || !"".equals(subtitle))
					IntegrationMan.sendTitles(player, title, subtitle, 20, tp1.showTitleTicks, 20);
			} catch(Exception ex) {
				BukkitPluginMain.consoleLog.log(Level.WARNING, Settings.CHAT_PREFIX + "ProtocolLib error, disabling titles.\n{0}", ex);
				tps.foundProtocolLib = false;
			}
		}
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
				float newYaw = (float)(fy1 + percent * tps.currentSegmentDeltaYaw);
				if(newYaw < -180.0f)
					newYaw += 360.0f;
				if(newYaw > 180.0f)
					newYaw -= 360.0f;
				target.setYaw(newYaw);
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
}
