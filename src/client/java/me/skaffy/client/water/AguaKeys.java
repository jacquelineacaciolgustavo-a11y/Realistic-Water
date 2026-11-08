package me.skaffy.client.water;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import me.skaffy.RealisticWater;
import me.skaffy.block.SourceBlock;
import me.skaffy.net.SourceToggleAllPayload;
import me.skaffy.net.SourceToggleVisiblePayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class AguaKeys {
    /** kind >= 6 significa "todos os tipos de fonte" no SourceToggleAllPayload. */
    private static final int ALL_KINDS = 6;
    /** Raio (em blocos) em que a tecla N procura fontes na tela. */
    private static final int VISIBLE_RADIUS = 64;

    private static KeyMapping keyCloseAll;
    private static KeyMapping keyOpenVisible;
    private static KeyMapping keyInteractRemote;
    private static KeyMapping keyEmergencyClear;

    private AguaKeys() {}

    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.register(RealisticWater.id("realistic_fluids"));

        keyCloseAll = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.realistic-water.close_all_sources", InputConstants.KEY_B, category));

        keyOpenVisible = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.realistic-water.open_visible_sources", InputConstants.KEY_N, category));

        keyInteractRemote = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.realistic-water.interact_remote", InputConstants.KEY_G, category));

        keyEmergencyClear = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.realistic-water.emergency_clear", InputConstants.KEY_J, category));

        ClientTickEvents.END_CLIENT_TICK.register(AguaKeys::tick);
    }

    private static void tick(Minecraft mc) {
        while (keyCloseAll.consumeClick()) {
            if (mc.gui.screen() == null) {
                sources(mc, ALL_KINDS, 0);
            }
        }

        while (keyOpenVisible.consumeClick()) {
            if (mc.gui.screen() == null) {
                openVisible(mc);
            }
        }

        while (keyInteractRemote.consumeClick()) {
            if (mc.gui.screen() == null) {
                interactRemote(mc);
            }
        }

        while (keyEmergencyClear.consumeClick()) {
            if (mc.gui.screen() == null) {
                clearAllEmergency(mc);
            }
        }
    }

    /** N: abre so os fontes que estao dentro do campo de visao (o que aparece na tela). */
    private static void openVisible(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;
        if (!ClientPlayNetworking.canSend(SourceToggleVisiblePayload.TYPE)) return;

        Vec3 eye = mc.player.getEyePosition();
        Vec3 forward = mc.player.getViewVector(1.0F).normalize();
        Vec3 right = forward.cross(new Vec3(0.0, 1.0, 0.0));
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        }
        right = right.normalize();
        Vec3 up = right.cross(forward).normalize();

        double tanY = Math.tan(Math.toRadians(mc.options.fov().get()) * 0.5) * 1.15;
        double aspect = (double) mc.getWindow().getWidth() / Math.max(1, mc.getWindow().getHeight());
        double tanX = tanY * aspect;

        List<BlockPos> found = new ArrayList<>();
        BlockPos center = mc.player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        double maxDistSq = (double) VISIBLE_RADIUS * VISIBLE_RADIUS;

        for (int dx = -VISIBLE_RADIUS; dx <= VISIBLE_RADIUS && found.size() < SourceToggleVisiblePayload.MAX_POSITIONS; dx++) {
            for (int dz = -VISIBLE_RADIUS; dz <= VISIBLE_RADIUS && found.size() < SourceToggleVisiblePayload.MAX_POSITIONS; dz++) {
                if (dx * dx + dz * dz > maxDistSq) continue;
                for (int dy = -VISIBLE_RADIUS; dy <= VISIBLE_RADIUS; dy++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = mc.level.getBlockState(cursor);
                    if (!(state.getBlock() instanceof SourceBlock)) continue;

                    double ox = cursor.getX() + 0.5 - eye.x;
                    double oy = cursor.getY() + 0.5 - eye.y;
                    double oz = cursor.getZ() + 0.5 - eye.z;
                    double depth = ox * forward.x + oy * forward.y + oz * forward.z;
                    if (depth <= 0.0) continue;
                    double sx = (ox * right.x + oy * right.y + oz * right.z) / depth;
                    double sy = (ox * up.x + oy * up.y + oz * up.z) / depth;
                    if (Math.abs(sx) <= tanX && Math.abs(sy) <= tanY) {
                        found.add(cursor.immutable());
                    }
                }
            }
        }

        ClientPlayNetworking.send(new SourceToggleVisiblePayload((byte) 1, found));
    }

    private static void interactRemote(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        HitResult hit = mc.player.pick(64.0D, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            if (state.getBlock() instanceof SourceBlock) {
                SourceScreen.openNextTick(pos, state);
                mc.player.sendOverlayMessage(Component.translatable("block.realistic-water.source.remote_opened"));
                return;
            }
        }
        mc.player.sendOverlayMessage(Component.translatable("block.realistic-water.source.remote_miss"));
    }

    public static void sources(Minecraft mc, int kind, int mode) {
        if (mc.player != null && ClientPlayNetworking.canSend(SourceToggleAllPayload.TYPE)) {
            ClientPlayNetworking.send(new SourceToggleAllPayload((byte) kind, (byte) mode));
        }
    }

    public static void clearAllEmergency(Minecraft mc) {
        if (mc.player == null) return;
        int radius = mc.options.renderDistance().get() + 4;
        WaterWorld world = WaterWorld.get();

        for (Material m : Material.values()) {
            world.removeAll(radius, m);
        }
        sources(mc, ALL_KINDS, 0);

        mc.player.sendSystemMessage(Component.translatable("realistic-water.emergency.cleared"));
    }
}
