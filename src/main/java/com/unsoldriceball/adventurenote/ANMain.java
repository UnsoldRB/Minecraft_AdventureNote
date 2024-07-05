package com.unsoldriceball.adventurenote;


import com.unsoldriceball.adventurenote.note_system.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;




@Mod(modid = ANMain.ID_MOD, acceptableRemoteVersions = "*")
public class ANMain
{
    public static final String ID_MOD = "adventurenote";
    public static Map<UUID, ANNoteUpdater> UPDATER_PER_PLAYER = new HashMap<>();





    //ModがInitializeを呼び出す前に発生するイベント。
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        //これでこのクラス内でForgeのイベントが動作するようになるらしい。
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ANDataCollector());
        MinecraftForge.EVENT_BUS.register(new ANPlayerObserver());

        //レシピの登録。
        if (ANConfig.c_Systems.enableRecipe_mob)
        {
            ANRecipes.recipe_NORMALENTITY();
        }
        if (ANConfig.c_Systems.enableRecipe_boss)
        {
            ANRecipes.recipe_BOSSENTITY();
        }
        if (ANConfig.c_Systems.enableRecipe_biome)
        {
            ANRecipes.recipe_BIOME();
        }
        if (ANConfig.c_Systems.enableRecipe_dimension)
        {
            ANRecipes.recipe_DIMENSION();
        }
    }



    //ワールドをロードした後から計算して、初回ログインのプレイヤーのUUIDをNOTE_PER_PLAYERに格納していく。
    //同時にANNoteUpdaterを作成してこのUUIDと関連付ける。
    @SubscribeEvent
    public void onPlayerLoggedin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!event.player.world.isRemote)
        {
            final EntityPlayer _P = event.player;
            final UUID _UUID_P = _P.getUniqueID();

            if (!UPDATER_PER_PLAYER.containsKey(_UUID_P))
            {
                UPDATER_PER_PLAYER.put(_UUID_P, new ANNoteUpdater(_UUID_P));

                //configで有効ならログイン時に本を渡す。
                if (ANConfig.c_Systems.giveNotes_onFirstJoin)
                {
                    giveAdvNote_onFirstLogin(_P);
                }
            }
        }
    }



    //初回ログイン時にノート全種類を与える関数。
    private static void giveAdvNote_onFirstLogin(EntityPlayer p)
    {
        final String _KEY = ANJsonEditor.KEY_FIRSTJOIN;
        final String _STRING_UUID = p.getUniqueID().toString();

        if (!ANJsonEditor.getValue_Json(null, _KEY).contains(_STRING_UUID))
        {
            if (ANConfig.c_Systems.enableNoteSystem_mob)
            {
                p.addItemStackToInventory(ANNoteBuilder.createBlankNotes(EnumANNoteType.MOBS));
            }
            if (ANConfig.c_Systems.enableNoteSystem_boss)
            {
                p.addItemStackToInventory(ANNoteBuilder.createBlankNotes(EnumANNoteType.BOSSES));
            }
            if (ANConfig.c_Systems.enableNoteSystem_biome)
            {
                p.addItemStackToInventory(ANNoteBuilder.createBlankNotes(EnumANNoteType.BIOMES));
            }
            if (ANConfig.c_Systems.enableNoteSystem_dimension)
            {
                p.addItemStackToInventory(ANNoteBuilder.createBlankNotes(EnumANNoteType.DIMENSIONS));
            }
            ANJsonEditor.updateJsonData(null, _KEY, _STRING_UUID);
        }
    }



    //プレイヤーがログアウトしたらANNoteUpdaterへの参照を消す。(後の仕事はガベージコレクションさんがやってくれる。)
    @SubscribeEvent
    public void onPlayerLoggedout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        final EntityPlayer _P = event.player;

        if (!_P.world.isRemote)
        {
            UPDATER_PER_PLAYER.remove(_P.getUniqueID());
        }
    }
}




