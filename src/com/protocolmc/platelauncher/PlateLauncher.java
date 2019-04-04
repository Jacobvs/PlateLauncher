package com.protocolmc.platelauncher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PlateLauncher extends JavaPlugin implements Listener {
    static PlateLauncher plugin;
    boolean ncp = true;
    private HashMap<Location, PressurePlate> plates = new HashMap<>();
    private HashMap<String, PlayerInfo> players = new HashMap<>();
    HashMap<Integer, LaunchedEntity> entities = new HashMap<>();

    private String VERSION = "1.3.1";

    @Override
    public void onEnable() {
        plugin = this;
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, PlateLauncher.this::everySecond, 0L, 20L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, PlateLauncher.this::perTick, 0L, 1L);
        this.saveDefaultConfig();
        List<String> platesList = this.getConfig().getStringList("plates");

        for (String plate : platesList) {
            String[] split = plate.split("\\|");

            try {
                if (this.getServer().getWorld(split[0]) == null) {
                    this.getLogger().info("Invalid world!");
                }

                this.plates.put(new Location(this.getServer().getWorld(split[0]), Double.valueOf(split[1]), Double.valueOf(split[2]), Double.valueOf(split[3])), new PressurePlate(1.0D, 1.0D));
                ((PressurePlate) this.plates.get(new Location(this.getServer().getWorld(split[0]), Double.valueOf(split[1]), Double.valueOf(split[2]), Double.valueOf(split[3])))).load(plate);
            } catch (Exception var6) {
                var6.printStackTrace();
                this.getLogger().info("Invalid plate!");
            }
        }

        Player[] var5;
        int var9 = (var5 = this.getServer().getOnlinePlayers().toArray(new Player[0])).length;

        for (int var8 = 0; var8 < var9; ++var8) {
            Player player = var5[var8];
            this.players.put(player.getName(), new PlayerInfo());
        }

        if (this.getServer().getPluginManager().getPlugin("NoCheatPlus") == null) {
            this.ncp = false;
        }
        Bukkit.getServer().getConsoleSender().sendMessage("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Bukkit.getServer().getConsoleSender().sendMessage("Developed for protocolmc.com");
        Bukkit.getServer().getConsoleSender().sendMessage("Version " + VERSION);
        Bukkit.getServer().getConsoleSender().sendMessage("PlateLauncher is now enabled!");
        Bukkit.getServer().getConsoleSender().sendMessage("~~~~~~~~~~~~~~~~[PL]~~~~~~~~~~~~~~~~");
    }

    @Override
    public void onDisable() {
        Bukkit.getServer().getScheduler().cancelTasks(this);
        Bukkit.getServer().getConsoleSender().sendMessage("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Bukkit.getServer().getConsoleSender().sendMessage("Developed for protocolmc.com");
        Bukkit.getServer().getConsoleSender().sendMessage("Version " + VERSION);
        Bukkit.getServer().getConsoleSender().sendMessage("PlateLauncher is now disabled");
        Bukkit.getServer().getConsoleSender().sendMessage("~~~~~~~~~~~~~~~~[PL]~~~~~~~~~~~~~~~~");
    }

    private void everySecond() {
        for (Integer key : this.entities.keySet()) {
            if (((LaunchedEntity) this.entities.get(key)).fallTicks > 0) {
                --((LaunchedEntity) this.entities.get(key)).fallTicks;
                if (((LaunchedEntity) this.entities.get(key)).fallTicks == 0 && ((LaunchedEntity) this.entities.get(key)).isPlayer && this.ncp) {
                    this.getServer().dispatchCommand(Bukkit.getConsoleSender(), "ncp unexempt " + key + " moving_survivalfly");
                }
            }
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.players.put(event.getPlayer().getName(), new PlayerInfo());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.players.remove(event.getPlayer().getName());
    }

    public void perTick() {
        Iterator var2 = this.plates.keySet().iterator();

        while (true) {
            Location loc;
            do {
                do {
                    if (!var2.hasNext()) {
                        return;
                    }

                    loc = (Location) var2.next();
                } while (loc == null);
            } while (loc.getWorld() == null);

            Iterator var4 = loc.getWorld().getEntities().iterator();

            while (var4.hasNext()) {
                Entity e = (Entity) var4.next();
                Location l = e.getLocation();
                if (l.getBlockX() == loc.getBlockX() && l.getBlockY() == loc.getBlockY() && l.getBlockZ() == loc.getBlockZ()) {
                    ((PressurePlate) this.plates.get(loc)).launch(e);
                }
            }
        }
    }

    @EventHandler
    void onBlockPlace(BlockPlaceEvent event) {
        if (((PlayerInfo) this.players.get(event.getPlayer().getName())).pplMode && (event.getBlockPlaced().getType() == Material.STONE_PLATE || event.getBlockPlaced().getType() == Material.WOOD_PLATE)) {
            this.plates.put(event.getBlockPlaced().getLocation(), new PressurePlate(((PlayerInfo) this.players.get(event.getPlayer().getName())).yPower, ((PlayerInfo) this.players.get(event.getPlayer().getName())).xPower));
            ((PressurePlate) this.plates.get(event.getBlockPlaced().getLocation())).loc = event.getBlockPlaced().getLocation();
            event.getPlayer().sendMessage("§2Added plate with x power of " + ((PlayerInfo) this.players.get(event.getPlayer().getName())).xPower + " and a y power of " + ((PlayerInfo) this.players.get(event.getPlayer().getName())).yPower + ".");
        }

    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
        if (((PlayerInfo) this.players.get(event.getPlayer().getName())).pplMode && (event.getBlock().getType() == Material.STONE_PLATE || event.getBlock().getType() == Material.WOOD_PLATE) && this.plates.containsKey(event.getBlock().getLocation())) {
            this.plates.remove(event.getBlock().getLocation());
            event.getPlayer().sendMessage("§cRemoved plate!");
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("platelauncher") || cmd.getName().equalsIgnoreCase("launcher") || cmd.getName().equalsIgnoreCase("plate")) && args.length >= 1) {
            if(args[0].equalsIgnoreCase("reload")){
                if (sender.hasPermission("platelauncher.admin")) {
                    Bukkit.getPluginManager().disablePlugin(this);
                    Bukkit.getPluginManager().enablePlugin(this);
                    sender.sendMessage(ChatColor.GREEN.toString() + "[PL] PL was successfully reloaded!");
                } else {
                    sender.sendMessage(ChatColor.RED + "[PL] No permission to execute this command!");
                }
            }
            else if (args[0].equalsIgnoreCase("toggle")) {
                if (sender instanceof Player && (sender.isOp() || sender.hasPermission("platelauncher.admin"))) {
                    if (((PlayerInfo) this.players.get(sender.getName())).pplMode) {
                        ((PlayerInfo) this.players.get(sender.getName())).pplMode = false;
                        sender.sendMessage("Turned pl mode off!");
                    } else {
                        ((PlayerInfo) this.players.get(sender.getName())).pplMode = true;
                        sender.sendMessage("Turned pl mode on!");
                    }
                }
            } else if (!args[0].equalsIgnoreCase("setpower") || args.length != 3 || !sender.isOp() && !sender.hasPermission("platelauncher.admin")) {
                Player player;
                if (!args[0].equalsIgnoreCase("launch") || args.length != 4 || !sender.isOp() && !sender.hasPermission("platelauncher.launch")) {
                    if (args[0].equalsIgnoreCase("saveplates") && (sender.isOp() || sender.hasPermission("platelauncher.admin"))) {
                        List<String> plateList = new ArrayList<>();
                        Iterator var14 = this.plates.keySet().iterator();

                        extractPlates(plateList, var14);

                        this.getConfig().set("plates", plateList);
                        this.saveConfig();
                        sender.sendMessage("§aSaved!");
                    } else if (args[0].equalsIgnoreCase("info") && sender instanceof Player && (sender.isOp() || sender.hasPermission("platelauncher.admin"))) {
                        player = ((Player) sender).getPlayer();
                        Block block = player.getTargetBlock((HashSet<Byte>) null, 100);
                        if (block == null) {
                            sender.sendMessage("§cInvalid plate");
                        }

                        if (this.plates.containsKey(block.getLocation())) {
                            sender.sendMessage("§aX Power:" + ((PressurePlate) this.plates.get(block.getLocation())).xPower + " Y Power:" + ((PressurePlate) this.plates.get(block.getLocation())).yPower);
                        } else {
                            sender.sendMessage("§cInvalid plate");
                        }
                    }
                } else {
                    player = null;
                    Player[] var9;
                    int var8 = (var9 = this.getServer().getOnlinePlayers().toArray(new Player[0])).length;

                    for (int var7 = 0; var7 < var8; ++var7) {
                        Player playerName = var9[var7];
                        if (playerName.getName().equalsIgnoreCase(args[1])) {
                            player = playerName;
                        }
                    }

                    if (player != null) {
                        this.launch(Double.valueOf(args[2] + "d"), Double.valueOf(args[3] + "d"), player);
                        sender.sendMessage("§aLaunched player!");
                    } else {
                        sender.sendMessage("§cInvalid player");
                    }
                }
            } else if (sender instanceof Player) {
                ((PlayerInfo) this.players.get(sender.getName())).xPower = Double.valueOf(args[1] + "d");
                ((PlayerInfo) this.players.get(sender.getName())).yPower = Double.valueOf(args[2] + "d");
                sender.sendMessage("§2Set x and y power!");
            }
        }

        return false;
    }

    private void extractPlates(List<String> plateList, Iterator var14) {
        while (var14.hasNext()) {
            Location key = (Location) var14.next();

            try {
                plateList.add(((PressurePlate) this.plates.get(key)).loc.getWorld().getName() + "|" + ((PressurePlate) this.plates.get(key)).loc.getBlockX() + "|" + ((PressurePlate) this.plates.get(key)).loc.getBlockY() + "|" + ((PressurePlate) this.plates.get(key)).loc.getBlockZ() + "|" + ((PressurePlate) this.plates.get(key)).xPower + "|" + ((PressurePlate) this.plates.get(key)).yPower);
            } catch (Exception var10) {
                var10.printStackTrace();
            }
        }
    }

    private void launch(Double xPow, Double yPow, Entity player) {
        this.getLogger().info("XPow:" + xPow + " YPow: " + yPow);
        if (this.ncp && player instanceof Player) {
            this.getServer().dispatchCommand(Bukkit.getConsoleSender(), "ncp exempt " + ((Player) player).getName() + " moving_survivalfly");
        }

        this.entities.put(player.getEntityId(), new LaunchedEntity());
        ((LaunchedEntity) this.entities.get(player.getEntityId())).fallTicks = 10;
        if (player.getType() == EntityType.PLAYER) {
            ((LaunchedEntity) this.entities.get(player.getEntityId())).isPlayer = true;
        }

        float pitch = player.getLocation().getPitch();
        Location pitchLoc = player.getLocation();
        pitchLoc.setPitch(0.0F);
        player.setVelocity(pitchLoc.getDirection().multiply(xPow));
        player.setVelocity(player.getVelocity().setY(yPow));
        if (player.isInsideVehicle()) {
            player.getVehicle().setVelocity(pitchLoc.getDirection().multiply(xPow));
            player.getVehicle().setVelocity(player.getVelocity().setY(yPow));
        }

        player.getLocation().setPitch(pitch);
    }

    @EventHandler
    void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER && this.entities.containsKey(event.getEntity().getEntityId()) && event.getCause() == DamageCause.FALL) {
            event.setCancelled(true);
            event.setDamage(0.0D);
        }

    }
}
