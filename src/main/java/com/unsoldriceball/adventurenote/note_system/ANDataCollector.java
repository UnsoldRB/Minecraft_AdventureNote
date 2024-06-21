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
    public static final Map<EnumANNoteType, Map<String, String>> f_registered_datas = new HashMap<>();
    public static Map<String, EntityLivingBase> f_entities_instance = new HashMap<>();
    public static Map<String, Biome> f_biomes_instance = new HashMap<>();
    private static List<String> f_ignore_entities = new ArrayList<>();
    private static List<String> f_ignore_biomes = new ArrayList<>();
    private static List<Integer> f_ignore_dimensions = new ArrayList<>();




    //コンストラクタ
    public ANDataCollector()
    {
        f_ignore_entities = Arrays.asList(ANConfig.c_Ignores.ignoreEntityList);
        f_ignore_biomes = Arrays.asList(ANConfig.c_Ignores.ignoreBiomeList);
        f_ignore_dimensions = Arrays.asList(ANConfig.c_Ignores.ignoreDimensionIDList);
    }



    //ワールドのロードが完了したときのイベント。
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        if (!event.getWorld().isRemote)
        {
            f_registered_datas.put(EnumANNoteType.MOBS, new TreeMap<>());
            f_registered_datas.put(EnumANNoteType.BOSSES, new TreeMap<>());
            f_registered_datas.put(EnumANNoteType.BIOMES, new TreeMap<>());
            f_registered_datas.put(EnumANNoteType.DIMENSIONS, new TreeMap<>());
            f_biomes_instance.clear();
            f_entities_instance.clear();

            getEntityData(event.getWorld());
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
                final EntityLivingBase __ELB = (EntityLivingBase) __E;
                final String __ID_ELB = Objects.requireNonNull(__ee.getRegistryName()).toString();
                final String __CLASSNAME_ELB = __ELB.getClass().getCanonicalName();

                //langファイルで名前が登録されていないentityと、configで設定されているentityは対象外とする。
                if (ANUtils.hasName(__ELB) && !f_ignore_entities.contains(__ID_ELB) && !isIgnoreElement(__CLASSNAME_ELB))
                {
                    if (__ELB.isNonBoss())
                    {

                        f_registered_datas.get(EnumANNoteType.MOBS).put(__CLASSNAME_ELB, __ID_ELB);
                    }
                    else
                    {
                        f_registered_datas.get(EnumANNoteType.BOSSES).put(__CLASSNAME_ELB, __ID_ELB);
                    }
                    f_entities_instance.put(__ID_ELB, __ELB);
                }
            }
        }
    }



    private void getBiomeData()
    {
        for(Biome __b : ForgeRegistries.BIOMES.getValuesCollection())
        {
            final String __ID_BIOME = Objects.requireNonNull(__b.getRegistryName()).toString();
            final String __CLASSNAME_BIOME = __b.getClass().getCanonicalName();

            //configで対象外に設定されているなら追加しない。
            if (!f_ignore_biomes.contains(__ID_BIOME) && !isIgnoreElement(__CLASSNAME_BIOME))
            {
                f_registered_datas.get(EnumANNoteType.BIOMES).put(__CLASSNAME_BIOME, __ID_BIOME);
                f_biomes_instance.put(__ID_BIOME, __b);
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
            f_registered_datas.get(EnumANNoteType.DIMENSIONS).put(String.valueOf(__d), __NAME_DIMENSION);
        }
    }



    //configで設定されている場合に、対象を無視するべきかどうかを名前で判断するための関数。
    private boolean isIgnoreElement(String name)
    {
        if (ANConfig.c_Ignores.ignore_debug_element && name.toLowerCase().contains("debug"))
        {
            return true;
        }
        else if (ANConfig.c_Ignores.ignore_test_element && name.toLowerCase().contains("test"))
        {
            return true;
        }
        else if (ANConfig.c_Ignores.ignore_temp_element && name.toLowerCase().contains("temp"))
        {
            return true;
        }
        return false;
    }
}
