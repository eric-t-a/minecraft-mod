package com.example.tutorialmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.example.tutorialmod.TutorialMod;

public class TutorialModClient implements ClientModInitializer {
    private long lastMinute = -1;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) return;

            int intervalSeconds = client.world.getGameRules().getInt(TutorialMod.INVISIBLE_BLOCK_INTERVAL);
            int intervalTicks = Math.max(1, intervalSeconds * 20);

            long currentInterval = client.world.getTime() / intervalTicks;

            if (currentInterval != lastMinute) {
                if (lastMinute != -1) {
                    // Smoother way: Schedule a redraw of all blocks in view distance
                    int dist = client.options.getClampedViewDistance() * 16;
                    int x = (int) client.player.getX();
                    int y = (int) client.player.getY();
                    int z = (int) client.player.getZ();
                    
                    client.worldRenderer.scheduleBlockRenders(
                        x - dist, y - dist, z - dist,
                        x + dist, y + dist, z + dist
                    );
                }
                lastMinute = currentInterval;
            }
        });
    }
}
