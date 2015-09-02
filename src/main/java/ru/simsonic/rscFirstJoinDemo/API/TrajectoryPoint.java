package ru.simsonic.rscFirstJoinDemo.API;

import org.bukkit.Location;

public class TrajectoryPoint extends SightedPoint
{
	public float   speedAfter;
	public String  messageOnReach;
	public int     freezeTicks;
	public boolean timeReset;
	public boolean timeUpdate;
	public long    timeUpdateValue;
	public boolean timeUpdateLock;
	public boolean weatherReset;
	public boolean weatherUpdate;
	public boolean weatherUpdateStormy;
	public String  showTitle;
	public String  showSubtitle;
	public int     showTitleTicks;
	public TrajectoryPoint()
	{
	}
	public TrajectoryPoint(Location location)
	{
		super(location);
	}
}
