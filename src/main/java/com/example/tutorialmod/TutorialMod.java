package com.example.tutorialmod;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Identifier;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.*;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class TutorialMod implements ModInitializer {
	public static final String MOD_ID = "tutorial-mod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private int tickCounter = 0;

	public static final GameRules.Key<GameRules.IntRule> INVISIBLE_BLOCK_INTERVAL =
		GameRuleRegistry.register("invisibleBlockInterval", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(10, 1));

	public static boolean isSpeedrunEssential(Block block) {
		Identifier id = Registries.BLOCK.getId(block);
		String path = id.getPath();

		// 1. Ignore decorative/technical variants
		if (path.contains("stairs") || path.contains("slab") || path.contains("wall") || 
			path.contains("fence") || path.contains("button") || path.contains("pressure_plate") ||
			path.contains("door") || path.contains("trapdoor") || path.contains("sign") ||
			path.contains("potted") || path.contains("candle") || path.contains("banner") ||
			path.contains("hanging_sign") || path.contains("pane")) {
			return false;
		}

		// 2. Ignore non-essential stone/building variants
		if (path.contains("polished") || path.contains("bricks") || path.contains("tiles") || 
			path.contains("chiseled") || path.contains("cracked") || path.contains("cut")) {
			// Keep basic stone bricks and nether bricks for structure challenge
			if (!path.equals("stone_bricks") && !path.equals("nether_bricks")) {
				return false;
			}
		}

		// 3. Ignore nature junk (except for things that might be used)
		if (path.endsWith("_tulip") || path.endsWith("_flower") || path.endsWith("_orchid") || 
			path.endsWith("_daisy") || path.equals("dandelion") || path.equals("poppy") ||
			path.contains("sapling") || path.equals("grass") || path.equals("fern") || 
			path.equals("dead_bush") || path.contains("leaves") || path.contains("vine") || 
			path.contains("lichen")) {
			return false;
		}

		// 4. Ignore all colored variants (Wool, Concrete, etc.)
		String[] colors = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
		for (String color : colors) {
			if (path.startsWith(color + "_")) return false;
		}

		// 5. Ignore "junk" materials
		if (path.contains("copper") || path.contains("terracotta") || path.contains("concrete") || 
			path.contains("coral") || path.contains("glazed") || path.contains("infested") || 
			path.contains("amethyst") || path.contains("tuff") || path.contains("calcite") ||
			path.contains("mud") || path.contains("froglight") || path.contains("sculk")) {
			return false;
		}

		return !block.getDefaultState().isAir() && block != Blocks.BARRIER;
	}

	@Override
	public void onInitialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			int intervalSeconds = server.getGameRules().getInt(INVISIBLE_BLOCK_INTERVAL);
			int intervalTicks = Math.max(1, intervalSeconds * 20);

			tickCounter++;
			if (tickCounter >= intervalTicks) { 
				tickCounter = 0;

				long worldSeed = server.getOverworld().getRegistryKey().getValue().hashCode();
				long currentInterval = server.getOverworld().getTime() / intervalTicks;

				// Group blocks by translation key for consistency (matches client logic)
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

				if (currentInterval > 0 && currentInterval <= shuffledGroups.size()) {
					Block newlyInvisible = shuffledGroups.get((int) currentInterval - 1);
					if (!newlyInvisible.getDefaultState().isAir() && newlyInvisible != Blocks.BARRIER) {
						server.getPlayerManager().broadcast(Text.literal("§c" + newlyInvisible.getName().getString() + " is now invisible!"), false);
					}
				}
			}

		});
	}
}
