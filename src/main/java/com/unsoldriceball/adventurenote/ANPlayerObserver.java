package com.unsoldriceball.adventurenote;

import com.unsoldriceball.adventurenote.note_system.ANDataCollector;
import com.unsoldriceball.adventurenote.note_system.ANNoteUpdater;
import com.unsoldriceball.adventurenote.note_system.EnumANNoteType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.unsoldriceball.adventurenote.ANMain.UPDATER_PER_PLAYER;

public class ANPlayerObserver
{
    private final Map<UUID, ArrayList<UUID>> ATTACKED_PLAYERS = new HashMap<>();
    private final Map<UUID, Biome> BIOME_PLAYER_ON = new HashMap<>();




    //プレイヤーがアイテムを右クリックしたら無条件で、
    //そのプレイヤーに対応しているANNoteUpdater.onPlayerClickNote()を発動させる。
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event)
    {
        if (!event.getWorld().isRemote)
        {
            final ANNoteUpdater _CLASS_NOTEUPDATER = UPDATER_PER_PLAYER.get(event.getEntityPlayer().getUniqueID());
            if (_CLASS_NOTEUPDATER != null)
            {
                _CLASS_NOTEUPDATER.onPlayerClickNote(event);
            }
        }
    }



    //エンティティがプレイヤーからダメージを受けた場合、
    //EntityのUUIDをキーとして、プレイヤーのUUIDをATTACKED_PLAYERSに格納する。
    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event)
    {
        final boolean _IS_ENABLE = ANConfig.c_Systems.enableNoteSystem_mob || ANConfig.c_Systems.enableNoteSystem_boss;
        final EntityLivingBase _VICTIM = event.getEntityLiving();

        if (_IS_ENABLE && _VICTIM != null && !_VICTIM.world.isRemote)
        {
            final Entity _ATTACKER = event.getSource().getTrueSource();

            if (_ATTACKER instanceof EntityPlayer && !(_ATTACKER instanceof FakePlayer))
            {
                put_AttackedPlayersMap(_VICTIM.getUniqueID(), _ATTACKER.getUniqueID());
            }
        }
    }



    //ATTACKED_PLAYERSに値をputする関数。
    //valueのArrayListに重複した値が入らないようにする。
    private void put_AttackedPlayersMap(UUID victim, UUID attacker)
    {
        ArrayList<UUID> _attackers_raw = ATTACKED_PLAYERS.get(victim);
        ArrayList<UUID> _attackers = _attackers_raw;

        if (_attackers == null)
        {
            _attackers = new ArrayList<>();
        }

        if (!_attackers.contains(attacker))
        {
            _attackers.add(attacker);
        }

        if (_attackers_raw == null || !_attackers_raw.equals(_attackers))
        {
            ATTACKED_PLAYERS.put(victim, _attackers);
        }
    }



    //ATTACKED_PLAYERSにUUIDがあるentityのHPが満タンになっている場合、
    //ATTACKED_PLAYERSからUUIDを削除する。
    @SubscribeEvent
    public void livingUpdate(LivingEvent.LivingUpdateEvent event)
    {
        final boolean _IS_ENABLE = ANConfig.c_Systems.enableNoteSystem_mob || ANConfig.c_Systems.enableNoteSystem_boss;
        final EntityLivingBase _ENTITY = event.getEntityLiving();

        if (_IS_ENABLE && _ENTITY != null && !_ENTITY.world.isRemote && !(_ENTITY instanceof EntityPlayer && !(_ENTITY instanceof FakePlayer)))
        {
            final UUID _UUID_ENTITY = _ENTITY.getUniqueID();

            if (ATTACKED_PLAYERS.containsKey(_UUID_ENTITY) && _ENTITY.getHealth() == _ENTITY.getMaxHealth())
            {
                ATTACKED_PLAYERS.remove(_UUID_ENTITY);
            }
        }
    }



    //ATTACKED_PLAYERSにUUIDがあるLivingEntityが死亡したときに、無条件でANNoteUpdater.onEntityDeath()を実行する。
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        final EntityLivingBase _VICTIM = event.getEntityLiving();
        final World _WORLD = _VICTIM.world;

        if (!_WORLD.isRemote)
        {
            final UUID _UUID_VICTIM = _VICTIM.getUniqueID();

            if (ATTACKED_PLAYERS.containsKey(_UUID_VICTIM))
            {
                for(UUID __uuid_player : ATTACKED_PLAYERS.get(_UUID_VICTIM))
                {
                    final EntityPlayer __PLAYER = _WORLD.getPlayerEntityByUUID(__uuid_player);
                    //対象がオンラインでないならスキップ。
                    if (__PLAYER == null) continue;
                    UPDATER_PER_PLAYER.get(__uuid_player).onEntityDeath(_VICTIM, __PLAYER);
                }

                ATTACKED_PLAYERS.remove(_UUID_VICTIM);
            }
        }
    }



    //プレイヤーがいるバイオームを監視して、バイオームが変更されたときに無条件でANNoteUpdater.を実行する。
    @SubscribeEvent
    public void onBiomeChanged(LivingEvent.LivingUpdateEvent event)
    {
        final EntityLivingBase _ENTITY = event.getEntityLiving();

        if (ANConfig.c_Systems.enableNoteSystem_biome && _ENTITY != null && !_ENTITY.world.isRemote && (_ENTITY instanceof EntityPlayer && !(_ENTITY instanceof FakePlayer)))
        {
            final EntityPlayer _PLAYER = (EntityPlayer) _ENTITY;
            final UUID _UUID_PLAYER = _PLAYER.getUniqueID();
            final Biome _BIOME = _PLAYER.world.getBiome(_PLAYER.getPosition());
            final Biome _BIOME_OLD = BIOME_PLAYER_ON.get(_UUID_PLAYER);

            if (_BIOME_OLD == null || _BIOME_OLD != _BIOME)
            {
                UPDATER_PER_PLAYER.get(_UUID_PLAYER).onBiomeChanged(_BIOME, _PLAYER);
                BIOME_PLAYER_ON.put(_UUID_PLAYER, _BIOME);
            }
        }

    }



    //ディメンションが変更されたときに無条件でANNoteUpdater.を実行する。
    @SubscribeEvent
    public void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (ANConfig.c_Systems.enableNoteSystem_dimension && !event.player.world.isRemote)
        {
            UPDATER_PER_PLAYER.get(event.player.getUniqueID()).onDimensionChanged(event.toDim, event.player);
        }
    }



    //ログイン時にはディメンションを検知する関数がないので、手動で検知させる。(初回ログイン時などで必要)
    @SubscribeEvent
    public void onDimensionChanged_ByLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (ANConfig.c_Systems.enableNoteSystem_dimension)
        {
            final EntityPlayer _P = event.player;

            if (!_P.world.isRemote)
            {
                UPDATER_PER_PLAYER.get(_P.getUniqueID()).onDimensionChanged(_P.dimension, _P);
            }
        }
    }
}
