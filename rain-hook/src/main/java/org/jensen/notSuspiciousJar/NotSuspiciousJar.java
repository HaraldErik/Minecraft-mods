package org.jensen.notSuspiciousJar;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class NotSuspiciousJar extends JavaPlugin {

    private static final String WEBHOOK_URL = "Not sharing my webhook buddy";

    // Track weather state for each world
    private Map<String, Boolean> isRaining = new HashMap<>();
    private Map<String, Boolean> isThundering = new HashMap<>();

    @Override
    public void onEnable() {


        // Initialize weather states for all worlds
        for (World world : Bukkit.getWorlds()) {
            isRaining.put(world.getName(), world.hasStorm());
            isThundering.put(world.getName(), world.isThundering());
        }

        // Check weather every 5 seconds (100 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                checkWeatherChanges();
            }
        }.runTaskTimer(this, 0L, 100L);
    }

    @Override
    public void onDisable() {

    }

    private void checkWeatherChanges() {
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            boolean currentlyRaining = world.hasStorm();
            boolean currentlyThundering = world.isThundering();

            // Check rain state change
            if (isRaining.get(worldName) != currentlyRaining) {
                if (currentlyRaining) {
                    sendWebhook("🌧️ It's starting to rain in **" + worldName + "**!");
                } else {
                    sendWebhook("☀️ The rain has stopped in **" + worldName + "**!");
                }
                isRaining.put(worldName, currentlyRaining);
            }

            // Check thunder state change
            if (isThundering.get(worldName) != currentlyThundering) {
                if (currentlyThundering) {
                    sendWebhook("⚡ Thunderstorm starting in **" + worldName + "**!");
                } else {
                    sendWebhook("🌤️ Thunderstorm ended in **" + worldName + "**!");
                }
                isThundering.put(worldName, currentlyThundering);
            }
        }
    }

    private void sendWebhook(String message) {
        // Run async to avoid blocking the main server thread
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{\"content\": \"" + message + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 204 || responseCode == 200) {

                } else {

                }

                conn.disconnect();
            } catch (Exception e) {

            }
        });
    }
}