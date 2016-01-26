package ru.simsonic.rscFirstJoinDemo.Bukkit;

import org.bukkit.command.CommandSender;
import ru.simsonic.rscFirstJoinDemo.API.Command;
import ru.simsonic.rscMinecraftLibrary.Bukkit.CommandAnswerException;

public class CommandReload extends Command
{
	public CommandReload(BukkitCommands cmdMan, String name)
	{
		super(cmdMan, name);
	}
	@Override
	public void execute(CommandSender sender, String[] args) throws CommandAnswerException
	{
		if(cmdMan.checkAdminOnly(sender))
		{
			cmdMan.plugin.reloadConfig();
			cmdMan.plugin.getPluginLoader().disablePlugin(cmdMan.plugin);
			cmdMan.plugin.getPluginLoader().enablePlugin(cmdMan.plugin);
			throw new CommandAnswerException("{_LG}Plugin has been reloaded.");
		}
	}
}
