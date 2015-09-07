package ru.simsonic.rscFirstJoinDemo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
	public Trajectory lazyFirstJoinTrajectoryLoading()
	{
		if(trajectories.containsKey(firstJoinTrajectory) == false)
			loadTrajectory(firstJoinTrajectory);
		return trajectories.get(firstJoinTrajectory);
	}
	public Trajectory loadTrajectory(String caption)
	{
		final File trajectoryFile = new File(plugin.getDataFolder(), caption.toLowerCase() + ".json");
		final Trajectory result = loadTrajectoryFile(trajectoryFile);
		result.caption = caption;
		BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} contains ({1} points)",
			new Object[] { caption, result.points.length });
		trajectories.put(caption, result);
		return result;
	}
	public Trajectory loadBufferTrajectory(Player player)
	{
		String caption = player.getName();
		try
		{
			caption = player.getUniqueId().toString();
		} catch(RuntimeException ex) {
			// Pre-1.7 servers
		}
		final File buffersDir = new File(plugin.getDataFolder(), "buffers");
		final File trajectoryFile = new File(buffersDir, caption.toLowerCase() + ".json");
		final Trajectory result = loadTrajectoryFile(trajectoryFile);
		result.caption = caption;
		BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} contains ({1} points)",
			new Object[] { caption, result.points.length });
		trajectories.put(caption, result);
		return result;
	}
	private Trajectory loadTrajectoryFile(File file)
	{
		Trajectory result;
		try
		{
			result = HashAndCipherUtilities.loadObject(file, Trajectory.class);
		} catch(IOException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Error reading {0}: {1}",
				new Object[] { file.toString(), ex });
			result = new Trajectory();
		}
		if(result.points == null)
			result.points = new TrajectoryPoint[] {};
		for(TrajectoryPoint tp : result.points)
			tp.location = locationForTrajectoryPoint(tp);
		return result;
	}
	public void saveTrajectory(Trajectory trajectory, String caption)
	{
		if(trajectory == null)
			return;
		final File trajectoryFile = new File(plugin.getDataFolder(), caption.toLowerCase() + ".json");
		try
		{
			saveTrajectoryFile(trajectory, trajectoryFile);
			BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} has been saved ({1})",
				new Object[] { caption, trajectory.points.length });
		} catch(IOException ex) {
		}
	}
	public void saveBufferTrajectory(Trajectory trajectory, Player player)
	{
		String caption = player.getName();
		try
		{
			caption = player.getUniqueId().toString();
		} catch(RuntimeException ex) {
			// Pre-1.7 servers
		}
		final File buffersDir = new File(plugin.getDataFolder(), "buffers");
		final File trajectoryFile = new File(buffersDir, caption.toLowerCase() + ".json");
		try
		{
			saveTrajectoryFile(trajectory, trajectoryFile);
			BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} has been saved ({1})",
				new Object[] { caption, trajectory.points.length });
		} catch(IOException ex) {
		}
	}
	public void saveTrajectoryFile(Trajectory trajectory, File file) throws IOException
	{
		try
		{
			HashAndCipherUtilities.saveObject(file, trajectory, Trajectory.class);
		} catch(IOException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Error writing {0}: {1}",
				new Object[] { file.toString(), ex });
			throw ex;
		}
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
	public void setFirstJoinTrajectory(String caption)
	{
		this.firstJoinTrajectory = caption;
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
