package ru.simsonic.rscFirstJoinDemo.API;

import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public interface Settings
{
	public static final String updaterURL = "http://simsonic.github.io/rscFirstJoinDemo/latest.json";
	
	public static final String defaultFirstJoinTrajectory = "firstjoin";
	public static final String buffersDir      = "buffers";
	public static final String trajectoriesDir = "public";
	public static final String chatPrefix      = "{_DG}[rscfjd] {_LS}";
	public static final String signLine0       = GenericChatCodes.processStringStatic("{_DG}[rscFJD]");
	
	public void onLoad();
	public void onEnable();
	
	public String  getFirstJoinTrajectory();
	public boolean getFirstJoinEnabled();
	public int     getFirstJoinDelay();
	public boolean getRequireSignPerms();
	public boolean getLogStartStop();
	public boolean getLogPointReached();
	public String  getLanguage();
	
	public TranslationProvider getTranslationProvider();
}
