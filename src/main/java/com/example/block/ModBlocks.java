package com.example.block;

import com.example.TemplateMod;

import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.item.ItemGroups;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ModBlocks {
    public static final Block PINK_GARNET_BLOCK = registerBlock(
        "pink_garnet_block",
        AbstractBlock.Settings.create().strength(4.0f).requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)
    );
    public static final Block RAW_PINK_GARNET_BLOCK = registerBlock(
        "raw_pink_garnet_block",
        AbstractBlock.Settings.create().strength(4.0f).requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)
    );

    private static Block registerBlock(String name, AbstractBlock.Settings settings) {
        Identifier id = Identifier.of(TemplateMod.MOD_ID, name);
        RegistryKey<Block> key = RegistryKey.of(Registries.BLOCK.getKey(), id);
        Block block = new Block(settings.registryKey(key));
        registerBlockItem(id, block);
        return Registry.register(Registries.BLOCK, id, block);
    }

    private static void registerBlockItem(Identifier id, Block block) {
        RegistryKey<Item> key = RegistryKey.of(Registries.ITEM.getKey(), id);
        BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(key));
        Registry.register(Registries.ITEM, id, blockItem);
    }
    

    public static void registerModBlocks() {
        TemplateMod.LOGGER.info("Registering Mod Blocks for " + TemplateMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(PINK_GARNET_BLOCK);
            entries.add(RAW_PINK_GARNET_BLOCK);
        }); 
    }
}
