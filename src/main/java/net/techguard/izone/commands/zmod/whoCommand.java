package net.techguard.izone.Commands.zmod;

import net.techguard.izone.Variables;
import net.techguard.izone.iZone;
import net.techguard.izone.Managers.ZoneManager;
import net.techguard.izone.Zones.Zone;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static net.techguard.izone.Utils.Localization.I18n.tl;

public class whoCommand extends zmodBase {
	public whoCommand(iZone instance) {
		super(instance);
	}

	public void onCommand(Player player, String[] cmd) {
		Zone zone = ZoneManager.getZone(player.getLocation());
		if (zone == null)
		{
			player.sendMessage(iZone.getPrefix() + tl("zone_need_in_zone"));
			return;
		}
		int    Count = 0;
		String List  = "";
		for (Player p2 : player.getWorld().getPlayers())
		{
			if (ZoneManager.getZone(p2.getLocation()) == zone)
			{
				List = List + " " + ChatColor.WHITE + p2.getName() + ChatColor.AQUA + ",";
				Count++;
			}
		}
		if (List.endsWith(ChatColor.AQUA + ","))
		{
			List = List.substring(0, List.length() - 3);
		}
		player.sendMessage(ChatColor.AQUA + "" + zone.getName() + "(" + ChatColor.WHITE + Count + ChatColor.AQUA + "):" + List);
	}

	public int getLength() {
		return 2;
	}

	public String[] getInfo() {
		return new String[]{"who", "", tl("help_who")};
	}

	public String getError(int i) {
		return "";
	}

	public String getPermission() {
		return Variables.PERMISSION_WHO;
	}
}