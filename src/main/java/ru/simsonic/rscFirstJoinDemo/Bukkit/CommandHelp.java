package ru.simsonic.rscFirstJoinDemo.Bukkit;

import org.bukkit.command.CommandSender;
import ru.simsonic.rscFirstJoinDemo.API.Command;
import ru.simsonic.rscMinecraftLibrary.Bukkit.CommandAnswerException;

public class CommandHelp extends Command
{
	public CommandHelp(BukkitCommands cmdMan, String name)
	{
		super(cmdMan, name);
	}
	@Override
	public void execute(CommandSender sender, String[] args) throws CommandAnswerException
	{
		if(cmdMan.checkAdminOnly(sender))
			throw new CommandAnswerException(new String[]
			{
				"Generic commands:",
				"{YELLOW}/rscfjd help {_LS}- show this help page.",
				"{YELLOW}/rscfjd play [<player> [caption]] {_LS}- start specific/first-join/buffer demo for you/player.",
				"{YELLOW}/rscfjd stop [player] {_LS}- cancel demo playing for you/player.",
				"Trajectory editor:",
				"{YELLOW}/rscfjd pause {_LS}- stop the demo but don\'t teleport you to the end.",
				"{YELLOW}/rscfjd resume {_LS}- run the demo for you from the point before selected.",
				"{YELLOW}/rscfjd add <freezeTicks> <speedAfter> [text] {_LS}- add new point after current and select it.",
				"{YELLOW}/rscfjd select [#] {_LS}- [re]select point by id for editing and teleport you there.",
				"{YELLOW}/rscfjd next {_LS}- select next point in your buffer.",
				"{YELLOW}/rscfjd prev {_LS}- select previous point in your buffer.",
				"{YELLOW}/rscfjd position {_LS}- update selected point with your location.",
				"{YELLOW}/rscfjd freeze <ticks> {_LS}- update freezeTicks when reach selected point.",
				"{YELLOW}/rscfjd speed <blocksPerSec> {_LS}- update speed after selected point. Zero means teleport.",
				"{YELLOW}/rscfjd text [text] {_LS}- update messageOnReach of selected point.",
				"{YELLOW}/rscfjd titletime <ticks> {_LS}- update showTitleTicks of selected point.",
				"{YELLOW}/rscfjd title [text] {_LS}- update showTitle of selected point.",
				"{YELLOW}/rscfjd subtitle [text] {_LS}- update showSubtitle of selected point.",
				"{YELLOW}/rscfjd time <reset|lock <value|now>|unlock <value|now>> {_LS}- setup point's time.",
				"{YELLOW}/rscfjd weather <reset|sunny|stormy> {_LS}- setup point's weather.",
				"{YELLOW}/rscfjd merge <caption> {_LS}- add another trajectory to the end of your buffer.",
				"{YELLOW}/rscfjd delete {_LS}- remove selected point.",
				"{YELLOW}/rscfjd info {_LS}- show info about server settings, your buffer and selected point.",
				// "{YELLOW}/rscfjd draw {_LS}- toggle showing of buffered trajectory as a 3D line.",
				"{YELLOW}/rscfjd clear {_LS}- clear your buffer",
				"{YELLOW}/rscfjd permission [permission] {_LS}- set/clear permission required to use trajectory by sign.",
				"{YELLOW}/rscfjd save [caption] {_LS}- save your buffer into file.",
				"{YELLOW}/rscfjd load [caption] {_LS}- load file into your buffer.",
				"Administrative:",
				"{YELLOW}/rscfjd reload {_LS}- restart this plugin and reread configuration.",
				"{YELLOW}/rscfjd update [do]{_LS}- download and install new version.",
			});
	}
}
