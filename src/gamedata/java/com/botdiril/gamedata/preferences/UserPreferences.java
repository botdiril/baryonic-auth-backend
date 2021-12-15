package com.botdiril.gamedata.preferences;

import com.botdiril.gamedata.player.properties.PlayerProperties;

public class UserPreferences
{
    public static final String PREFERENCE_BITFIELD = "preferences_bool_bitfield";

    public static boolean isBitEnabled(PlayerProperties po, EnumUserPreference preference)
    {
        long bit = preference.getBit();
        return (po.getPreferencesBitfield() & bit) != 0;
    }

    public static boolean toggleBit(PlayerProperties po, EnumUserPreference preference)
    {
        long bit = preference.getBit();
        var newVal = po.getPreferencesBitfield() ^ bit;

        po.setPreferencesBitfield(newVal);

        return (newVal & bit) != 0; // The new, toggled value
    }

    public static void setBit(PlayerProperties po, EnumUserPreference preference)
    {
        long bit = preference.getBit();

        po.setPreferencesBitfield(po.getPreferencesBitfield() | bit);
    }

    public static void clearBit(PlayerProperties po, EnumUserPreference preference)
    {
        long bit = preference.getBit();

        po.setPreferencesBitfield(po.getPreferencesBitfield() & ~bit);
    }
}
