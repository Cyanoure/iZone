package net.techguard.izone.Commands.zmod;

import net.techguard.izone.Variables;
import net.techguard.izone.iZone;
import net.techguard.izone.Managers.ZoneManager;
import net.techguard.izone.Zones.Zone;
import org.bukkit.entity.Player;

import static net.techguard.izone.Utils.Localization.I18n.tl;

public class parentCommand extends zmodBase {
	public parentCommand(iZone instance) {
		super(instance);
	}

	public void onCommand(Player player, String[] cmd) {
		Zone child  = null;
		Zone parent = null;

		if (ZoneManager.getZone(cmd[2]) != null)
		{
			child = ZoneManager.getZone(cmd[2]);
		}
		else
		{
			player.sendMessage(iZone.getPrefix() + tl("zone_not_a_zone", cmd[2]));
		}
		if (ZoneManager.getZone(cmd[3]) != null)
		{
			parent = ZoneManager.getZone(cmd[3]);
		}
		else
		{
			player.sendMessage(iZone.getPrefix() + tl("zone_not_a_zone", cmd[3]));
		}

		if ((child != null) && (parent != null))
		{
			if ((!child.getOwners().contains(player.getName())) && (!player.hasPermission(Variables.PERMISSION_OWNER)))
			{
				player.sendMessage(iZone.getPrefix() + tl("zone_notowner"));
				return;
			}
			child.setParent(parent.getName());
			player.sendMessage(iZone.getPrefix() + tl("zone_parent_set", parent.getName(), child.getName()));
		}
	}

	public int getLength() {
		return 4;
	}

	public String[] getInfo() {
		return new String[]{"parent", " <"+tl("help_param_child")+"> <"+tl("help_param_parent")+">", tl("help_parent")};
	}

	public String getError(int i) {
		return "§c"+tl("command_usage")+": /zmod parent <"+tl("help_param_child")+"> <"+tl("help_param_parent")+">";
	}

	public String getPermission() {
		return Variables.PERMISSION_PARENT;
	}
}