package ru.simsonic.rscFirstJoinDemo.API;

import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public interface Settings
{
	public static final String UPDATER_URL = "http://simsonic.github.io/rscFirstJoinDemo/latest.json";
	public static final String CHAT_PREFIX = "{_DG}[rscfjd] {_LS}";
	public static final String UPDATE_CMD  = "/rscfjd update do";
	
	public static final String SIGN_LINE_0 = GenericChatCodes.processStringStatic("{_DG}[rscFJD]");
	public static final String DEFAULT_TRAJECTORY = "firstjoin";
	public static final String DIR_TRAJECTORIES   = "public";
	public static final String DIR_PERSONAL       = "buffers";
	
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
