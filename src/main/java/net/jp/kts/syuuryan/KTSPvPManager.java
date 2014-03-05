// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   KTSPvPManager.java

package net.jp.kts.syuuryan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

// Referenced classes of package net.jp.kts.syuuryan:
//            PvPPlayer, ChatMode

public class KTSPvPManager extends JavaPlugin implements Listener {
    Logger log;
    ScoreboardManager manager;
    Scoreboard board;
    ArrayList<Team> teamList;
//    ArrayList<PvPPlayer> pvpPlayers;
    ArrayList<OfflinePlayer> teamChatPlayers;

    @Override
	public void onEnable()
    {
        super.onEnable();
        log = getLogger();
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        teamList = new ArrayList<Team>();
//        pvpPlayers = new ArrayList<PvPPlayer>();
        teamChatPlayers = new ArrayList<OfflinePlayer>();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
	public void onDisable()
    {
        super.onDisable();
        Team team;
        for(Iterator iterator = teamList.iterator(); iterator.hasNext(); team.setPrefix(ChatColor.RESET.toString()))
            team = (Team)iterator.next();

        teamList.clear();
//        pvpPlayers.clear();
        teamChatPlayers.clear();
        board.clearSlot(DisplaySlot.SIDEBAR);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
    	Player player = event.getPlayer();
    	boolean inTeam = false;
    	for (Team team : board.getTeams()) {
    		if (team.getPlayers().contains(player)) {
    			inTeam = true;
    			break;
    		}
    	}
    	if (inTeam) {
    		player.setScoreboard(board);
    	}
    }

    @EventHandler
    public  void onQuit(PlayerQuitEvent event) {
    	Player player = event.getPlayer();
    	for (OfflinePlayer offlinePlayer : teamChatPlayers) {
    		if (offlinePlayer.getName().equals(player.getName())) {
    			teamChatPlayers.remove(offlinePlayer);
    			break;
    		}
    	}
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent chat)
    {
        OfflinePlayer player = null;
        for (OfflinePlayer p : teamChatPlayers) {
        	if (p.getName().equals(chat.getPlayer().getName())) {
        		player = p;
        		break;
        	}
        }

        if(player == null) {
            return;
        }

        for (Team team : teamList) {
        	if (team.getPlayers().contains(player.getPlayer())) {
        		for (OfflinePlayer sendTarget : team.getPlayers()) {
                    if(sendTarget.isOnline())
                        ((Player)sendTarget).sendMessage((new StringBuilder("[TEAM]<")).append(player.getPlayer().getName()).append("> ").append(chat.getMessage()).toString());
        		}
        		break;
        	}
        }
        log.info((new StringBuilder("[TEAM]<")).append(player.getPlayer().getName()).append("> ").append(chat.getMessage()).toString());

        chat.setCancelled(true);
    }

    @Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLagel, String args[])
    {
        if(sender == null)
            return false;
        if(sender instanceof ConsoleCommandSender)
            return false;
        if(cmd.getName().equalsIgnoreCase("pvp")) {
            if(args.length > 1 && args[0].equalsIgnoreCase("team")) {
                if(args[1].equalsIgnoreCase("add"))
                    return addTeam(sender, args);
                if(args[1].equalsIgnoreCase("join"))
                    return joinPlayerOnTeam(sender, args);
                if(args[1].equalsIgnoreCase("leave"))
                    return leaveTeam(sender, args);
                if(args[1].equalsIgnoreCase("empty"))
                    return emptyTeam(sender, args);
                if(args[1].equalsIgnoreCase("remove"))
                	return removeTeam(sender, args);
                if(args[1].equalsIgnoreCase("clear"))
                	return clearTeam(sender, args);
                if(args[1].equalsIgnoreCase("list")) {
                	return seeTeamList(sender);
                }
            }
            else {
                if(args.length > 0 && args[0].equalsIgnoreCase("ff"))
                    return setFriendlyFire(sender, args);
                if(args.length > 0 && args[0].equalsIgnoreCase("fv"))
                    return setFriendlyVisibilities(sender, args);
            }
        }
        else if(cmd.getName().equalsIgnoreCase("chat")) {
            return turnChatMode(sender);
        }
        return false;
    }

	private boolean seeTeamList(CommandSender sender) {
		if (teamList.size() == 0) {
			sender.sendMessage("No team exists");
			return true;
		}

		for (int i = 0; i < teamList.size(); i++) {
			ChatColor color = ChatColor.RESET;
			switch (i) {
			case 0: color = ChatColor.RED; break;
			case 1: color = ChatColor.BLUE; break;
			case 2: color = ChatColor.GREEN; break;
			case 3: color = ChatColor.LIGHT_PURPLE; break;
			}
			sender.sendMessage(color + teamList.get(i).getDisplayName());
			for (OfflinePlayer player : teamList.get(i).getPlayers()) {
				sender.sendMessage("  " + player.getName());
			}
		}
		return true;
	}

