package ru.simsonic.rscFirstJoinDemo;

import org.bukkit.GameMode;

public class TrajectoryPlayState
{
	public Trajectory trajectory;
	public boolean    started;
	public int        currentPoint;
	public long       currentPointStartTick;
	public long       currentSegmentFlightTime;
	public int        playTask;
	public boolean    originalFlightAllow;
	public boolean    originalFlightState;
	public float      deltaYaw;
	public long       localTick;
	public GameMode   gamemode;
	public boolean    protocolLibFound;
}
