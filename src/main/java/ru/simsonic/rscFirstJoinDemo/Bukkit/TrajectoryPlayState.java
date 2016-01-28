package ru.simsonic.rscFirstJoinDemo.Bukkit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import ru.simsonic.rscFirstJoinDemo.API.TargetProcessor;
import ru.simsonic.rscFirstJoinDemo.API.Trajectory;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;

public class TrajectoryPlayState implements TargetProcessor
{
	// Server capatibilities
	public boolean    supportSpectatorMode;
	public boolean    foundPlaceholderAPI;
	public boolean    foundProtocolLib;
	public boolean    foundNoCheatPlus;
	// Original player state
	public boolean    originalFlightAllow;
	public boolean    originalFlightState;
	public GameMode   originalGameMode;
	// Playing state
	public int        scheduledTaskId;
	public Trajectory trajectory;
	public boolean    started;
	public int        currentPoint;
	public long       currentPointStartTick;
	public long       currentSegmentFlightTime;
	public float      currentSegmentDeltaYaw;
	public long       localTick;
	@Override
	public void onBegin()
	{
	}
	@Override
	public void onPointReached(TrajectoryPoint point)
	{
	}
	@Override
	public void onTeleport(Location location)
	{
	}
	@Override
	public void onFinish()
	{
	}
}