    private boolean turnChatMode(CommandSender sender)
    {
    	boolean exists = false;
    	OfflinePlayer player = getServer().getOfflinePlayer(sender.getName());
    	for (Team team : board.getTeams()) {
    		if (team.getPlayers().contains(player)) {
    			exists = true;
    			break;
    		}
    	}

    	if (!exists) return false;

    	String chatMode = null;
    	if (teamChatPlayers.contains(player)) {
        	chatMode = "NOMAL";
        	teamChatPlayers.remove(player);
    	}
    	else {
        	chatMode = "TEAM";
        	teamChatPlayers.add(player);
    	}
        player.getPlayer().sendMessage("Chat mode is " + chatMode);
        return true;
    }

    private boolean clearTeam(CommandSender sender, String args[])
    {
        if(board != null)
        {
            Set<Team> teams = board.getTeams();
            if(teams != null)
                for (Team team : teams)
                	team.unregister();
        }
        teamChatPlayers.clear();
        teamList.clear();
        return true;
    }

    private boolean removeTeam(CommandSender sender, String args[])
    {
        Team team = board.getTeam(args[2]);
        if(team == null) {
        	log.warning("does not exists " + args[2]);
        	return false;
        }

        teamChatPlayers.removeAll(team.getPlayers());
        teamList.remove(team);
        team.unregister();
        return true;
    }

    private boolean setFriendlyVisibilities(CommandSender sender, String args[])
    {
        if(args.length != 2)
        {
            String reason = args.length >= 2 ? "Too many arguments. " : "Reason:Lack of arguments. ";
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(reason).append(ChatColor.RESET).append("NOTICE:/pvp fv on|off|true|false").toString());
            return false;
        }
        boolean enabled = false;
        if(args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true"))
            enabled = true;
        else
        if(!args[1].equalsIgnoreCase("off") && !args[1].equalsIgnoreCase("false"))
        {
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Invalid option! NOTICE:/pvp fv on|off|true|false").toString());
            return false;
        }
        Set<Team> teams = board.getTeams();
        for(Iterator iterator = teams.iterator(); iterator.hasNext();)
        {
            Team team = (Team)iterator.next();
            team.setCanSeeFriendlyInvisibles(enabled);
            Set<OfflinePlayer> members = team.getPlayers();
            for(Iterator iterator1 = members.iterator(); iterator1.hasNext();)
            {
                OfflinePlayer member = (OfflinePlayer)iterator1.next();
                if(member.isOnline())
                    if(enabled)
                        ((Player)member).sendMessage((new StringBuilder()).append(ChatColor.GREEN).append("FriendlyVisibilites has been enabeld.").toString());
                    else
                        ((Player)member).sendMessage((new StringBuilder()).append(ChatColor.RED).append("FriendlyVnvisibilities has been diabled.").toString());
            }

        }

        return false;
    }

    private boolean setFriendlyFire(CommandSender sender, String args[])
    {
        if(args.length != 2)
        {
            String reason = args.length >= 2 ? "Too many arguments. " : "Reason:Lack of arguments. ";
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(reason).append(ChatColor.RESET).append("NOTICE:/pvp ff on|off|true|false").toString());
            return false;
        }
        boolean enabled = false;
        if(args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true"))
            enabled = true;
        else
        if(!args[1].equalsIgnoreCase("off") && !args[1].equalsIgnoreCase("false"))
        {
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(args[1]).append(" is invalid option! NOTICE:/pvp ff on|off|true|false").toString());
            return false;
        }
        Set<Team> teams = board.getTeams();
        for(Iterator iterator = teams.iterator(); iterator.hasNext();)
        {
            Team team = (Team)iterator.next();
            team.setAllowFriendlyFire(enabled);
            Set<OfflinePlayer> members = team.getPlayers();
            for(Iterator iterator1 = members.iterator(); iterator1.hasNext();)
            {
                OfflinePlayer member = (OfflinePlayer)iterator1.next();
                if(member.isOnline())
                    if(enabled)
                        ((Player)member).sendMessage((new StringBuilder()).append(ChatColor.RED).append("FriendlyFire has been enabeld.").toString());
                    else
                        ((Player)member).sendMessage((new StringBuilder()).append(ChatColor.GREEN).append("FriendlyFire has been diabled.").toString());
            }

        }

