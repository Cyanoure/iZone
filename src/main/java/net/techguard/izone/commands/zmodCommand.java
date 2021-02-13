package net.techguard.izone.Commands;

import net.milkbowl.vault.economy.Economy;
import net.techguard.izone.Commands.zmod.*;
import net.techguard.izone.Configuration.ConfigManager;
import net.techguard.izone.Managers.VaultManager;
import net.techguard.izone.Managers.ZoneManager;
import net.techguard.izone.Utils.MenuBuilder.ItemBuilder;
import net.techguard.izone.Utils.MenuBuilder.PageInventory;
import net.techguard.izone.Utils.MenuBuilder.inventory.InventoryMenuBuilder;
import net.techguard.izone.Utils.MenuBuilder.inventory.InventoryMenuListener;
import net.techguard.izone.Variables;
import net.techguard.izone.Zones.Flags;
import net.techguard.izone.Zones.Zone;
import net.techguard.izone.iZone;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static net.techguard.izone.Utils.Localization.I18n.tl;

public class zmodCommand extends BaseCommand {
	private static zmodCommand instance;
	private final ArrayList<zmodBase> coms = new ArrayList<>();

	private Zone                  zone;
	private InventoryMenuListener settingsMenuListener;
	private InventoryMenuListener flagsMenuListener;
	private InventoryMenuListener membersMenuListener;
	private iZone iZoneInstance;

