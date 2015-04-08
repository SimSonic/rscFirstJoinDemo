package ru.simsonic.rscFirstJoinDemo;

import java.util.logging.Level;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import ru.simsonic.rscUtilityLibrary.TextProcessing.GenericChatCodes;

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
			tps.currentPoint = -1;
			tps.originalFlightAllow = player.getAllowFlight();
			tps.originalFlightState = player.isFlying();
			tps.gamemode = player.getGameMode();
			player.setAllowFlight(true);
			player.setFlying(true);
			player.setPlayerWeather(WeatherType.CLEAR);
			player.setGameMode(GameMode.SPECTATOR);
			plugin.playing.put(player, tps);
			tps.playTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
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
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Demo starting error: {0}", new Object[] { ex });
		}
	}
	public void finishDemo(Player player)
	{
		try
		{
			final TrajectoryPlayState tps = plugin.playing.get(player);
			if(tps != null)
			{
				player.setGameMode(tps.gamemode);
				player.setAllowFlight(tps.originalFlightAllow);
				player.setFlying(tps.originalFlightState);
				player.setFallDistance(0.0f);
				player.resetPlayerWeather();
				player.resetPlayerTime();
				plugin.getServer().getScheduler().cancelTask(tps.playTask);
				final int points = tps.trajectory.points.length;
				if(points > 0)
				{
					player.teleport(tps.trajectory.points[points - 1].location);
					player.saveData();
				}
				plugin.playing.remove(player);
				BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Finished playing demo to {0}", player.getName());
			}
			for(Player online : plugin.getServer().getOnlinePlayers())
				online.showPlayer(player);
		} catch(RuntimeException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Demo stopping error: {0}", new Object[] { ex });
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
				tps.deltaYaw = calculateYawDelta(tp1, tp2);
				// Message on point reach
				if(tp1.messageOnReach != null && !"".equals(tp1.messageOnReach))
					player.sendMessage(GenericChatCodes.processStringStatic(tp1.messageOnReach));
				BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Player {0} has reached demo point #{1}", new Object[]
				{
					player.getName(),
					tps.currentPoint
				});
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
			}
			final TrajectoryPoint tp1 = tps.trajectory.points[tps.currentPoint];
			player.setAllowFlight(true);
			player.setFlying(tp1.fly);
			long currentSegmentTimeSpent = tps.localTick - tps.currentPointStartTick;
			// Teleport player to the next position
			if(currentSegmentTimeSpent >= tp1.freezeTicks)
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
					float fp1 = tp1.location.getPitch(), fy1 = tp1.location.getYaw();
					target.setPitch((float)(fp1 + percent * (tp2.location.getPitch() - fp1)));
					target.setYaw((float)(fy1 + percent * tps.deltaYaw));
				}
				// Teleport
				player.teleport(target, TeleportCause.PLUGIN);
			} else
				player.teleport(tp1.location, TeleportCause.PLUGIN);
		} catch(RuntimeException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Demo processing error: {0}", new Object[] { ex });
			finishDemo(player);
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
					result += Math.ceil(0.5 + l1.distance(l2) * 20.0f / tp1.speedAfter);
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
