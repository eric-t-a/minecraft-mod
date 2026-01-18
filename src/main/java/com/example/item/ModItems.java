package com.example.item;

import com.example.TemplateMod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemGroups;

public class ModItems {
    public static final Item PINK_GARNET = registerItem("pink_garnet", new Item.Settings());
    public static final Item RAW_PINK_GARNET = registerItem("raw_pink_garnet", new Item.Settings());
    
    public static final Item registerItem(String name, Item.Settings settings) {
        Identifier id = Identifier.of(TemplateMod.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(Registries.ITEM.getKey(), id);
        Item item = new Item(settings.registryKey(key));
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void registerModItems() {
        TemplateMod.LOGGER.info("Registering Mod Items for " + TemplateMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(PINK_GARNET);
            entries.add(RAW_PINK_GARNET);
        });
    }
}
