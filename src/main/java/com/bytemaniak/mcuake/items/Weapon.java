package com.bytemaniak.mcuake.items;

import com.bytemaniak.mcuake.entity.QuakePlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Weapon extends Item implements GeoItem {
    private static final float HITSCAN_HORIZONTAL_OFFSET = .2f;

    private final Identifier weaponIdentifier;
    private final long refireRate;
    private final boolean hasRepeatedFiringSound;
    private final SoundEvent firingSound;
    public final boolean hasActiveLoopSound;

    public final QuakePlayer.WeaponSlot weaponSlot;

    protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    protected Weapon(QuakePlayer.WeaponSlot weaponSlot, Identifier id, long refireRateInTicks,
                     boolean hasRepeatedFiringSound, SoundEvent firingSound, boolean hasActiveLoopSound) {
        super(new Item.Settings().maxCount(1));
        this.weaponSlot = weaponSlot;
        this.weaponIdentifier = id;

        this.refireRate = refireRateInTicks;
        this.hasRepeatedFiringSound = hasRepeatedFiringSound;
        this.firingSound = firingSound;
        this.hasActiveLoopSound = hasActiveLoopSound;

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // By calling this function, the weapon continues to stay used until the player stops pressing the use key
        user.setCurrentHand(hand);
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    // Mimick the animation used to shoot bows. It works pretty well with the weapon models made so far
    public UseAction getUseAction(ItemStack stack) { return UseAction.BOW; }

    @Override
    // Player can shoot weapon indefinitely
    public int getMaxUseTime(ItemStack stack) { return 1000000; }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        long currentTick = world.getTime();
        QuakePlayer player = (QuakePlayer) user;
        boolean clientside = world.isClient;

        if (currentTick - player.getWeaponTick(weaponSlot, clientside) >= refireRate) {
            if (!player.useAmmo(weaponSlot)) {
                if (!clientside) {
                    // Whatever projectile the weapon shoots, its initial position is approximated
                    // to be shot from the held weapon, not from the player's eye
                    Vec3d lookDir = Vec3d.fromPolar(user.getPitch(), user.getYaw());
                    Vec3d upDir = Vec3d.fromPolar(user.getPitch() + 90, user.getYaw());
                    Vec3d rightDir = lookDir.crossProduct(upDir).normalize().multiply(HITSCAN_HORIZONTAL_OFFSET);
                    Vec3d weaponPos = user.getEyePos().subtract(rightDir);
                    this.onWeaponRefire(world, user, stack, lookDir, weaponPos);
                }

                if (hasRepeatedFiringSound) {
                    world.playSoundFromEntity(null, user, firingSound, SoundCategory.PLAYERS, 1, 1);
                }
            }
            player.setWeaponTick(weaponSlot, currentTick, clientside);
        }
    }

    protected abstract void onWeaponRefire(World world, LivingEntity user, ItemStack stack, Vec3d lookDir, Vec3d weaponPos);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public Supplier<Object> getRenderProvider() { return renderProvider; }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private final GeoItemRenderer<?> renderer =
                    new GeoItemRenderer<>(new DefaultedItemGeoModel<>(weaponIdentifier));

            @Override
            public GeoItemRenderer<?> getCustomRenderer() {
                return this.renderer;
            }
        });
    }

    protected abstract PlayState handle(AnimationState<Weapon> state);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", this::handle)
                .triggerableAnim("shoot", DefaultAnimations.ATTACK_SHOOT)
                .triggerableAnim("idle", DefaultAnimations.IDLE));
    }
}
