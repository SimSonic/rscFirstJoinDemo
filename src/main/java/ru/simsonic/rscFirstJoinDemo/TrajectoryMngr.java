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
import ru.simsonic.rscFirstJoinDemo.API.Trajectory;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;

public class TrajectoryMngr
{
	private final BukkitPluginMain plugin;
	private final HashMap<String, Trajectory> trajectories = new HashMap<>();
	public TrajectoryMngr(BukkitPluginMain plugin)
	{
		this.plugin = plugin;
	}
	public Trajectory getFirstJoinTrajectory()
	{
		return loadTrajectory(plugin.settings.getFirstJoinTrajectory(), false);
	}
	public Trajectory loadTrajectory(String caption, boolean forceReload)
	{
		final String lowerCaption = caption.toLowerCase();
		// IS IT ALREADY IN CACHE?
		Trajectory result = trajectories.get(lowerCaption);
		if(!forceReload && result != null)
			return result;
		// LOAD FROM FILE
		final File trajectoryFile = new File(plugin.getDataFolder(), lowerCaption + ".json");
		result = loadTrajectoryFile(trajectoryFile, true);
		result.caption = caption;
		BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} contains ({1} points)",
			new Object[] { caption, result.points.length });
		trajectories.put(lowerCaption, result);
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
		final String lowerCaption = caption.toLowerCase();
		final File buffersDir = new File(plugin.getDataFolder(), "buffers");
		final File trajectoryFile = new File(buffersDir, lowerCaption + ".json");
		final Trajectory result = loadTrajectoryFile(trajectoryFile, false);
		result.caption = caption;
		BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} contains ({1} points)",
			new Object[] { caption, result.points.length });
		trajectories.put(lowerCaption, result);
		return result;
	}
	public void saveTrajectory(Trajectory trajectory, String caption)
	{
		if(trajectory == null)
			return;
		final String lowerCaption = caption.toLowerCase();
		trajectories.put(lowerCaption, trajectory);
		final File trajectoryFile = new File(plugin.getDataFolder(), lowerCaption + ".json");
		try
		{
			trajectory.caption = caption;
			saveTrajectoryFile(trajectory, trajectoryFile);
			BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} has been saved ({1})",
				new Object[] { caption, trajectory.points.length });
		} catch(IOException ex) {
		}
	}
	public void saveBufferTrajectory(Player player, Trajectory trajectory)
	{
		if(trajectory == null)
			return;
		String caption = player.getName();
		try
		{
			caption = player.getUniqueId().toString();
		} catch(RuntimeException ex) {
			// Pre-1.7 servers
		}
		final String lowerCaption = caption.toLowerCase();
		trajectories.put(lowerCaption, trajectory);
		final File buffersDir = new File(plugin.getDataFolder(), "buffers");
		final File trajectoryFile = new File(buffersDir, lowerCaption + ".json");
		try
		{
			saveTrajectoryFile(trajectory, trajectoryFile);
			BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Trajectory {0} has been saved ({1})",
				new Object[] { caption, trajectory.points.length });
		} catch(IOException ex) {
		}
	}
	public void onDisable()
	{
		trajectories.clear();
	}
	private void saveTrajectoryFile(Trajectory trajectory, File file) throws IOException
	{
		for(int id = 0; id < trajectory.points.length; id += 1)
			trajectory.points[id]._id = id;
		try
		{
			HashAndCipherUtilities.saveObject(file, trajectory, Trajectory.class);
		} catch(IOException ex) {
			BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] Error writing {0}: {1}",
				new Object[] { file.toString(), ex });
			throw ex;
		}
	}
	private Trajectory loadTrajectoryFile(File file, boolean verbose)
	{
		Trajectory result;
		try
		{
			result = HashAndCipherUtilities.loadObject(file, Trajectory.class);
		} catch(IOException ex) {
			if(verbose)
				BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Error reading {0}: {1}",
					new Object[] { file.toString(), ex });
			result = new Trajectory();
		}
		if(result.points == null)
			result.points = new TrajectoryPoint[] {};
		if(result.caption == null)
			result.caption = file.getName().replace(".json", "");
		for(TrajectoryPoint tp : result.points)
			tp.location = locationForTrajectoryPoint(tp);
		return result;
	}
	private Location locationForTrajectoryPoint(TrajectoryPoint tp)
	{
		final World world = plugin.getServer().getWorld(tp.world);
		if(world != null)
			return new Location(world, tp.x, tp.y, tp.z, tp.yaw, tp.pitch);
		BukkitPluginMain.consoleLog.log(Level.WARNING, "[rscfjd] World isn't found: {0}", tp.world);
		return null;
	}
}
