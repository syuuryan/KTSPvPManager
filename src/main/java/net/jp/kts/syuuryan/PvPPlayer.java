// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   PvPPlayer.java

package net.jp.kts.syuuryan;

import org.bukkit.entity.Player;

// Referenced classes of package net.jp.kts.syuuryan:
//            ChatMode

public class PvPPlayer
{

    public PvPPlayer(Player player)
    {
        this.player = player;
        chatMode = ChatMode.NOMAL;
    }

    public void turnChatMode()
    {
        chatMode = chatMode != ChatMode.NOMAL ? ChatMode.NOMAL : ChatMode.TEAM;
    }

    public Player getPlayer()
    {
        return player;
    }

    public ChatMode getChatMode()
    {
        return chatMode;
    }

    public void sendMessage(String message)
    {
        player.sendMessage(message);
    }

    public void sendMessage(String messages[])
    {
        player.sendMessage(messages);
    }

    private final Player player;
    private ChatMode chatMode;
}
