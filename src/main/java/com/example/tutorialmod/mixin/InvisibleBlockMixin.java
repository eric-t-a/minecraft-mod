package com.example.tutorialmod.mixin;

import com.example.tutorialmod.TutorialMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(BlockModels.class)
public class InvisibleBlockMixin {
    @Unique
    private static volatile Set<String> invisibleKeys = Collections.emptySet();
    @Unique
    private static long lastSeed = -1;
    @Unique
    private static long lastInterval = -1;
    @Unique
    private static int lastIntervalValue = -1;

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void onGetModel(BlockState state, CallbackInfoReturnable<BakedModel> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || state.isOf(Blocks.BARRIER) || state.isAir()) return;

        int intervalSeconds = client.world.getGameRules().getInt(TutorialMod.INVISIBLE_BLOCK_INTERVAL);
        int intervalTicks = Math.max(1, intervalSeconds * 20);

        long worldSeed = client.world.getRegistryKey().getValue().hashCode();
        long currentInterval = client.world.getTime() / intervalTicks;

        // Thread-safe update based on Translation Keys
        if (currentInterval != lastInterval || worldSeed != lastSeed || intervalTicks != lastIntervalValue) {
            synchronized (InvisibleBlockMixin.class) {
                if (currentInterval != lastInterval || worldSeed != lastSeed || intervalTicks != lastIntervalValue) {
                    lastSeed = worldSeed;
                    lastInterval = currentInterval;
                    lastIntervalValue = intervalTicks;

                    // Group blocks by translation key for consistency (e.g. torch and wall_torch)
                    Map<String, Block> uniqueBlocks = new LinkedHashMap<>();
                    List<Block> allBlocks = Registries.BLOCK.stream()
                        .filter(TutorialMod::isSpeedrunEssential)
                        .sorted(Comparator.comparing(b -> Registries.BLOCK.getId(b).toString()))
                        .toList();
                    
                    for (Block b : allBlocks) {
                        uniqueBlocks.putIfAbsent(b.getTranslationKey(), b);
                    }
                    
                    List<Block> shuffledGroups = new ArrayList<>(uniqueBlocks.values());
                    Collections.shuffle(shuffledGroups, new java.util.Random(worldSeed));

                    Set<String> newSet = new HashSet<>();
                    int count = (int) Math.min(currentInterval, shuffledGroups.size());
                    for (int i = 0; i < count; i++) {
                        newSet.add(shuffledGroups.get(i).getTranslationKey());
                    }
                    invisibleKeys = Collections.unmodifiableSet(newSet);
                }
            }
        }

        if (invisibleKeys.contains(state.getBlock().getTranslationKey())) {
            BlockModels models = (BlockModels) (Object) this;
            // Use the Barrier model instead of null to avoid NullPointerException in some renderers (like Indigo)
            cir.setReturnValue(models.getModel(Blocks.BARRIER.getDefaultState()));
        }
    }
}
