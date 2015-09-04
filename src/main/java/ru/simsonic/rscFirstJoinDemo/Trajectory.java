package ru.simsonic.rscFirstJoinDemo;

import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;

public class Trajectory
{
	public TrajectoryPoint[] points = new TrajectoryPoint[] {};
	public transient String  caption;
	public transient int     selected;
	public transient boolean drawSketch;
	public Trajectory()
	{
	}
	public TrajectoryPlayState newPlayState()
	{
		final TrajectoryPlayState result = new TrajectoryPlayState();
		result.trajectory = this;
		return result;
	}
	public void setSelected(int id)
	{
		if(points != null)
		{
			if(id >= 0 && id < points.length)
				this.selected = id;
			else
				this.selected = points.length > 0 ? points.length - 1 : 0;
		} else
			this.selected = 0;
	}
	public int getSelected()
	{
		return selected;
	}
	public TrajectoryPoint getSelectedPoint()
	{
		return (selected >= 0 && selected < points.length) ? points[selected] : null;
	}
}
