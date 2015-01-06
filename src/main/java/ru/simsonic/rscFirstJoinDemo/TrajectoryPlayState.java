package ru.simsonic.rscFirstJoinDemo;

public class TrajectoryPlayState
{
	public Trajectory trajectory;
	public boolean started;
	public int currentPoint;
	public long currentPointStartTick;
	public long currentSegmentFlightTime;
	public int playTask;
	public boolean originalFlightAllow;
	public boolean originalFlightState;
	public float deltaYaw;
	public long localTick;
}
