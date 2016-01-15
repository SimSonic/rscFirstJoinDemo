package ru.simsonic.rscFirstJoinDemo.API;

public interface Settings
{
	public static final String chatPrefix = "{_DG}[rscfjd] {_LS}";
	public static final String defaultFirstJoinTrajectory = "firstjoin";
	
	public void onLoad();
	public void onEnable();
	
	public String  getFirstJoinTrajectory();
	public boolean getFirstJoinEnabled();
	public int     getFirstJoinDelay();
	public boolean getLogStartStop();
	public boolean getLogPointReached();
}
