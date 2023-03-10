package com.bytemaniak.mcuake.items;

import com.bytemaniak.mcuake.entity.QuakePlayer;
import com.bytemaniak.mcuake.registry.DamageSources;
import com.bytemaniak.mcuake.registry.Packets;
import com.bytemaniak.mcuake.registry.Sounds;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

public class LightningGun extends HitscanWeapon {
    private static final long LIGHTNING_REFIRE_RATE = 1;
    private static final int LIGHTNING_QUAKE_DAMAGE = 8;
    private static final int LIGHTNING_MC_DAMAGE = 2;
    private static final float LIGHTNING_RANGE = 30;

    public LightningGun() {
        super(QuakePlayer.WeaponSlot.LIGHTNING_GUN, new Identifier("mcuake", "lightning_gun"),
                LIGHTNING_REFIRE_RATE, false, null, true,
                LIGHTNING_QUAKE_DAMAGE, LIGHTNING_MC_DAMAGE, DamageSources.LIGHTNING_DAMAGE, LIGHTNING_RANGE);
    }

    @Override
    protected void onProjectileCollision(World world, Vec3d userPos, Vec3d iterPos) {
        sendLightningGunTrail(world, userPos, iterPos);
    }

    private void sendLightningGunTrail(World world, Vec3d startPos, Vec3d endPos) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(startPos.x);
        buf.writeDouble(startPos.y);
        buf.writeDouble(startPos.z);
        buf.writeDouble(endPos.x);
        buf.writeDouble(endPos.y);
        buf.writeDouble(endPos.z);
        buf.writeInt(QuakePlayer.WeaponSlot.LIGHTNING_GUN.slot());
        for (ServerPlayerEntity plr : PlayerLookup.world((ServerWorld) world))
            ServerPlayNetworking.send(plr, Packets.SHOW_TRAIL, buf);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        world.playSoundFromEntity(null, user, Sounds.LIGHTNING_FIRE, SoundCategory.PLAYERS, 1, 1);

        return super.use(world, user, hand);
    }

    @Override
    protected PlayState handle(AnimationState<Weapon> state) {
        return null;
    }
}
