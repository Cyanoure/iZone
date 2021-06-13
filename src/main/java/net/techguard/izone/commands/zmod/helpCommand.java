package net.techguard.izone.Commands.zmod;

import net.techguard.izone.Variables;
import net.techguard.izone.Commands.zmodCommand;
import net.techguard.izone.iZone;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static net.techguard.izone.Utils.Localization.I18n.tl;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class helpCommand extends zmodBase {
	public helpCommand(iZone instance) {
		super(instance);
	}

	public void onCommand(Player player, String[] cmd) {
		player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "////////////// " + ChatColor.GOLD + "[" + "iZone" + "]" + ChatColor.GRAY + "" + ChatColor.BOLD + " //////////////");
		for (zmodBase zmod : zmodCommand.getInstance().getComs())
		{
			boolean permission = player.hasPermission(zmod.getPermission());
			if ((zmod instanceof listCommand))
			{
				permission = (permission) || (player.hasPermission(Variables.PERMISSION_LIST_ALL));
			}
			if (permission)
			{
				String[] info = zmod.getInfo();
				if (info.length == 3)
				{
					player.sendMessage("§b/zmod " + info[0] + info[1] + " §f- " + info[2]);
				}
				else if (info.length == 2)
				{
					player.sendMessage("§b/zmod " + info[0] + " §f- " + info[1]);
				}
				else if (info.length == 1)
				{
					player.sendMessage("§b/zmod " + info[0]);
				}
			}
		}
	}

	public int getLength() {
		return 1;
	}

	public String[] getInfo() {
		return new String[]{"help", tl("help_help")};
	}

	public String getError(int i) {
		return "§c"+tl("command_usage")+": /zmod help";
	}

	public String getPermission() {
		return Variables.PERMISSION_INFO;
	}
}
