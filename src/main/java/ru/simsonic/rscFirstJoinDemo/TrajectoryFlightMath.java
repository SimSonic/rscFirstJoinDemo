package ru.simsonic.rscFirstJoinDemo;

import org.bukkit.Location;
import org.bukkit.World;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;

public final class TrajectoryFlightMath
{
	public static long calculateFlightTime(TrajectoryPoint tp1, TrajectoryPoint tp2)
	{
		long result = tp1.freezeTicks;
		if(tp1.speedAfter > 0.005f && tp2 != null)
		{
			final Location l1 = tp1.location;
			final Location l2 = tp2.location;
			if(l1 != null && l2 != null)
			{
				final World w1 = l1.getWorld();
				final World w2 = l2.getWorld();
				if(w1 != null && w2 != null && w1.equals(w2))
					result += Math.floor(l1.distance(l2) * 20.0 / tp1.speedAfter) + 2;
			}
		}
		return result;
	}
	public static float calculateYawDelta(TrajectoryPoint tp1, TrajectoryPoint tp2)
	{
		if(tp2 == null)
			return 0.0f;
		float result = tp2.location.getYaw() - tp1.location.getYaw();
		if(result > 180.0f)
			result -= 360.0f;
		if(result < -180.0f)
			result += 360.0f;
		return result;
	}
}
