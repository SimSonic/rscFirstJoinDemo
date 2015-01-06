package ru.simsonic.rscFirstJoinDemo;

public class Trajectory
{
	public transient String caption;
	public TrajectoryPoint[] points = new TrajectoryPoint[] {};
	public Trajectory()
	{
	}
	public TrajectoryPlayState newPlayState()
	{
		final TrajectoryPlayState result = new TrajectoryPlayState();
		result.trajectory = this;
		return result;
	}
}
