package ru.simsonic.rscFirstJoinDemo.Bukkit;

import org.bukkit.GameMode;
import ru.simsonic.rscFirstJoinDemo.API.Trajectory;

public class TrajectoryPlayState
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
}
