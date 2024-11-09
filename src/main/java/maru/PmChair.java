package maru;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockStairs;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.*;

public class PmChair extends PluginBase implements Listener {

    private final Map<Long, Long> doubleTap = new HashMap<>();
    private final Set<String> disabled = new HashSet<>();
    private Map<String, Object> messages;

    private static final int m_version = 2;

    @Override
    public void onEnable() {
        loadMessage();
        Entity.registerEntity("Chair", ChairEntity.class);
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void loadMessage() {
        saveResource("messages.yml");
        messages = new Config(new File(getDataFolder(), "messages.yml"), Config.YAML).getAll();
        if ((int) messages.get("m_version") < m_version) {
            saveResource("messages.yml", true);
            messages = new Config(new File(getDataFolder(), "messages.yml"), Config.YAML).getAll();
        }
    }

    private String getTranslation(String m) {
        return (String) messages.get(m);
    }

    @EventHandler
    public void onTouch(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.riding != null ||
                player.isSneaking() ||
                event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK ||
                disabled.contains(player.getName())) {
            return;
        }

        Block block = event.getBlock();
        if (block instanceof BlockStairs) {
            if (block.y > player.y + 2 || block.y < player.y - 2) return;
            if (block.distanceSquared(player) > 100) return;
            if ((block.getDamage() & 4) != 0 || block.up().isSolid()) return;

            long playerId = player.getId();
            Long last = doubleTap.get(playerId);
            if (last == null) {
                doubleTap.put(playerId, System.currentTimeMillis());
                player.sendPopup(getTranslation("touch-stairs-popup"));
                return;
            }

            if (System.currentTimeMillis() - last < 500) {
                Entity minecart = Entity.createEntity("Chair", block.add(0.5, 0, 0.5));
                player.setPosition(minecart);
                minecart.mountEntity(player);
                minecart.spawnToAll();

                doubleTap.remove(playerId);
            } else {
                doubleTap.put(playerId, System.currentTimeMillis());
                player.sendPopup(getTranslation("touch-stairs-popup"));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        doubleTap.remove(event.getPlayer().getId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("chair")) {
            if (!(sender instanceof Player)) {
                return true;
            }

            if (args.length == 0) {
                return false;
            }

            if (args[0].equalsIgnoreCase("off")) {
                disabled.add(sender.getName());
                sender.sendMessage(getTranslation("command-disabled"));
            } else if (args[0].equalsIgnoreCase("on")) {
                disabled.remove(sender.getName());
                sender.sendMessage(getTranslation("command-enabled"));
            } else {
                return false;
            }

            return true;
        }

        return false;
    }
}
