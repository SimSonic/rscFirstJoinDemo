package ru.simsonic.rscFirstJoinDemo.API;

import org.bukkit.Location;

public class TrajectoryPoint extends SightedPoint
{
	public float   speedAfter          = 1.0f;
	public String  messageOnReach      = "";
	public int     freezeTicks         = 0;
	public boolean timeReset           = false;
	public boolean timeUpdate          = false;
	public long    timeUpdateValue     = 0;
	public boolean timeUpdateLock      = false;
	public boolean weatherReset        = false;
	public boolean weatherUpdate       = false;
	public boolean weatherUpdateStormy = false;
	public String  showTitle           = "";
	public String  showSubtitle        = "";
	public int     showTitleTicks      = 60;
	public TrajectoryPoint()
	{
	}
	public TrajectoryPoint(Location location)
	{
		super(location);
	}
}
