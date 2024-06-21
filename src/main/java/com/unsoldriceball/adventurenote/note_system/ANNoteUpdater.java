package com.unsoldriceball.adventurenote.note_system;

import com.unsoldriceball.adventurenote.ANConfig;
import com.unsoldriceball.adventurenote.ANUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;




public class ANNoteUpdater
{
    private final UUID UUID_PLAYER;
    //本の内容と、このmodで使用するプレイヤーのデータを更新するべきかどうかを判断するために、その都度JSONを読み取るのはなんか違う気がするので、
    //以下2つの変数に、プレイヤーのデータと、そのサイズを保存しておく。
    //これによって、JSONを読み取るのは、本の作成・更新時のみになる。
    private final Map<EnumANNoteType, ArrayList<String>> note_datas = new HashMap<>();
    public final Map<EnumANNoteType, Integer> note_progress = new HashMap<>();




    public ANNoteUpdater(UUID uuid)
    {
        UUID_PLAYER = uuid;

        //以下、プレイヤーデータが保存されているJsonファイルからデータを読み取る処理。
        final ArrayList<String> _DATA_NORMAL_ENTITY = ANJsonEditor.getValue_Json(UUID_PLAYER, EnumANNoteType.MOBS.name());
        final ArrayList<String> _DATA_BOSS_ENTITY = ANJsonEditor.getValue_Json(UUID_PLAYER, EnumANNoteType.BOSSES.name());
        final ArrayList<String> _DATA_BIOME = ANJsonEditor.getValue_Json(UUID_PLAYER, EnumANNoteType.BIOMES.name());
        final ArrayList<String> _DATA_DIMENSION = ANJsonEditor.getValue_Json(UUID_PLAYER, EnumANNoteType.DIMENSIONS.name());
        note_progress.put(EnumANNoteType.MOBS, _DATA_NORMAL_ENTITY.size());
        note_progress.put(EnumANNoteType.BOSSES, _DATA_BOSS_ENTITY.size());
        note_progress.put(EnumANNoteType.BIOMES, _DATA_BIOME.size());
        note_progress.put(EnumANNoteType.DIMENSIONS, _DATA_DIMENSION.size());

        note_datas.put(
                EnumANNoteType.MOBS,
                ANJsonEditor.convertIDtoClassName(
                        _DATA_NORMAL_ENTITY,
                        EnumANNoteType.MOBS
                )
        );
        note_datas.put(
                EnumANNoteType.BOSSES,
                ANJsonEditor.convertIDtoClassName(
                        _DATA_BOSS_ENTITY,
                        EnumANNoteType.BOSSES
                )
        );
        note_datas.put(EnumANNoteType.BIOMES,
                ANJsonEditor.convertIDtoClassName(
                        _DATA_BIOME,
                        EnumANNoteType.BIOMES
                )
        );
        note_datas.put(EnumANNoteType.DIMENSIONS, _DATA_DIMENSION);
    }



    //プレイヤーがノートを右クリックした際、
    //プレイヤーが手に持っているノートが古いものであれば更新する関数。
    //ANPlayerObserverから呼び出される。
    public void onPlayerClickNote(PlayerInteractEvent.RightClickItem event)
    {
        final EntityPlayer _P = event.getEntityPlayer();
        final ItemStack _HELDITEM = event.getItemStack();
        final EnumANNoteType _TYPE_NOTE = ANUtils.getNoteType(_HELDITEM);

        //_TYPE_NOTEがnullの場合は、そもそも本ではない場合と、このmodで追加された本でない場合がある。
        if (_TYPE_NOTE != null)
        {
            final UUID _OWNER = ANUtils.getUUID_fromAuthor(_HELDITEM);
            if (_OWNER == null)
            {
                //新品のAdventureNoteだった場合にこちらの処理が実行される(クラフトしたばかりの場合など。)。
                event.setCanceled(true);
                _P.setHeldItem(EnumHand.MAIN_HAND, ANNoteBuilder.createNote(_TYPE_NOTE, _P));
                onPlayerClickNote_stageEffect(_P);
            }
            else if(_P.getUniqueID().equals(_OWNER))
            {
                final NBTTagCompound _NBT_HELDITEM = _HELDITEM.getTagCompound();

                if (_NBT_HELDITEM != null && ANUtils.isNotEqualNBT_progress(_HELDITEM, note_progress.get(_TYPE_NOTE)))
                {
                    event.setCanceled(true);
                    _P.setHeldItem(EnumHand.MAIN_HAND, ANNoteBuilder.createNote(_TYPE_NOTE, _P));
                    onPlayerClickNote_stageEffect(_P);
                }
            }
        }
    }



