package net.techguard.izone.Commands.zmod;

import net.techguard.izone.Variables;
import net.techguard.izone.iZone;
import net.techguard.izone.Managers.ZoneManager;
import net.techguard.izone.Zones.Zone;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static net.techguard.izone.Utils.Localization.I18n.tl;

public class listCommand extends zmodBase {
	public listCommand(iZone instance) {
		super(instance);
	}

	public void onCommand(Player player, String[] cmd) {
		ArrayList<Zone> zones = ZoneManager.getZones();
		String          list  = "§b"+tl("zone_list")+"(§f" + zones.size() + "§b):";

		for (Zone zone : zones)
		{
			if ((!player.hasPermission(Variables.PERMISSION_LIST_ALL)) && (!zone.getOwners().contains(player.getName())))
			{
				continue;
			}
			list = list + " §f" + zone.getName() + "§b,";
		}

		if (list.endsWith("§b,"))
		{
			list = list.substring(0, list.length() - 3);
		}

		player.sendMessage(list);
	}

	public int getLength() {
		return 2;
	}

	public String[] getInfo() {
		return new String[]{"list", "", tl("help_list")};
	}

	public String getError(int i) {
		return "";
	}

	public String getPermission() {
		return Variables.PERMISSION_LIST;
	}
}