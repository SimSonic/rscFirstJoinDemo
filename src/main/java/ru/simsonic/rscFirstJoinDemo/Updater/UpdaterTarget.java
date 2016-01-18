package ru.simsonic.rscFirstJoinDemo.Updater;

import java.util.Set;
import org.bukkit.entity.Player;

public interface UpdaterTarget
{
	public void informAboutUpdate(Set<Player> players, Latest latest);
}