        return false;
    }

    private boolean emptyTeam(CommandSender sender, String args[])
    {
        if(args.length != 3)
        {
            String reason = args.length >= 3 ? "Too many arguments. " : "Reason:Lack of arguments. ";
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(reason).append(ChatColor.RESET).append("NOTICE:/pvp team empty <teamname>").toString());
            return false;
        }

        boolean removed = false;
        Set<Team> teams = board.getTeams();
        for(Team team : teams)
        {
            if(team.getName().equalsIgnoreCase(args[2]))
            {
                Set<OfflinePlayer> teamMembers = team.getPlayers();
                for(OfflinePlayer member : teamMembers)
                {
                    team.removePlayer(member);
                    teamChatPlayers.remove(member);
                    removed = true;
                }

            }
        }

        if(removed)
            sender.sendMessage((new StringBuilder(String.valueOf(args[2]))).append(" had been cleared up. No member exists.").toString());
        return removed;
    }

    private boolean leaveTeam(CommandSender sender, String args[])
    {
        if(args.length != 3)
        {
            String reason = args.length >= 3 ? "Too many arguments. " : "Reason:Lack of arguments. ";
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(reason).append(ChatColor.RESET).append("NOTICE:/pvp team leave <playername>").toString());
            return false;
        }

        if (args[2].equalsIgnoreCase("all")) {
        	return emptyTeam(sender, args);
        }

        Set<Team> teams = board.getTeams();
        String targetName = args[2];
        if (targetName.equalsIgnoreCase("me")) {
        	targetName = sender.getName();
        }
        OfflinePlayer targetPlayer = getServer().getOfflinePlayer(targetName);
        for(Team team : teams)
        {
            Set<OfflinePlayer> teamMembers = team.getPlayers();
            if(teamMembers.contains(targetPlayer))
            {
                team.removePlayer(targetPlayer);
                teamChatPlayers.remove(targetPlayer);
                sender.sendMessage((new StringBuilder(String.valueOf(targetPlayer.getName()))).append(" was removed from Team:").append(team.getDisplayName()).toString());
                return true;
            }
        }

        return false;
    }

    private boolean joinPlayerOnTeam(CommandSender sender, String args[])
    {
        if(args.length < 4)
        {
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Reason:Lack of arguments. ").append(ChatColor.RESET).append("NOTICE:/pvp team join <teamname> <playername>").toString());
            return false;
        }
        boolean added = false;
        Set<Team> teams = board.getTeams();
        for(int i = 3; i < args.length; i++)
        {
            Iterator iterator = teams.iterator();
            while(iterator.hasNext())
            {
                Team team = (Team)iterator.next();
                if(!team.getName().equalsIgnoreCase(args[2]))
                    continue;
                Set<OfflinePlayer> teamMembers = team.getPlayers();
                String targetName = args[i];
                if (targetName.equalsIgnoreCase("me")) {
                	targetName = sender.getName();
                }
                OfflinePlayer targetPlayer = getServer().getOfflinePlayer(targetName);
                if(teamMembers.contains(targetPlayer))
                {
                    sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(targetName).append(" already exists in ").append(team.getDisplayName()).toString());
                    break;
                }
                team.addPlayer(targetPlayer);

                // スコアボードの設定
//                if (team.getPlayers().size() == 1) {
//                	team.addPlayer(Bukkit.getOfflinePlayer(team.getDisplayName()));
//                }
//                Objective objective = board.getObjective(DisplaySlot.SIDEBAR);
////                objective.getScore(Bukkit.getOfflinePlayer(sender.getName()));
//                Score score = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "kills"));
//                score.setScore(0);

                sender.sendMessage((new StringBuilder(String.valueOf(targetName))).append(" joined team on ").append(team.getDisplayName()).toString());
                targetPlayer.getPlayer().setScoreboard(board);
                added = true;
            }
        }

        return added;
    }

    private boolean addTeam(CommandSender sender, String args[])
    {
        if(args.length != 3)
        {
            String reason = args.length >= 3 ? "Too many arguments. " : "Reason:Lack of arguments. ";
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(reason).append(ChatColor.RESET).append("NOTICE:/pvp team add <teamname>").toString());
            return false;
        }
        for(Iterator iterator = teamList.iterator(); iterator.hasNext();)
        {
            Team t = (Team)iterator.next();
            if(t.getName().equals(args[2]))
            {
                sender.sendMessage((new StringBuilder("Couldn't add Team. Name:")).append(args[2]).append(" This name already exists.").toString());
                return false;
            }
        }

        Team team = board.registerNewTeam(args[2]);
        teamList.add(team);
        team.setDisplayName(args[2]);
        int index = teamList.indexOf(team);
        ChatColor teamColor;
        switch(index)
        {
        case 0: // '\0'
            teamColor = ChatColor.RED;
            break;

        case 1: // '\001'
            teamColor = ChatColor.BLUE;
            break;

        case 2: // '\002'
            teamColor = ChatColor.GREEN;
            break;

        case 3: // '\003'
            teamColor = ChatColor.LIGHT_PURPLE;
            break;

        default:
            teamColor = ChatColor.RESET;
            break;
        }
        team.setPrefix(teamColor.toString());

        // スコアボードの設定
//        Objective objective = board.registerNewObjective(teamColor + team.getName(), "dummy");
//        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
//        Score score = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "kills:"));
//        score.setScore(99);


        sender.sendMessage((new StringBuilder("Created team Name:")).append(args[2]).toString());
        return true;
    }

}
