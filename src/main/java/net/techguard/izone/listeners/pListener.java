package net.techguard.izone.Listeners;

import net.techguard.izone.Configuration.ConfigManager;
import net.techguard.izone.Managers.InvManager;
import net.techguard.izone.Managers.ZoneManager;
import net.techguard.izone.Utils.MenuBuilder.ItemBuilder;
import net.techguard.izone.Utils.MenuBuilder.inventory.InventoryMenuBuilder;
import net.techguard.izone.Utils.MessagesAPI;
import net.techguard.izone.Variables;
import net.techguard.izone.Zones.Flags;
import net.techguard.izone.Zones.Settings;
import net.techguard.izone.Zones.Zone;
import net.techguard.izone.iZone;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.techguard.izone.Utils.Localization.I18n.tl;

public class pListener implements Listener {

	private CopyOnWriteArrayList<UUID> rightClickPlayers = new CopyOnWriteArrayList<>();

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (/*Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1) && */event.getHand() != EquipmentSlot.HAND) {
			return;
		}

		Player player = event.getPlayer();
		if (ZoneManager.IsDisabledWorld(player.getWorld())) {
			return;
		}

		Settings  sett   = Settings.getSett(player);
		ItemStack inHand = /*Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1) ? */player.getInventory().getItemInMainHand()/* : player.getItemInHand()*/;
		if (inHand == null) {
			return;
		}

