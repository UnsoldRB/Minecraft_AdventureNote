package com.unsoldriceball.adventurenote;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.unsoldriceball.adventurenote.ANMain.ID_MOD;


@Config(modid = ID_MOD)
public class ANConfig
{
    public static Systems c_Systems = new Systems();
    public static Ignores c_Ignores = new Ignores();
    public static Texts c_Texts = new Texts();




    public static class Systems
    {
        @Config.RequiresMcRestart
        @Config.Comment("If set to false, this mod will not give the notes when a player joins for the first time.")
        public boolean giveNotes_onFirstJoin = true;
        @Config.RangeDouble(max = 1.0d, min = 0.0d)
        @Config.Comment("If set to 0.0, this mod will not play sound when you update the Adventure Note.")
        public float soundVolume_WhenUpdateNote = 0.5f;
        @Config.RangeDouble(max = 1.0d, min = 0.0d)
        @Config.Comment("If set to 0.0, this mod will not play sound when you unlock something.")
        public float soundVolume_WhenUnlock = 0.5f;
        @Config.Comment("If set to true, the element names in the note will be replaced with string_hideLockedName.")
        public boolean hideLockedName = false;
        @Config.Comment("If set to true, when you hover over the name of a mod in the note, this mod will show the text.")
        public boolean showHoverText_pageTo = true;
        @Config.Comment("The maximum length of text. If the length of the text leached this value, the text that is after of leached maximum length will be replaced with string_lengthCap_omit.")
        public int elementName_lengthCap_english = 18;
        @Config.Comment("The maximum length of text. If the length of the text leached this value, the text that is after of leached maximum length will be replaced with string_lengthCap_omit.")
        public int modName_lengthCap_english = 18;
        @Config.Comment("The maximum length of text. If the length of the text leached this value, the text that is after of leached maximum length will be replaced with string_lengthCap_omit.")
        public int elementName_lengthCap_other = 12;
        @Config.Comment("The maximum length of text. If the length of the text leached this value, the text that is after of leached maximum length will be replaced with string_lengthCap_omit.")
        public int modName_lengthCap_other = 12;
        @Config.Comment("If set to true, progress will be expressed with percentage.")
        public boolean progress_usePercentage = false;
        @Config.Comment("If set to false, this mod will not record player data which is about mobs.")
        public boolean enableNoteSystem_mob = true;
        @Config.Comment("If set to false, this mod will not record player data which is about bosses.")
        public boolean enableNoteSystem_boss = true;
        @Config.Comment("If set to false, this mod will not record player data which is about biomes.")
        public boolean enableNoteSystem_biome = true;
        @Config.Comment("If set to false, this mod will not record player data which is about dimensions.")
        public boolean enableNoteSystem_dimension = true;
        @Config.Comment("If set to false, this mod will not register the recipe of the note about mobs.")
        public boolean enableRecipe_mob = true;
        @Config.Comment("If set to false, this mod will not register the recipe of the note about bosses.")
        public boolean enableRecipe_boss = true;
        @Config.Comment("If set to false, this mod will not register the recipe of the note about biomes.")
        public boolean enableRecipe_biome = true;
        @Config.Comment("If set to false, this mod will not register the recipe of the note about dimensions.")
        public boolean enableRecipe_dimension = true;
    }



    public static class Ignores
    {
        @Config.Comment("If set to true, this mod will ignore any elements that include the text of debug in the canonical class name.")
        public boolean ignore_debug_element = true;
        @Config.Comment("If set to true, this mod will ignore any elements that include the text of test in the canonical class name.(Recommend to false. Because also ignores for example greatest....)")
        public boolean ignore_test_element = false;
        @Config.Comment("If set to true, this mod will ignore any elements that include the text of temp in the canonical class name.")
        public boolean ignore_temp_element = true;
        @Config.RequiresMcRestart
        @Config.Comment("If listed name here, this mod will ignore that.")
        public String[] ignoreEntityList =
                {
                        "twilightforest:boggard",
                        "twilightforest:apocalypse_cube",
                        "twilightforest:adherent",
                        "twilightforest:castle_guardian"

                };
        @Config.RequiresMcRestart
        @Config.Comment("If listed name here, this mod will ignore that.")
        public String[] ignoreBiomeList =
                {

                };
        @Config.RequiresMcRestart
        @Config.Comment("If listed name here, this mod will ignore that.")
        public Integer[] ignoreDimensionIDList =
                {

                };
    }



    public static class Texts
    {
        @Config.Comment("Used to lengthCap config. (If set to empty, the text will be given an additional line instead replace text.)")
        public String string_lengthCap_omit = "";
        @Config.Comment("If you set hideLockedName to true, the names of elements will be replaced with this text.")
        public String string_hideLockedName = "?";
        public String prefixInNote_element_locked = "";
        public String prefixInNote_element_unlocked = "\u00A77\u00A7m";
        public String prefixInNote_modName = "\u00A73\u00A7n";
        public String prefixInNote_modPageIndex = "\u00A7r\u00A77";
        @Config.Comment("Used in contents table and first page in the note.")
        public String prefixInNote_modProgress_completed = "\u00A7r\u00A76";
        @Config.Comment("Used in contents table and first page in the note.")
        public String prefixInNote_modProgress_notCompleted = "\u00A7r\u00A77";
        @Config.Comment("The name of the note that about normal mobs.(Can use %player%.)")
        public String noteName_Mob = "AdventureNote - Mobs";
        @Config.Comment("The name of the note that about bosses.(Can use %player%.)")
        public String noteName_Boss = "AdventureNote - Bosses";
        @Config.Comment("The name of the note that about biomes.(Can use %player%.)")
        public String noteName_Biome = "AdventureNote - Biomes";
        @Config.Comment("The name of the note that about dimensions.(Can use %player%.)")
        public String noteName_Dimension = "AdventureNote - Dimensions";
        public String noteFirstPageTitle_Mob = "\u00A76\u00A7o\u00A7n\u00A7lMobs";
        public String noteFirstPageTitle_Boss = "\u00A76\u00A7o\u00A7n\u00A7lBosses";
        public String noteFirstPageTitle_Biome = "\u00A76\u00A7o\u00A7n\u00A7lBiomes";
        public String noteFirstPageTitle_Dimension = "\u00A76\u00A7o\u00A7n\u00A7lDimensions";
        @Config.Comment("The name of the author of the note that was added by this mod.")
        public String name_author = "AdventureNote mod";
        @Config.Comment("The message displayed when updated your note.")
        public String msg_UpdateNote = "Note updated.";
        @Config.Comment("The message displayed when unlocking a new mob.(Can use %name%.)")
        public String msg_Unlock_Mob = "\u00A7aNew mob killed!\u00A77(%name%)";
        @Config.Comment("The message displayed when unlocking a new boss.(Can use %name%.)")
        public String msg_Unlock_Boss = "\u00A7aNew boss killed!\u00A77(%name%)";
        @Config.Comment("The message displayed when unlocking a new biome.(Can use %name%.)")
        public String msg_Unlock_Biome = "\u00A7aNew biome discovered!\u00A77(%name%)";
        @Config.Comment("The message displayed when unlocking a new dimension.(Can use %name%.)")
        public String msg_Unlock_Dimension = "\u00A7aNew dimension discovered!\u00A77(%name%)";
    }



    //ゲーム内からConfigを変更したときのイベント
    @Mod.EventBusSubscriber(modid = ID_MOD)
    private static class EventHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(ID_MOD))
            {
                ConfigManager.sync(ID_MOD, Config.Type.INSTANCE);
            }
        }
    }
}