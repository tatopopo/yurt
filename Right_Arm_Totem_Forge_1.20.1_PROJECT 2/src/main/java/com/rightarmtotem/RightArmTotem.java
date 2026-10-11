package com.rightarmtotem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingUseTotemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(RightArmTotem.MODID)
public class RightArmTotem {
    public static final String MODID = "rightarmtotem";
    private static boolean rightArmGone = false;
    private static final Path FLAG = FMLPaths.CONFIGDIR.get().resolve("right-arm-totem-used.txt");

    public RightArmTotem() {
        rightArmGone = Files.exists(FLAG);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static void permanentlyHideRightArm() {
        rightArmGone = true;
        try {
            Files.createDirectories(FLAG.getParent());
            Files.writeString(FLAG, "The Totem of Undying was used. Right arm hidden permanently.\n");
        } catch (IOException ignored) {
        }
    }

    @SubscribeEvent
    public void onTotemUsed(LivingUseTotemEvent event) {
        if (event.getEntity().level().isClientSide()
                && event.getEntity() instanceof LocalPlayer
                && event.getTotem().is(Items.TOTEM_OF_UNDYING)) {
            permanentlyHideRightArm();
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
            if (!rightArmGone) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || event.getEntity() != mc.player) return;

            PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
            model.rightArm.visible = false;
            model.rightSleeve.visible = false;
        }

        @SubscribeEvent
        public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
            if (!rightArmGone) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || event.getEntity() != mc.player) return;

            PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
            model.rightArm.visible = true;
            model.rightSleeve.visible = true;
        }

        @SubscribeEvent
        public static void onRenderHand(RenderHandEvent event) {
            if (!rightArmGone) return;

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;

            // For the normal right-handed player, MAIN_HAND is the visible right hand.
            // Canceling this event hides the first-person right hand and whatever it is holding.
            if (event.getHand() == InteractionHand.MAIN_HAND) {
                event.setCanceled(true);
            }
        }
    }
}