		int      set     = -1;
		Location clicked = event.getClickedBlock().getLocation();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if ((player.hasPermission(Variables.PERMISSION_CHECK)) && (inHand.getType() == Material.matchMaterial(ConfigManager.getCheckTool()))) {
				if (rightClickPlayers.contains(player.getUniqueId())) {
					event.setCancelled(true);
					return;
				}

				rightClickPlayers.add(player.getUniqueId());
				iZone.instance.getServer().getScheduler().runTaskLaterAsynchronously(iZone.instance, () -> rightClickPlayers.remove(player.getUniqueId()), 20L);

				iZone.instance.sendInfo(player, clicked);
				event.setCancelled(true);
			} else if ((player.hasPermission(Variables.PERMISSION_DEFINE)) && (inHand.getType() == Material.matchMaterial(ConfigManager.getDefineTool()))) {
				set = 2;
				sett.setBorder(2, clicked);
				player.sendMessage(iZone.getPrefix() + tl("zone_position_2_set", clicked.getBlockX(), clicked.getBlockY(), clicked.getBlockZ()));
				event.setCancelled(true);
			}
		} else if ((event.getAction() == Action.LEFT_CLICK_BLOCK) &&
				(player.hasPermission(Variables.PERMISSION_DEFINE)) && (inHand.getType() == Material.matchMaterial(ConfigManager.getDefineTool()))) {
			set = 1;
			sett.setBorder(1, clicked);
			player.sendMessage(iZone.getPrefix() + tl("zone_position_1_set", clicked.getBlockX(), clicked.getBlockY(), clicked.getBlockZ()));
			event.setCancelled(true);
		}

		if ((set != -1 && ConfigManager.getAutoExpandEnabled()) && (sett.getBorder1() != null && sett.getBorder2() != null)) {
			//if (sett.getBorder1().getY() == sett.getBorder2().getY()) {
			if (sett.getBorder1().getWorld().equals(sett.getBorder2().getWorld())) {
				int maxHeight = (int) sett.getMaxSize().getY();
				if (maxHeight == -1) {
					maxHeight = player.getWorld().getMaxHeight();
				}

				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);

				InventoryMenuBuilder imb = new InventoryMenuBuilder(27).withTitle(tl("gui_autoexpand"));
				imb.withItem(11, new ItemBuilder(Material.RED_STAINED_GLASS, (short) 14).setTitle(tl("gui_no")).build());
				imb.withItem(15, new ItemBuilder(Material.GREEN_STAINED_GLASS, (short) 13).setTitle(tl("gui_yes")).build());
				imb.show(player);

				int finalMaxHeight = maxHeight;
				int finalSet       = set;
				imb.onInteract((_player, _action, _event) -> {
					if (_event.getCurrentItem().getDurability() == (short) 13) {
						autoExpandZone(_player, clicked, finalSet, finalMaxHeight);
						_player.sendMessage(iZone.getPrefix() + tl("zone_position_" + finalSet + "_set", clicked.getBlockX(), finalMaxHeight, clicked.getBlockZ()));
					}
					_player.closeInventory();
				}, ClickType.LEFT);
			}
		}

		Zone zone = ZoneManager.getZone(clicked);

		if (zone != null) {
			if (!ZoneManager.checkPermission(zone, player, Flags.INTERACT)) {
				event.setCancelled(true);
				player.sendMessage(iZone.getPrefix() + tl("zone_protected"));
			}

			if (event.getAction() == Action.PHYSICAL && !ZoneManager.checkPermission(zone, player, Flags.PROTECTION)) {
				Block block = event.getClickedBlock();
				if (block == null) {
					return;
				}
				if (block.getType() == Material.FARMLAND) {
					player.sendMessage(iZone.getPrefix() + tl("zone_protected"));
					event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
					event.setCancelled(true);

					block.setType(block.getType());
					block.setBlockData(block.getBlockData());
					//block.setTypeIdAndData(block.getType().getId(), block.getData(), true);
				}
			}
		}
	}

	private void autoExpandZone(Player player, Location defaultLocation, int finalSet, int maxHeight) {
		Settings settings = Settings.getSett(player);
		Location b1 = settings.getBorder1();
		Location b2 = settings.getBorder2();
		settings.setBorder(1,new Location(defaultLocation.getWorld(),b1.getBlockX(), maxHeight, b1.getBlockZ()));
		settings.setBorder(2,new Location(defaultLocation.getWorld(),b2.getBlockX(),0, b2.getBlockZ()));
		//settings.setBorder(finalSet, new Location(defaultLocation.getWorld(), defaultLocation.getBlockX(), maxHeight, defaultLocation.getBlockZ()));
		//System.out.println(settings.getBorder1().toString());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (ZoneManager.IsDisabledWorld(player.getWorld())) {
			return;
		}


		Location from = event.getFrom();
		from = new Location(from.getWorld(), from.getBlockX(), from.getBlockY(), from.getBlockZ(), from.getYaw(), from.getPitch());
		Location to = event.getTo();
		to = new Location(to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ(), to.getYaw(), to.getPitch());

		Zone fzone = ZoneManager.getZone(from);
		Zone tzone = ZoneManager.getZone(to);

		if ((tzone != fzone) && (tzone != null) && (tzone.hasFlag(Flags.RESTRICTION)) &&
				(!ZoneManager.checkPermission(tzone, player, Flags.RESTRICTION))) {
			player.sendMessage(iZone.getPrefix() + tl("zone_restricted"));
			event.setCancelled(true);
			return;
		}

		if ((fzone != tzone) && (fzone != null) && (fzone.hasFlag(Flags.JAIL)) &&
				(!ZoneManager.checkPermission(fzone, player, Flags.JAIL))) {
			player.sendMessage(iZone.getPrefix() + tl("zone_protected"));
			event.setCancelled(true);
			return;
		}

		if ((fzone != tzone) && (fzone != null)) {
			if (fzone.hasFlag(Flags.FAREWELL)) {
				String s = fzone.getFarewell();
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.GRAY + fzone.getName() + " > " + ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', s));

				if (/*Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1) && */ConfigManager.getTitlesEnabled()) {
					MessagesAPI.sendTitle(player, ConfigManager.getTitleFadeIn(), ConfigManager.getTitleStay(), ConfigManager.getTitleFadeOut(), "", ChatColor.YELLOW + s);
				}
			}

			if ((fzone.hasFlag(Flags.GAMEMODE)) && (player.getServer().getDefaultGameMode() != player.getGameMode())) {
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + fzone.getName() + "> " + ChatColor.GRAY + tl("zone_gamemode"));
				player.setGameMode(player.getServer().getDefaultGameMode());
			}

			if (fzone.hasFlag(Flags.GIVEITEM_OUT)) {
				ArrayList<ItemStack> inventory = fzone.getInventory(Flags.GIVEITEM_OUT);
				for (ItemStack item : inventory) {
					InvManager.addToInventory(player.getInventory(), item);
				}
			}
			if (fzone.hasFlag(Flags.TAKEITEM_OUT)) {
				ArrayList<ItemStack> inventory = fzone.getInventory(Flags.TAKEITEM_OUT);
				for (ItemStack item : inventory) {
					InvManager.removeFromInventory(player.getInventory(), item);
				}
			}
			if (fzone.hasFlag(Flags.TAKEEFFECT_OUT)) {
				ArrayList<PotionEffect> effects = fzone.getEffects(Flags.TAKEEFFECT_OUT);
				for (PotionEffect effect : effects) {
					player.removePotionEffect(effect.getType());
				}
			}
			if (fzone.hasFlag(Flags.GIVEEFFECT_OUT)) {
				ArrayList<PotionEffect> effects = fzone.getEffects(Flags.GIVEEFFECT_OUT);
				for (PotionEffect effect : effects) {
					player.addPotionEffect(effect);
				}
			}
		}
		if ((tzone != fzone) && (tzone != null)) {
			if (tzone.hasFlag(Flags.WELCOME)) {
				String s = tzone.getWelcome();
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.GRAY + tzone.getName() + " > " + ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', s));
				if (/*Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1) && */ConfigManager.getTitlesEnabled()) {
					MessagesAPI.sendTitle(player, ConfigManager.getTitleFadeIn(), ConfigManager.getTitleStay(), ConfigManager.getTitleFadeOut(), "", ChatColor.YELLOW + s);
				}
			}
			if ((tzone.hasFlag(Flags.GAMEMODE)) && (tzone.getGamemode() != player.getGameMode())) {
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + tzone.getName() + "> " + ChatColor.GRAY + tl("zone_gamemode"));
				player.setGameMode(tzone.getGamemode());
			}

			if (tzone.hasFlag(Flags.GIVEITEM_IN)) {
				ArrayList<ItemStack> inventory = tzone.getInventory(Flags.GIVEITEM_IN);
				for (ItemStack item : inventory) {
					InvManager.addToInventory(player.getInventory(), item);
				}
			}
			if (tzone.hasFlag(Flags.TAKEITEM_IN)) {
				ArrayList<ItemStack> inventory = tzone.getInventory(Flags.TAKEITEM_IN);
				for (ItemStack item : inventory) {
					InvManager.removeFromInventory(player.getInventory(), item);
				}
			}
			if (tzone.hasFlag(Flags.TAKEEFFECT_IN)) {
				ArrayList<PotionEffect> effects = tzone.getEffects(Flags.TAKEEFFECT_IN);
				for (PotionEffect effect : effects) {
					player.removePotionEffect(effect.getType());
				}
			}
			if (tzone.hasFlag(Flags.GIVEEFFECT_IN)) {
				ArrayList<PotionEffect> effects = tzone.getEffects(Flags.GIVEEFFECT_IN);
				for (PotionEffect effect : effects) {
					player.addPotionEffect(effect);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (ZoneManager.IsDisabledWorld(player.getWorld())) {
			return;
		}


		Location from = event.getFrom();
		from = new Location(from.getWorld(), from.getBlockX(), from.getBlockY(), from.getBlockZ(), from.getYaw(), from.getPitch());
		Location to = event.getTo();
		to = new Location(to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ(), to.getYaw(), to.getPitch());

		if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
			return;
		}
		Zone fzone = ZoneManager.getZone(from);
		Zone tzone = ZoneManager.getZone(to);

		if (tzone != fzone && tzone != null && tzone.hasFlag(Flags.RESTRICTION) && !ZoneManager.checkPermission(tzone, player, Flags.RESTRICTION)) {
			player.sendMessage(iZone.getPrefix() + tl("zone_protected"));
			from.setX(from.getBlockX() + 0.5D);
			from.setY(from.getBlockY() + 0.0D);
			from.setZ(from.getBlockZ() + 0.5D);
			player.teleport(from);
			return;
		}

		if (fzone != tzone && fzone != null && fzone.hasFlag(Flags.JAIL) && !ZoneManager.checkPermission(fzone, player, Flags.JAIL)) {
			player.sendMessage(iZone.getPrefix() + tl("zone_protected"));
			from.setX(from.getBlockX() + 0.5D);
			from.setY(from.getBlockY() + 0.0D);
			from.setZ(from.getBlockZ() + 0.5D);
			player.teleport(from);
			return;
		}

		if ((fzone != tzone) && (fzone != null)) {
			if (fzone.hasFlag(Flags.FAREWELL)) {
				String s = fzone.getFarewell();
				player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + fzone.getName() + " > " + ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', s));
				if (/*Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1) && */ConfigManager.getTitlesEnabled()) {
					MessagesAPI.sendTitle(player, ConfigManager.getTitleFadeIn(), ConfigManager.getTitleStay(), ConfigManager.getTitleFadeOut(), "", ChatColor.YELLOW + s);
				}
			}
			if (fzone.hasFlag(Flags.GAMEMODE) && player.getServer().getDefaultGameMode() != player.getGameMode()) {
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + fzone.getName() + "> " + ChatColor.GRAY + tl("zone_gamemode"));
				player.setGameMode(player.getServer().getDefaultGameMode());
			}
			if (fzone.hasFlag(Flags.FLY) && player.isFlying() && (!player.isOp() && (!player.hasPermission(Variables.PERMISSION_FLY + fzone.getName()) || !player.hasPermission(Variables.PERMISSION_FLY + "*")))) {
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + fzone.getName() + "> " + ChatColor.GRAY + tl("zone_fly"));
				player.setFlying(false);
			}

			if (fzone.hasFlag(Flags.GIVEITEM_OUT)) {
				ArrayList<ItemStack> inventory = fzone.getInventory(Flags.GIVEITEM_OUT);
				for (ItemStack item : inventory) {
					InvManager.addToInventory(player.getInventory(), item);
				}
			}
			if (fzone.hasFlag(Flags.TAKEITEM_OUT)) {
				ArrayList<ItemStack> inventory = fzone.getInventory(Flags.TAKEITEM_OUT);
				for (ItemStack item : inventory) {
					InvManager.removeFromInventory(player.getInventory(), item);
				}
			}
			if (fzone.hasFlag(Flags.TAKEEFFECT_OUT)) {
				ArrayList<PotionEffect> effects = fzone.getEffects(Flags.TAKEEFFECT_OUT);
				for (PotionEffect effect : effects) {
					player.removePotionEffect(effect.getType());
				}
			}
			if (fzone.hasFlag(Flags.GIVEEFFECT_OUT)) {
				ArrayList<PotionEffect> effects = fzone.getEffects(Flags.GIVEEFFECT_OUT);
				for (PotionEffect effect : effects) {
					player.addPotionEffect(effect);
				}
			}
		}
		if (tzone != fzone && tzone != null) {
			if (tzone.hasFlag(Flags.WELCOME)) {
				String s = tzone.getWelcome();
				player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + tzone.getName() + " > " + ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', s));
				if (/*Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1) && */ConfigManager.getTitlesEnabled()) {
					MessagesAPI.sendTitle(player, ConfigManager.getTitleFadeIn(), ConfigManager.getTitleStay(), ConfigManager.getTitleFadeOut(), "", ChatColor.YELLOW + s);
				}
			}
			if (tzone.hasFlag(Flags.GIVEITEM_IN)) {
				ArrayList<ItemStack> inventory = tzone.getInventory(Flags.GIVEITEM_IN);
				for (ItemStack item : inventory) {
					InvManager.addToInventory(player.getInventory(), item);
				}
			}
			if (tzone.hasFlag(Flags.TAKEITEM_IN)) {
				ArrayList<ItemStack> inventory = tzone.getInventory(Flags.TAKEITEM_IN);
				for (ItemStack item : inventory) {
					InvManager.removeFromInventory(player.getInventory(), item);
				}
			}
			if (tzone.hasFlag(Flags.TAKEEFFECT_IN)) {
				ArrayList<PotionEffect> effects = tzone.getEffects(Flags.TAKEEFFECT_IN);
				for (PotionEffect effect : effects) {
					player.removePotionEffect(effect.getType());
				}
			}
			if (tzone.hasFlag(Flags.GIVEEFFECT_IN)) {
				ArrayList<PotionEffect> effects = tzone.getEffects(Flags.GIVEEFFECT_IN);
				for (PotionEffect effect : effects) {
					player.addPotionEffect(effect);
				}
			}
		}
		if (tzone != null && tzone.hasFlag(Flags.GAMEMODE) && tzone.getGamemode() != player.getGameMode()) {
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + tzone.getName() + "> " + ChatColor.GRAY + tl("zone_gamemode"));
			player.setGameMode(tzone.getGamemode());
		}
		if (tzone != null && tzone.hasFlag(Flags.FLY) && player.isFlying() && (!player.isOp() || !player.hasPermission(Variables.PERMISSION_FLY + tzone.getName()) || !player.hasPermission(Variables.PERMISSION_FLY + "*"))) {
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + tzone.getName() + "> " + ChatColor.GRAY + tl("zone_fly"));
			player.setFlying(false);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (ZoneManager.IsDisabledWorld(player.getWorld())) {
			return;
		}

		Item item = event.getItemDrop();
		Zone zone = ZoneManager.getZone(item.getLocation());

		if ((zone != null) && (!ZoneManager.checkPermission(zone, player, Flags.DROP))) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		if (ZoneManager.IsDisabledWorld(player.getWorld())) {
			return;
		}

		Item item = event.getItem();
		Zone zone = ZoneManager.getZone(item.getLocation());

		if ((zone != null) && (!ZoneManager.checkPermission(zone, player, Flags.DROP))) {
			event.getItem().remove();
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		if (ZoneManager.IsDisabledWorld(player.getWorld())) {
			return;
		}

		Block b    = event.getBlockClicked().getRelative(event.getBlockFace());
		Zone  zone = ZoneManager.getZone(b.getLocation());

		if ((zone != null) && (!ZoneManager.checkPermission(zone, player, Flags.PROTECTION))) {
			event.setCancelled(true);
			player.sendMessage(iZone.getPrefix() + tl("zone_protected"));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		if (ZoneManager.IsDisabledWorld(player.getWorld())) {
			return;
		}

		Block b    = event.getBlockClicked().getRelative(event.getBlockFace());
		Zone  zone = ZoneManager.getZone(b.getLocation());

		if ((zone != null) && (!ZoneManager.checkPermission(zone, player, Flags.PROTECTION))) {
			event.setCancelled(true);
			player.sendMessage(iZone.getPrefix() + tl("zone_protected"));
		}
	}
}
