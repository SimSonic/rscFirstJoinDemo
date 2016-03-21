package ru.simsonic.rscFirstJoinDemo.API;

import java.util.HashMap;
import org.bukkit.command.CommandSender;
import ru.simsonic.rscFirstJoinDemo.Bukkit.BukkitCommands;
import ru.simsonic.rscFirstJoinDemo.Phrases;
import ru.simsonic.rscMinecraftLibrary.Bukkit.CommandAnswerException;

public abstract class Command
{
	protected final BukkitCommands cmdMan;
	protected final String  name;
	protected Phrases usage;
	protected Phrases info;
	protected final static HashMap<String, Command> commands = new HashMap<>();
	public Command(BukkitCommands cmdMan, String name)
	{
		this.cmdMan = cmdMan;
		this.name   = name;
		try
		{
			this.usage  = Phrases.valueOf("commands." + name + ".usage");
			this.info   = Phrases.valueOf("commands." + name + ".info");
		} catch(IllegalArgumentException ex) {
			
		}
		commands.put(name, Command.this);
	}
	public abstract void execute(CommandSender sender, String[] args) throws CommandAnswerException;
}
