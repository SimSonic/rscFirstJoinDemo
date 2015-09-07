package ru.simsonic.rscFirstJoinDemo;

import org.bukkit.GameMode;

public class TrajectoryPlayState
{
	// Server capatibilities
	public boolean    supportSpectatorMode;
	public boolean    foundPlaceholderAPI;
	public boolean    foundProtocolLib;
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
