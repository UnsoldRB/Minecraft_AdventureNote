package com.unsoldriceball.adventurenote.note_system;

import com.unsoldriceball.adventurenote.ANConfig;
import com.unsoldriceball.adventurenote.ANUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;


import java.util.*;


public class ANDataCollector
{
    public static final Map<EnumANNoteType, Map<Integer, String>> f_registered_datas = new HashMap<>();
    public static Map<Integer, String> f_entities_name = new HashMap<>();
    public static Map<Integer, Biome> f_biomes_instance = new HashMap<>();
    public static List<String> f_ignore_entities = new ArrayList<>();
    public static List<String> f_ignore_biomes = new ArrayList<>();
    public static List<Integer> f_ignore_dimensions = new ArrayList<>();




    //コンストラクタ
    public ANDataCollector()
    {
        f_ignore_entities = Arrays.asList(ANConfig.c_Ignores.ignoreEntityList);
        f_ignore_biomes = Arrays.asList(ANConfig.c_Ignores.ignoreBiomeList);
        f_ignore_dimensions = Arrays.asList(ANConfig.c_Ignores.ignoreDimensionIDList);
    }



    //ワールドを読み込むときのイベント。データを読み込むにはこれが一番都合がよかった、
    @SubscribeEvent
    public void onLoadWorld(WorldEvent.Load event)
    {
        final World _WORLD = event.getWorld();

        //イベントがサーバー側、かつプレイヤーが一人もいない場合にのみ処理を行う。(そうしないとdimensionを移動したりした場合にも処理が実行される。)
        if (!_WORLD.isRemote && Objects.requireNonNull(_WORLD.getMinecraftServer()).getPlayerList().getPlayers().isEmpty())
        {
            f_registered_datas.clear();
            f_registered_datas.put(EnumANNoteType.MOBS, new HashMap<>());
            f_registered_datas.put(EnumANNoteType.BOSSES, new HashMap<>());
            f_registered_datas.put(EnumANNoteType.BIOMES, new HashMap<>());
            f_registered_datas.put(EnumANNoteType.DIMENSIONS, new HashMap<>());
            f_biomes_instance.clear();
            f_entities_name.clear();

            getEntityData(_WORLD);
            getBiomeData();
            getDimensionData();
        }
    }



    private void getEntityData(World world)
    {
        //登録済みの全Entityを取得する。
        final Collection<EntityEntry> _REGISTERED_ENTITIES = ForgeRegistries.ENTITIES.getValuesCollection();

        for (EntityEntry __ee : _REGISTERED_ENTITIES)
        {
            if (EntityLivingBase.class.isAssignableFrom(__ee.getEntityClass()))
            {
                final Entity __E = __ee.newInstance(world);
                try
                {
                    EntityLivingBase __elb = (EntityLivingBase) __E;
                    final String __ID_ELB = Objects.requireNonNull(__ee.getRegistryName()).toString();
                    final Integer __HASH_ELB = ANUtils.getHashCodeFromELBClass(__elb);
                    final String __NAME_ELB = __elb.getName();
                    final boolean __HASNAME_ELB = ANUtils.hasName(__elb);
                    final boolean __IS_IGNORE_ELEMENT = isIgnoreElement(__elb);
                    final boolean __IS_NON_BOSS_ELB = __elb.isNonBoss();

                    __elb = null;
                    world.removeEntityDangerously(__E);   //不具合が起きると嫌なので早めに__Eを消す。

                    //クラスのhashCodeを取得できなかったentityと、langファイルで名前が登録されていないentityと、configで設定されているentityは対象外とする。
                    if (__HASH_ELB != null && __HASNAME_ELB && !f_ignore_entities.contains(__ID_ELB) && !__IS_IGNORE_ELEMENT)
                    {
                        if (__IS_NON_BOSS_ELB)
                        {
                            f_registered_datas.get(EnumANNoteType.MOBS).put(__HASH_ELB, __ID_ELB);
                            debug_printIDs(__NAME_ELB, __ID_ELB, EnumANNoteType.MOBS);
                        }
                        else
                        {
                            f_registered_datas.get(EnumANNoteType.BOSSES).put(__HASH_ELB, __ID_ELB);
                            debug_printIDs(__NAME_ELB, __ID_ELB, EnumANNoteType.BOSSES);
                        }
                        f_entities_name.put(__HASH_ELB, __NAME_ELB);
                    }
                }
                catch (NullPointerException exc)
                {
                    if (__E != null)
                    {
                        world.removeEntityDangerously(__E);
                    }
                }
            }
        }
    }



    private void getBiomeData()
    {
        for(Biome __b : ForgeRegistries.BIOMES.getValuesCollection())
        {
            final String __ID_BIOME = Objects.requireNonNull(__b.getRegistryName()).toString();
            final int __HASH_BIOME = __b.getClass().hashCode();

            //configで対象外に設定されているなら追加しない。
            if (!f_ignore_biomes.contains(__ID_BIOME) && !isIgnoreElement(__b))
            {
                f_registered_datas.get(EnumANNoteType.BIOMES).put(__HASH_BIOME, __ID_BIOME);
                f_biomes_instance.put(__HASH_BIOME, __b);
                debug_printIDs("", __ID_BIOME, EnumANNoteType.BIOMES);
            }
        }
    }



    private void getDimensionData()
    {
        final Integer[] _DIMENSIONS = DimensionManager.getIDs();

        for (int __d : _DIMENSIONS)
        {
            //configで対象外に設定されているなら追加しない。
            if (f_ignore_dimensions.contains(__d)) continue;
            final String __NAME_DIMENSION = DimensionManager.getProviderType(__d).getName();

            //configで対象外に設定されているなら追加しない。
            if (isIgnoreElement(__NAME_DIMENSION)) continue;
            f_registered_datas.get(EnumANNoteType.DIMENSIONS).put(__d, __NAME_DIMENSION);
            debug_printIDs(__NAME_DIMENSION, String.valueOf(__d), EnumANNoteType.DIMENSIONS);
        }
    }



    //configで設定されている場合に、対象を無視するべきかどうかを名前で判断するための関数。
    public boolean isIgnoreElement(Object o)
    {
        final String _CLASS_NAME;

        try
        {
            _CLASS_NAME = o.getClass().getCanonicalName();
        }
        catch (Exception exc)
        {
            //ClassのCanonicalNameを取得できなかった場合はあきらめる。
            return false;
        }

        if (ANConfig.c_Ignores.ignore_debug_element && _CLASS_NAME.toLowerCase().contains("debug"))
        {
            return true;
        }
        else if (ANConfig.c_Ignores.ignore_test_element && _CLASS_NAME.toLowerCase().contains("test"))
        {
            return true;
        }
        else if (ANConfig.c_Ignores.ignore_temp_element && _CLASS_NAME.toLowerCase().contains("temp"))
        {
            return true;
        }
        return false;
    }



    //Configで有効な場合は各IDを.logファイルに出力する関数。
    private static void debug_printIDs(String name, String id, EnumANNoteType type)
    {
        if (ANConfig.c_Systems.debug_printAllIDs)
        {
            final StringBuilder _STR_BUILDER = new StringBuilder();
            _STR_BUILDER.append(type.name());
            _STR_BUILDER.append("-> (");
            _STR_BUILDER.append(name);
            _STR_BUILDER.append(" = ");
            _STR_BUILDER.append(id);
            _STR_BUILDER.append(")\n");
            System.out.println(_STR_BUILDER);
        }
    }
}