	public zmodCommand(iZone instance) {
		super(instance);
		iZoneInstance = instance;
		this.coms.add(new listCommand(instance));
		this.coms.add(new whoCommand(instance));
		this.coms.add(new infoCommand(instance));
		this.coms.add(new createCommand(instance));
		this.coms.add(new deleteCommand(instance));
		this.coms.add(new helpCommand(instance));
		this.coms.add(new allowCommand(instance));
		this.coms.add(new disallowCommand(instance));
		this.coms.add(new flagCommand(instance));
		this.coms.add(new parentCommand(instance));
		this.coms.add(new expandCommand(instance));
		this.coms.add(new visualiseCommand(instance));

		zmodCommand.instance = this;

		settingsMenuListener = (player, action, event) ->
		{
			if (event.getAction() != InventoryAction.PICKUP_ALL) {
				event.setCancelled(true);

				player.closeInventory();
				player.openInventory(event.getClickedInventory());

				return;
			}

			ItemStack item = event.getInventory().getItem(event.getSlot());
			if (item.getType().name().contains("SIGN")) {
				InventoryMenuBuilder imb = new InventoryMenuBuilder(InventoryType.PLAYER).withTitle(tl("gui_zone_management") + " - " + tl("gui_flags"));
				int i = 0;
				for (Flags flag : zone.getAllFlags()) {
					imb.withItem(i, new ItemBuilder(Material.OAK_SIGN).setTitle(ChatColor.WHITE + "" + ChatColor.BOLD + flag.getName()).addLore(ChatColor.GREEN + "" + ChatColor.BOLD + (zone.hasFlag(flag) ? tl("gui_on") : ChatColor.RED + "" + ChatColor.BOLD + tl("gui_off"))).build());
					i++;
				}

				imb.show(player);
				imb.onInteract(flagsMenuListener, ClickType.LEFT);
			} else if (item.getType() == Material.SKELETON_SKULL) { // ? idk
				InventoryMenuBuilder imb = new InventoryMenuBuilder(InventoryType.PLAYER).withTitle(tl("gui_zone_management") + " - " + tl("gui_allowed_players"));

				int i = 0;
				for (String member : zone.getAllowed()) {
					if (member.startsWith("o:")) {
						continue;
					}
					imb.withItem(i, new ItemBuilder(Material.SKELETON_SKULL, (short) 3).setTitle(ChatColor.WHITE + "" + ChatColor.BOLD + member).addLore(ChatColor.RED + "" + ChatColor.BOLD + tl("gui_remove_member_lore")).build());
					i++;
				}

				imb.show(player);
				imb.onInteract(membersMenuListener, ClickType.LEFT);
			} else if (item.getType() == Material.LAVA_BUCKET || item.getType() == Material.BARRIER) {
				player.closeInventory();
				Bukkit.dispatchCommand(player, "zmod delete " + zone.getName());
			}
		};

		flagsMenuListener = (player, action, event) ->
		{
			if (event.getAction() != InventoryAction.PICKUP_ALL) {
				event.setCancelled(true);

				player.closeInventory();
				player.openInventory(event.getClickedInventory());

				return;
			}

			ItemStack flagItem = event.getCurrentItem();

			for (Flags flag : zone.getAllFlags()) {
				if (flag.getName().equals(ChatColor.stripColor(flagItem.getItemMeta().getDisplayName()))) {
					if (!player.hasPermission(Variables.PERMISSION_FLAGS + flag.toString())) {
						player.sendMessage(iZone.getPrefix() + tl("zone_flag_no_permission"));
						return;
					}

					if (zone.hasFlag(flag)) {
						zone.setFlag(flag.getId(), false);
					} else {
						zone.setFlag(flag.getId(), true);
					}

					if (flag.getName().equalsIgnoreCase("gamemode") && zone.hasFlag(flag)) {
						player.sendMessage(iZone.getPrefix() + tl("flag_gamemode_default"));
						player.sendMessage(iZone.getPrefix() + tl("flag_gamemode_values"));
						player.sendMessage(iZone.getPrefix() + tl("flag_gamemode_help", "/zmod flag " + zone.getName() + " gamemode YourGamemode"));
						zone.setGamemode(GameMode.SURVIVAL);
					}

					if (flag.getName().equalsIgnoreCase("welcome") && zone.hasFlag(flag)) {
						player.sendMessage(iZone.getPrefix() + tl("flag_welcome_default"));
						player.sendMessage(iZone.getPrefix() + tl("flag_welcome_help", "/zmod flag " + zone.getName() + " welcome YourMessage"));
						zone.setWelcome("Welcome to my zone");
					}

					if (flag.getName().equalsIgnoreCase("farewell") && zone.hasFlag(flag)) {
						player.sendMessage(iZone.getPrefix() + tl("flag_farewell_default"));
						player.sendMessage(iZone.getPrefix() + tl("flag_farewell_help", "/zmod flag " + zone.getName() + " farewell YourMessage"));
						zone.setFarewell("See you soon");
					}

					player.sendMessage(iZone.getPrefix() + tl("flag_set", flag.getName(), (zone.hasFlag(flag) ? ChatColor.GREEN + "" + ChatColor.BOLD + tl("gui_on") : ChatColor.RED + "" + ChatColor.BOLD + tl("gui_off"))));
					event.getInventory().setItem(event.getSlot(), new ItemBuilder(Material.OAK_SIGN).setTitle(ChatColor.WHITE + "" + ChatColor.BOLD + flag.getName()).addLore(ChatColor.GREEN + "" + ChatColor.BOLD + (zone.hasFlag(flag) ? tl("gui_on") : ChatColor.RED + "" + ChatColor.BOLD + tl("gui_off"))).build());

					player.updateInventory();
				}
			}
		};

		membersMenuListener = (player, action, event) ->
		{
			if (event.getAction() != InventoryAction.PICKUP_ALL) {
				event.setCancelled(true);

				player.closeInventory();
				player.openInventory(event.getClickedInventory());

				return;
			}

			ItemStack memberItem = event.getCurrentItem();

			String target = ChatColor.stripColor(memberItem.getItemMeta().getDisplayName());

			if (zone.getAllowed().contains(target)) {
				if (ConfigManager.isVaultEnabled()) {
					Economy vault = VaultManager.instance;

					if (vault.has(Bukkit.getOfflinePlayer(player.getUniqueId()), ConfigManager.getDisallowPlayerPrice())) {
						vault.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), ConfigManager.getDisallowPlayerPrice());
					} else {
						player.sendMessage(iZone.getPrefix() + tl("notenough_money", vault.format(ConfigManager.getDisallowPlayerPrice())));
						return;
					}
				}
				zone.Remove(target);
				event.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
				player.updateInventory();
				player.sendMessage(iZone.getPrefix() + tl("zone_removeuser", target));
			} else {
				player.sendMessage(iZone.getPrefix() + tl("zone_cantremoveuser"));
			}
		};
	}

	public static zmodCommand getInstance() {
		return instance;
	}

	public ArrayList<zmodBase> getComs() {
		return this.coms;
	}

	public void onPlayerCommand(Player player, String[] cmd) {
		if(ZoneManager.IsDisabledWorld(player.getWorld())) {
			player.sendMessage(iZone.getPrefix() + tl("world_disabled"));
			return;
		}

		if (cmd.length == 1) {
			ArrayList<Zone> zones = ZoneManager.getZones().stream().filter(zone -> zone.getOwners().contains(player.getName())).collect(Collectors.toCollection(ArrayList::new));

			if(zones.size() == 0){ // print help if no claimed zones
				helpCommand helpCMD = new helpCommand(iZoneInstance);
				helpCMD.onCommand(player, cmd);
				return;
			}

			ArrayList<ItemStack> items = new ArrayList<>();
			for (Zone zone1 : zones) {
				items.add(new ItemBuilder(Variables.getMyHouseItem()).setTitle(ChatColor.WHITE + "" + ChatColor.BOLD + zone1.getName()).addLore("§8", ChatColor.GRAY + "" + ChatColor.BOLD + "[LEFT CLICK]" + ChatColor.GREEN + " To manage the zone.", ChatColor.GRAY + "" + ChatColor.BOLD + "[SHIFT + LEFT CLICK]" + ChatColor.GREEN + " To teleport to the zone border.").build());
			}

			PageInventory pageInventory = new PageInventory(tl("gui_main_title"), items);
			pageInventory.show(player);

			pageInventory.onInteract((player1, action, event) ->
			{
				if (event.getAction() != InventoryAction.PICKUP_ALL && event.getClick() != ClickType.SHIFT_LEFT) {
					event.setCancelled(true);

					player.closeInventory();
					player.openInventory(event.getClickedInventory());

					return;
				}

				ItemStack item = pageInventory.getInventory().getItem(event.getSlot());

				if(item == null)
				{
					event.setCancelled(true);

					player.closeInventory();
					player.openInventory(event.getClickedInventory());

					return;
				}

				int newPage = 0;
				if (item.equals(pageInventory.getBackPage())) {
					newPage = -1;
				} else if (item.equals(pageInventory.getForwardsPage())) {
					newPage = 1;
				}
				if (newPage != 0) {
					pageInventory.setPage(pageInventory.getCurrentPage() + newPage);
					return;
				}

				zone = ZoneManager.getZone(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
				if (zone == null) return;

				if (action == ClickType.SHIFT_LEFT) {
					Location loc = zone.getTeleport();
					if (loc == null) {
						player.sendMessage(iZone.getPrefix() + tl("zone_teleport_not_set"));
						return;
					}

					if (!isSafeLocation(loc)) {
						player.teleport(loc);
					} else {
						player.teleport(player.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ()).getLocation());
					}
					player.closeInventory();
					return;
				}

				InventoryMenuBuilder imb = new InventoryMenuBuilder(9, tl("gui_zone_management"));
				imb.withItem(0, new ItemBuilder(Material.OAK_SIGN).setTitle(ChatColor.WHITE + "" + ChatColor.BOLD + tl("gui_button_flags")).addLore(ChatColor.GREEN + "" + ChatColor.BOLD + tl("gui_set_flag_lore")).build());
				imb.withItem(4, new ItemBuilder(Material.SKELETON_SKULL, (short) 3).setTitle(ChatColor.WHITE + "" + ChatColor.BOLD + tl("gui_button_allowed_players")).addLore(ChatColor.GREEN + "" + ChatColor.BOLD + tl("gui_add_players_lore")).build());

				imb.withItem(8, new ItemBuilder(Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1) ? Material.BARRIER : Material.LAVA_BUCKET).setTitle(ChatColor.RED + "" + ChatColor.BOLD + tl("gui_button_delete_zone")).addLore(ChatColor.RED + "" + ChatColor.BOLD + tl("gui_remove_zone")).build());

				imb.show(player);
				imb.onInteract(settingsMenuListener, ClickType.LEFT);
			}, ClickType.LEFT, ClickType.SHIFT_LEFT);

			player.sendMessage(iZone.getPrefix() + tl("chat_help", "/zmod help"));
		} else {
			for (zmodBase zmod : this.coms) {
				if (zmod.getInfo()[0].equalsIgnoreCase(cmd[1])) {
					boolean permission = player.hasPermission(zmod.getPermission());
					if ((zmod instanceof listCommand)) {
						permission = (permission) || (player.hasPermission(Variables.PERMISSION_LIST_ALL));
					}
					if (permission) {
						if (cmd.length < zmod.getLength()) {
							if ((zmod instanceof flagCommand)) {
								player.sendMessage(((flagCommand) zmod).getError(player, cmd.length));
							} else {
								player.sendMessage(zmod.getError(cmd.length));
							}
						} else {
							zmod.onCommand(player, cmd);
						}
					} else {
						player.sendMessage(iZone.getPrefix() + tl("chat_nopermission"));
					}
				}
			}
		}
	}

	public void onSystemCommand(ConsoleCommandSender player, String[] cmd) {
		player.sendMessage(tl("only_ingame"));
	}

	protected String[] getUsage() {
		return new String[]{"zmod"};
	}

	private boolean isSafeLocation(Location location) {

		Block feet = location.getBlock();
		if (!feet.getType().isTransparent() && !feet.getLocation().add(0, 1, 0).getBlock().getType().isTransparent()) {
			return false;
		}

		Block head = feet.getRelative(BlockFace.UP);
		if (!head.getType().isTransparent()) {
			return false;
		}

		Block ground = feet.getRelative(BlockFace.DOWN);
		if (!ground.getType().isSolid()) {
			return false;
		}
		return true;
	}
}
