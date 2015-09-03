package ru.simsonic.rscFirstJoinDemo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import ru.simsonic.rscCommonsLibrary.HashAndCipherUtilities;
import ru.simsonic.rscFirstJoinDemo.API.Settings;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;

public class TrajectoryMngr
{
	private final BukkitPluginMain plugin;
	private final HashMap<String, Trajectory> trajectories = new HashMap<>();
	public String firstJoinTrajectory = Settings.defaultTrajectory;
	public TrajectoryMngr(BukkitPluginMain plugin)
	{
		this.plugin = plugin;
	}
	public void setFirstJoinTrajectory(String caption)
	{
		this.firstJoinTrajectory = caption;
	}
	public void saveTrajectory(Trajectory trajectory, String caption)
	{
		if(trajectory == null)
			return;
		caption = caption.toLowerCase();
		try
		{
			HashAndCipherUtilities.saveObject(new File(plugin.getDataFolder(), caption + ".json"), trajectory, Trajectory.class);
			BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} has been saved ({1})",
				new Object[] { caption, trajectory.points.length });
		} catch(IOException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Error writing {0}.json: {1}",
				new Object[] { caption, ex });
		}
	}
	public boolean lazyFirstJoinTrajectoryLoading()
	{
		// Lazy trajectory loading
		if(trajectories.containsKey(firstJoinTrajectory) == false)
			loadTrajectory(firstJoinTrajectory);
		return trajectories.containsKey(firstJoinTrajectory);
	}
	public Trajectory loadTrajectory(String caption)
	{
		Trajectory result;
		caption = caption.toLowerCase();
		try
		{
			result = HashAndCipherUtilities.loadObject(new File(plugin.getDataFolder(), caption + ".json"), Trajectory.class);
		} catch(IOException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Error reading {0}.json: {1}",
				new Object[] { caption, ex });
			result = new Trajectory();
		}
		if(result.points == null)
			result.points = new TrajectoryPoint[] {  };
		for(TrajectoryPoint tp : result.points)
			tp.location = locationForTrajectoryPoint(tp);
		BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} contains ({1} points)",
				new Object[] { caption, result.points.length });
		result.caption = caption;
		trajectories.put(caption, result);
		return result;
	}
	private Location locationForTrajectoryPoint(TrajectoryPoint tp)
	{
		final World world = plugin.getServer().getWorld(tp.world);
		if(world != null)
			return new Location(world, tp.x, tp.y, tp.z, tp.yaw, tp.pitch);
		BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] World not found: {0}", tp.world);
		return null;
	}
	public String getFirstJoinCaption()
	{
		return firstJoinTrajectory;
	}
	public Trajectory getFirstJoin()
	{
		return trajectories.get(firstJoinTrajectory);
	}
	public boolean contains(String caption)
	{
		return trajectories.containsKey(caption);
	}
	public Trajectory get(String caption)
	{
		return trajectories.get(caption);
	}
	public void clear()
	{
		trajectories.clear();
	}
}