    //onPlayerClickNote()で使用する演出効果。
    private static void onPlayerClickNote_stageEffect(EntityPlayer p)
    {
        if (ANConfig.c_Systems.soundVolume_WhenUpdateNote != 0.0f)
        {
            p.world.playSound(null, p.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, ANConfig.c_Systems.soundVolume_WhenUpdateNote, 2.0f);
        }
        p.sendStatusMessage(new TextComponentString(ANConfig.c_Texts.msg_UpdateNote), true);
    }



    //プレイヤーが新規dimension, biome, entityを達成したときに呼び出される。
    private void onUpdateData(EnumANNoteType type, String new_data, String new_data_forJson, String displayname, EntityPlayer p)
    {
        note_datas.get(type).add(new_data);
        note_progress.put(type, note_progress.get(type) + 1);
        //プレイヤーがデータのJsonファイルを見たときにわかりやすいように、Jsonには別の形式のdataを追加する。
        ANJsonEditor.updateJsonData(UUID_PLAYER, type.name(), new_data_forJson);

        if (ANConfig.c_Systems.soundVolume_WhenUnlock != 0.0f)
        {
            p.world.playSound(null, p.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, ANConfig.c_Systems.soundVolume_WhenUnlock, 2.0f);
        }
        String _f_message = "";
        switch (type)
        {
            case MOBS:
                _f_message = ANConfig.c_Texts.msg_Unlock_Mob;
                break;
            case BOSSES:
                _f_message = ANConfig.c_Texts.msg_Unlock_Boss;
                break;
            case BIOMES:
                _f_message = ANConfig.c_Texts.msg_Unlock_Biome;
                break;
            case DIMENSIONS:
                _f_message = ANConfig.c_Texts.msg_Unlock_Dimension;
        }
        p.sendMessage(new TextComponentString(_f_message.replaceAll("%name%", displayname)));
    }



    //ANPlayerObserverから呼び出される。onUpdateData()のための関数。
    public void onEntityDeath(EntityLivingBase entity, EntityPlayer p)
    {
        final String _NAME_CLASS = entity.getClass().getCanonicalName();

        if (ANDataCollector.f_registered_datas.get(EnumANNoteType.MOBS).containsKey(_NAME_CLASS))
        {
            if (!note_datas.get(EnumANNoteType.MOBS).contains(_NAME_CLASS))
            {
                final String _ID_ENTITY = ANDataCollector.f_registered_datas.get(EnumANNoteType.MOBS).get(_NAME_CLASS);
                onUpdateData(EnumANNoteType.MOBS, _NAME_CLASS, _ID_ENTITY, entity.getName(), p);
            }
        }
        else if (ANDataCollector.f_registered_datas.get(EnumANNoteType.BOSSES).containsKey(_NAME_CLASS))
        {
            if (!note_datas.get(EnumANNoteType.BOSSES).contains(_NAME_CLASS))
            {
                final String _ID_ENTITY = ANDataCollector.f_registered_datas.get(EnumANNoteType.BOSSES).get(_NAME_CLASS);
                onUpdateData(EnumANNoteType.BOSSES, _NAME_CLASS, _ID_ENTITY, entity.getName(), p);
            }
        }
    }



    //ANPlayerObserverから呼び出される。onUpdateData()のための関数。
    public void onBiomeChanged(Biome biome, EntityPlayer p)
    {
        if (ANDataCollector.f_registered_datas.get(EnumANNoteType.BIOMES).containsKey(biome.getClass().getCanonicalName()))
        {
            final String _NAME_CLASS = biome.getClass().getCanonicalName();

            if (!note_datas.get(EnumANNoteType.BIOMES).contains(_NAME_CLASS))
            {
                final String _ID_BIOME = ANDataCollector.f_registered_datas.get(EnumANNoteType.BIOMES).get(_NAME_CLASS);
                onUpdateData(EnumANNoteType.BIOMES, _NAME_CLASS, _ID_BIOME, biome.getBiomeName(), p);
            }
        }
    }



    //ANPlayerObserverから呼び出される。onUpdateData()のための関数。
    public void onDimensionChanged(Integer dim, EntityPlayer p)
    {
        final Map<String, String> _DATA_DIM = ANDataCollector.f_registered_datas.get(EnumANNoteType.DIMENSIONS);
        final String _ID_DIM = String.valueOf(dim);

        if (_DATA_DIM.containsKey(String.valueOf(_ID_DIM)))
        {
            if (!note_datas.get(EnumANNoteType.DIMENSIONS).contains(_ID_DIM))
            {
                final String _NAME_DIMENSION = _DATA_DIM.get(_ID_DIM);
                onUpdateData(EnumANNoteType.DIMENSIONS, _ID_DIM, _ID_DIM, _NAME_DIMENSION, p);
            }
        }
    }
}
