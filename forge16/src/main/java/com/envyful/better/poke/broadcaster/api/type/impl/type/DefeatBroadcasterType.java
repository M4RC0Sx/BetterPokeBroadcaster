package com.envyful.better.poke.broadcaster.api.type.impl.type;

import com.envyful.api.forge.world.UtilWorld;
import com.envyful.better.poke.broadcaster.api.type.impl.AbstractBroadcasterType;
import com.envyful.better.poke.broadcaster.api.util.BroadcasterUtil;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.util.helpers.BiomeHelper;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DefeatBroadcasterType extends AbstractBroadcasterType<BattleEndEvent> {

    public DefeatBroadcasterType() {
        super("defeat", BattleEndEvent.class);
    }

    @Override
    protected boolean isEvent(BattleEndEvent event) {
        PixelmonEntity entity = this.getEntity(event);

        if (entity == null) {
            return false;
        }

        BattleResults result = this.getResult(event, EntityType.PLAYER);

        if (result == null) {
            return false;
        }

        return result == BattleResults.VICTORY;
    }

    public BattleResults getResult(BattleEndEvent event, EntityType<?> entityType) {
        for (Map.Entry<BattleParticipant, BattleResults> entry : event.getResults().entrySet()) {
            if (Objects.equals(entityType, entry.getKey().getEntity().getType())) {
                return entry.getValue();
            }
        }

        return null;
    }

    @Override
    protected PixelmonEntity getEntity(BattleEndEvent event) {
        for (BattleParticipant battleParticipant : event.getResults().keySet()) {
            if (battleParticipant instanceof WildPixelmonParticipant) {
                return (PixelmonEntity)((WildPixelmonParticipant) battleParticipant).getEntity();
            }
        }

        return null;
    }

    @Override
    protected String translateEventMessage(BattleEndEvent event, String line, PixelmonEntity pixelmon, ServerPlayerEntity nearestPlayer) {
        final int ivs = (int) Math.round(pixelmon.getPokemon().getIVs().getPercentage(2));

        return line.replace("%nearest_name%", nearestPlayer == null ? "None" : nearestPlayer.getName().getString())
                .replace("%x%", pixelmon.getX() + "")
                .replace("%y%", pixelmon.getY() + "")
                .replace("%z%", pixelmon.getZ() + "")
                .replace("%world%", UtilWorld.getName(pixelmon.level) + "")
                .replace("%pokemon%", pixelmon.getPokemonName())
                .replace("%species%", pixelmon.getSpecies().getLocalizedName())
                .replace("%species_lower%", pixelmon.getSpecies().getLocalizedName().toLowerCase(Locale.ROOT))
                .replace("%ivs%", Integer.toString(ivs))
                .replace("%biome%", BiomeHelper.getLocalizedBiomeName(pixelmon.level.getBiome(pixelmon.blockPosition())).getString());
    }

    @Override
    public ServerPlayerEntity findNearestPlayer(BattleEndEvent event, PixelmonEntity entity, double range) {
        return (ServerPlayerEntity) entity.level.getNearestPlayer(entity, range);
    }

    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event) {
        BroadcasterUtil.handleEvent(event);
    }
}
