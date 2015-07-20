package ru.simsonic.rscFirstJoinDemo.API;

import org.bukkit.Location;

public class TrajectoryPoint extends SightedPoint
{
	public float   speedAfter;
	public String  messageOnReach;
	public int     freezeTicks;
	public boolean timeUpdate;
	public long    timeUpdateValue;
	public boolean timeUpdateLock;
	public boolean timeReset;
	public boolean weatherUpdate;
	public boolean weatherUpdateStormy;
	public boolean weatherReset;
	public TrajectoryPoint()
	{
	}
	public TrajectoryPoint(Location location)
	{
		super(location);
	}
}
