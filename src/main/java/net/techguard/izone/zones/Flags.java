package net.techguard.izone.Zones;
import static net.techguard.izone.Utils.Localization.I18n.tl;

public enum Flags {
	PROTECTION,
	MONSTER,
	ANIMAL,
	WELCOME,
	FAREWELL,
	HEAL,
	HURT,
	PVP,
	CREEPER,
	TNT,
	EXPLOSION,
	FIRE,
	RESTRICTION,
	JAIL,
	LIGHTNING,
	DEATHDROP,
	SAFEDEATH,
	DROP,
	INTERACT,
	ENDERMAN,
	GOD,
	GAMEMODE,
	FLY,
	TAKEITEM_IN,
	TAKEITEM_OUT,
	GIVEITEM_IN,
	GIVEITEM_OUT,
	GIVEEFFECT_IN,
	GIVEEFFECT_OUT,
	TAKEEFFECT_IN,
	TAKEEFFECT_OUT,
	MELT,
	TELEPORT,
	WATER_FLOW,
	LAVA_FLOW;

	static
	{
		int ids = 0;

		for (Flags flag : values())
		{
			flag.id = (ids++);
		}
	}

	private int id;

	public String getName() {
		if (this == PROTECTION)
		{
			return tl("zone_flag_protection");
		}
		if (this == MONSTER)
		{
			return tl("zone_flag_monster");
		}
		if (this == ANIMAL)
		{
			return tl("zone_flag_animal");
		}
		if (this == WELCOME)
		{
			return tl("zone_flag_welcome");
		}
		if (this == FAREWELL)
		{
			return tl("zone_flag_farewell");
		}
		if (this == HEAL)
		{
			return tl("zone_flag_heal");
		}
		if (this == HURT)
		{
			return tl("zone_flag_hurt");
		}
		if (this == PVP)
		{
			return tl("zone_flag_pvp");
		}
		if (this == CREEPER)
		{
			return tl("zone_flag_creeper");
		}
		if (this == TNT)
		{
			return tl("zone_flag_tnt");
		}
		if (this == EXPLOSION)
		{
			return tl("zone_flag_explosion");
		}
		if (this == FIRE)
		{
			return tl("zone_flag_fire");
		}
		if (this == RESTRICTION)
		{
			return tl("zone_flag_restriction");
		}
		if (this == LIGHTNING)
		{
			return tl("zone_flag_lightning");
		}
		if (this == JAIL)
		{
			return tl("zone_flag_jail");
		}
		if (this == DEATHDROP)
		{
			return tl("zone_flag_deathdrop");
		}
		if (this == SAFEDEATH)
		{
			return tl("zone_flag_safedeath");
		}
		if (this == DROP)
		{
			return tl("zone_flag_drop");
		}
		if (this == INTERACT)
		{
			return tl("zone_flag_interact");
		}
		if (this == ENDERMAN)
		{
			return tl("zone_flag_enderman");
		}
		if (this == GOD)
		{
			return tl("zone_flag_god");
		}
		if (this == GAMEMODE)
		{
			return tl("zone_flag_gamemode");
		}
		if (this == FLY)
		{
			return tl("zone_flag_fly");
		}
		if (this == TAKEITEM_IN)
		{
			return tl("zone_flag_takeitem_in");
		}
		if (this == TAKEITEM_OUT)
		{
			return tl("zone_flag_takeitem_out");
		}
		if (this == GIVEITEM_IN)
		{
			return tl("zone_flag_giveitem_in");
		}
		if (this == GIVEITEM_OUT)
		{
			return tl("zone_flag_giveitem_out");
		}
		if (this == GIVEEFFECT_IN)
		{
			return tl("zone_flag_giveeffect_in");
		}
		if (this == GIVEEFFECT_OUT)
		{
			return tl("zone_flag_giveeffect_out");
		}
		if (this == TAKEEFFECT_IN)
		{
			return tl("zone_flag_takeeffect_in");
		}
		if (this == TAKEEFFECT_OUT)
		{
			return tl("zone_flag_takeeffect_out");
		}
		if (this == MELT)
		{
			return tl("zone_flag_melt");
		}
		if (this == TELEPORT)
		{
			return tl("zone_flag_teleport");
		}
		if (this == WATER_FLOW)
		{
			return tl("zone_flag_water_flow");
		}
		if (this == LAVA_FLOW)
		{
			return tl("zone_flag_lava_flow");
		}
		return tl("zone_flag_unknown");
	}

	public String toString() {
		return super.toString().toLowerCase();
	}

	public Integer getId() {
		return this.id;
	}
}