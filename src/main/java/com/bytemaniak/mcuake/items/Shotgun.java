package com.bytemaniak.mcuake.items;

import com.bytemaniak.mcuake.entity.QuakePlayer;
import com.bytemaniak.mcuake.entity.projectile.Shell;
import com.bytemaniak.mcuake.registry.Sounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

import java.util.concurrent.ThreadLocalRandom;

public class Shotgun extends Weapon {
    private static final long SHOTGUN_REFIRE_TICK_RATE = 20;

    private static final float SHOTGUN_RANGE = 20;
    private static final float SHOTGUN_VERTICAL_SPAWN_OFFSET = .5f;
    private static final float SHOTGUN_HORIZONTAL_SPAWN_OFFSET = -.2f;
    private static final float SHOTGUN_FORWARD_SPAWN_OFFSET = .5f;
    private static final float SHOTGUN_PROJECTILE_SPEED = 4.25f;

    public Shotgun() {
        super(QuakePlayer.WeaponSlot.SHOTGUN, new Identifier("mcuake", "shotgun"),
                SHOTGUN_REFIRE_TICK_RATE, true, Sounds.SHOTGUN_FIRE, false);
    }

    private void fireProjectile(World world, LivingEntity user, Vec3d upVec, Vec3d rightVec,
                                Vec3d destDir, Vec3d weaponPos, int maxPitchSpread, int maxYawSpread) {
        // Spawn a new shell approximately from the weapon
        Vec3d spread = destDir;

        if (maxYawSpread > 0) {
            int yawSpread = ThreadLocalRandom.current().nextInt(-maxYawSpread, maxYawSpread);
            spread = spread.add(rightVec.multiply(yawSpread/(float)maxYawSpread/4.f));
        }
        if (maxPitchSpread > 0) {
            int pitchSpread = ThreadLocalRandom.current().nextInt(-maxPitchSpread, maxPitchSpread);
            spread = spread.add(upVec.multiply(pitchSpread/(float)maxPitchSpread/6.f));
        }

        Shell shell = new Shell(world);
        shell.setOwner(user);
        shell.setPosition(weaponPos);
        shell.setVelocity(spread.x, spread.y, spread.z, SHOTGUN_PROJECTILE_SPEED, 0);
        world.spawnEntity(shell);
    }

    @Override
    protected void onWeaponRefire(World world, LivingEntity user, ItemStack stack, Vec3d lookDir, Vec3d weaponPos) {
        // Spawn a new shell approximately from the weapon
        Vec3d upVec = Vec3d.fromPolar(user.getPitch() + 90, user.getYaw()).normalize();
        Vec3d rightVec = lookDir.crossProduct(upVec).normalize();
        Vec3d offsetWeaponPos = weaponPos
                .add(upVec.multiply(SHOTGUN_VERTICAL_SPAWN_OFFSET))
                .add(rightVec.multiply(SHOTGUN_HORIZONTAL_SPAWN_OFFSET))
                .add(lookDir.multiply(SHOTGUN_FORWARD_SPAWN_OFFSET));

        // The furthest point, to which the projectile will go towards
        Vec3d destPos = user.getEyePos().add(lookDir.multiply(SHOTGUN_RANGE));
        Vec3d destDir = destPos.subtract(offsetWeaponPos).normalize();

        fireProjectile(world, user, upVec, rightVec, destDir, offsetWeaponPos, 0, 0);
        for (int i = 0; i < 3; i++) fireProjectile(world, user, upVec, rightVec, destDir, offsetWeaponPos, 7, 7);
        for (int i = 0; i < 6; i++) fireProjectile(world, user, upVec, rightVec, destDir, offsetWeaponPos, 12, 15);

        triggerAnim(user, GeoItem.getOrAssignId(user.getActiveItem(), (ServerWorld) world), "controller", "shoot");
    }

    @Override
    protected PlayState handle(AnimationState<Weapon> state) {
        return null;
    }
}
