package ru.simsonic.rscFirstJoinDemo;

import ru.simsonic.rscFirstJoinDemo.API.GenericPoint;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;

public class Trajectory<T extends GenericPoint>
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
