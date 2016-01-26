package ru.simsonic.rscFirstJoinDemo.API;
import ru.simsonic.rscFirstJoinDemo.Bukkit.TrajectoryPlayState;

public class Trajectory
{
	// Saveable data
	public TrajectoryPoint[] points  = new TrajectoryPoint[] {};
	public String requiredPermission = null;
	// Transient data
	public transient String  caption    = "unnamed";
	public transient int     selected   = 0;
	public transient boolean drawSketch = false;
	public Trajectory()
	{
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
	public TrajectoryPlayState newPlayState()
	{
		final TrajectoryPlayState result = new TrajectoryPlayState();
		result.trajectory = this;
		return result;
	}
}
