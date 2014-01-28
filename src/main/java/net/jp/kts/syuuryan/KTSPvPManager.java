// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   KTSPvPManager.java

package net.jp.kts.syuuryan;

import java.util.*;
import java.util.logging.Logger;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

// Referenced classes of package net.jp.kts.syuuryan:
//            PvPPlayer, ChatMode

public class KTSPvPManager extends JavaPlugin
    implements Listener
{

    public KTSPvPManager()
    {
    }

    public void onEnable()
    {
        super.onEnable();
        log = getLogger();
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        teamList = new ArrayList();
        pvpPlayers = new ArrayList();
        getServer().getPluginManager().registerEvents(this, this);
    }

    public void onDisable()
    {
        super.onDisable();
        Team team;
        for(Iterator iterator = teamList.iterator(); iterator.hasNext(); team.setPrefix(ChatColor.RESET.toString()))
            team = (Team)iterator.next();

        teamList.clear();
        pvpPlayers.clear();
    }

    public void onChat(AsyncPlayerChatEvent chat)
    {
        PvPPlayer player = null;
        for(Iterator iterator = pvpPlayers.iterator(); iterator.hasNext();)
        {
            PvPPlayer p = (PvPPlayer)iterator.next();
            if(p.getPlayer().equals(chat.getPlayer()))
            {
                player = p;
                break;
            }
        }

        if(player == null)
            return;
        if(player.getChatMode() == ChatMode.NOMAL)
            return;
        for(Iterator iterator1 = teamList.iterator(); iterator1.hasNext();)
        {
            Team team = (Team)iterator1.next();
            if(team.getPlayers().contains(player.getPlayer()))
            {
                for(Iterator iterator2 = team.getPlayers().iterator(); iterator2.hasNext();)
                {
                    OfflinePlayer sendTarget = (OfflinePlayer)iterator2.next();
                    if(sendTarget.isOnline())
                        ((Player)sendTarget).sendMessage((new StringBuilder("[TEAM]<")).append(player.getPlayer().getName()).append("> ").append(chat.getMessage()).toString());
                    else
                        log.warning((new StringBuilder(String.valueOf(sendTarget.getName()))).append(" is offline!").toString());
                }

                break;
            }
        }

        chat.setCancelled(true);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLagel, String args[])
    {
        if(sender == null)
            return false;
        if(sender instanceof ConsoleCommandSender)
            return false;
        if(cmd.getName().equalsIgnoreCase("pvp"))
        {
            if(args.length > 1 && args[0].equalsIgnoreCase("team"))
            {
                if(args[1].equalsIgnoreCase("add"))
                    return addTeam(sender, args);
                if(args[1].equalsIgnoreCase("join"))
                    return joinPlayerOnTeam(sender, args);
                if(args[1].equalsIgnoreCase("remove"))
                    return removeTeam(sender, args);
                if(args[1].equalsIgnoreCase("clear"))
                    return removeAllTeam(sender, args);
            } else
            if(args.length > 1 && args[0].equalsIgnoreCase("player"))
            {
                if(args[1].equalsIgnoreCase("add"))
                    return joinPlayerOnTeam(sender, args);
                if(args[1].equalsIgnoreCase("remove"))
                    return removePlayerOnTeam(sender, args);
                if(args[1].equalsIgnoreCase("clear"))
                    return clearTeam(sender, args);
            } else
            {
                if(args.length > 0 && args[0].equalsIgnoreCase("ff"))
                    return setFriendlyFire(sender, args);
                if(args.length > 0 && args[0].equalsIgnoreCase("visible"))
                    return setSeeFriendlyInvisibles(sender, args);
            }
        } else
        if(cmd.getName().equalsIgnoreCase("chat"))
            return turnChatMode(sender);
        return false;
    }

    private boolean turnChatMode(CommandSender sender)
    {
        PvPPlayer targetPlayer = null;
        for(Iterator iterator = pvpPlayers.iterator(); iterator.hasNext();)
        {
            PvPPlayer player = (PvPPlayer)iterator.next();
            if(player.getPlayer().getName().equals(sender.getName()))
            {
                targetPlayer = player;
                break;
            }
        }

        if(targetPlayer != null)
        {
            targetPlayer.turnChatMode();
            targetPlayer.sendMessage((new StringBuilder("Chat mode is ")).append(targetPlayer.getChatMode()).toString());
            return true;
        } else
        {
            return false;
        }
    }

    private boolean removeAllTeam(CommandSender sender, String args[])
    {
        pvpPlayers.clear();
        teamList.clear();
        if(board != null)
        {
            Set teams = board.getTeams();
            if(teams != null)
                teams.clear();
        }
        return true;
    }

    private boolean removeTeam(CommandSender sender, String args[])
    {
        Team team = board.getTeam(args[2]);
        if(team == null)
        {
            return false;
        } else
        {
            teamList.remove(team);
            return board.getTeams().remove(team);
        }
    }

    private boolean setSeeFriendlyInvisibles(CommandSender sender, String args[])
    {
        if(args.length != 2)
        {
            String reason = args.length >= 2 ? "Too many arguments. " : "Reason:Lack of arguments. ";
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(reason).append(ChatColor.RESET).append("NOTICE:/pvp visible on|off|true|false").toString());
            return false;
        }
        boolean enabled = false;
        if(args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true"))
            enabled = true;
        else
        if(!args[1].equalsIgnoreCase("off") && !args[1].equalsIgnoreCase("false"))
        {
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Invalid option! NOTICE:/pvp visible on|off|true|false").toString());
            return false;
        }
        Set teams = board.getTeams();
        for(Iterator iterator = teams.iterator(); iterator.hasNext();)
        {
            Team team = (Team)iterator.next();
            team.setCanSeeFriendlyInvisibles(enabled);
            Set members = team.getPlayers();
            for(Iterator iterator1 = members.iterator(); iterator1.hasNext();)
            {
                OfflinePlayer member = (OfflinePlayer)iterator1.next();
                if(member.isOnline())
                    if(enabled)
                        ((Player)member).sendMessage((new StringBuilder()).append(ChatColor.GREEN).append("SeeFriendlyInvisibles has been enabeld.").toString());
                    else
                        ((Player)member).sendMessage((new StringBuilder()).append(ChatColor.RED).append("SeeFriendlyInvisibles has been diabled.").toString());
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
        Set teams = board.getTeams();
        for(Iterator iterator = teams.iterator(); iterator.hasNext();)
        {
            Team team = (Team)iterator.next();
            team.setAllowFriendlyFire(enabled);
            Set members = team.getPlayers();
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

    private boolean clearTeam(CommandSender sender, String args[])
    {
        if(args.length != 3)
        {
            String reason = args.length >= 3 ? "Too many arguments. " : "Reason:Lack of arguments. ";
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(reason).append(ChatColor.RESET).append("NOTICE:/pvp team clear <teamname>").toString());
            return false;
        }
        boolean removed = false;
        Set teams = board.getTeams();
        for(Iterator iterator = teams.iterator(); iterator.hasNext();)
        {
            Team team = (Team)iterator.next();
            if(team.getName().equalsIgnoreCase(args[2]))
            {
                Set teamMembers = team.getPlayers();
                for(Iterator iterator1 = teamMembers.iterator(); iterator1.hasNext();)
                {
                    OfflinePlayer member = (OfflinePlayer)iterator1.next();
                    team.removePlayer(member);
                    pvpPlayers.remove(member);
                    removed = true;
                }

            }
        }

        if(removed)
            sender.sendMessage((new StringBuilder(String.valueOf(args[2]))).append(" had been cleared up. No member exists.").toString());
        return removed;
    }

    private boolean removePlayerOnTeam(CommandSender sender, String args[])
    {
        if(args.length != 3)
        {
            String reason = args.length >= 3 ? "Too many arguments. " : "Reason:Lack of arguments. ";
            sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(reason).append(ChatColor.RESET).append("NOTICE:/pvp team remove <playername>").toString());
            return false;
        }
        Set teams = board.getTeams();
        OfflinePlayer targetPlayer = getServer().getOfflinePlayer(args[2]);
        for(Iterator iterator = teams.iterator(); iterator.hasNext();)
        {
            Team team = (Team)iterator.next();
            Set teamMembers = team.getPlayers();
            if(teamMembers.contains(targetPlayer))
            {
                team.removePlayer(targetPlayer);
                pvpPlayers.remove(targetPlayer);
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
        Set teams = board.getTeams();
        for(int i = 3; i < args.length; i++)
        {
            Iterator iterator = teams.iterator();
            while(iterator.hasNext()) 
            {
                Team team = (Team)iterator.next();
                if(!team.getName().equalsIgnoreCase(args[2]))
                    continue;
                Set teamMembers = team.getPlayers();
                OfflinePlayer targetPlayer = getServer().getOfflinePlayer(args[i]);
                if(teamMembers.contains(targetPlayer))
                {
                    sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(args[i]).append(" already exists in ").append(team.getDisplayName()).toString());
                    break;
                }
                team.addPlayer(targetPlayer);
                if(!pvpPlayers.contains(targetPlayer))
                    pvpPlayers.add(new PvPPlayer((Player)targetPlayer));
                sender.sendMessage((new StringBuilder(String.valueOf(args[i]))).append(" joined team on ").append(team.getDisplayName()).toString());
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
        sender.sendMessage((new StringBuilder("Created team Name:")).append(args[2]).toString());
        return true;
    }

    Logger log;
    ScoreboardManager manager;
    Scoreboard board;
    ArrayList teamList;
    ArrayList pvpPlayers;
}
