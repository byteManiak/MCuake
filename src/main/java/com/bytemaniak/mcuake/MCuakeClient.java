package com.bytemaniak.mcuake;

import com.bytemaniak.mcuake.blocks.jumppad.JumppadScreen;
import com.bytemaniak.mcuake.cs.ClientReceivers;
import com.bytemaniak.mcuake.entity.projectile.PlasmaBallRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class MCuakeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient()
    {
        ClientReceivers.init();
        HandledScreens.register(MCuake.JUMPPAD_SCREEN_HANDLER, JumppadScreen::new);
        EntityRendererRegistry.register(MCuake.PLASMA_BALL, (ctx) -> new PlasmaBallRenderer(ctx));
    }
}
