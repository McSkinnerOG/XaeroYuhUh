package com.blacklizardcode.xaeronuhuh;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class XaeroNuhUh implements ModInitializer {
    public static final String MOD_ID = "xaeronuhuh";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static GameRules.Key<GameRules.BooleanRule> DisableMiniMap;
    private static GameRules.Key<GameRules.BooleanRule> EnableFairMode;

    // Keep track of previous game rule states
    private final Map<GameRules.Key<GameRules.BooleanRule>, Boolean> lastRuleStates = new HashMap<>();

    @Override
    public void onInitialize() {
        DisableMiniMap = GameRuleRegistry.register("DisableMiniMap", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        EnableFairMode = GameRuleRegistry.register("EnableFairMode", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

        // Initialize lastRuleStates
        lastRuleStates.put(DisableMiniMap, true);
        lastRuleStates.put(EnableFairMode, true);

        // Run logic on player join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> runJoinLogic(handler.getPlayer(), server.getOverworld()));

        // Check game rules each server tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();
            boolean fairMode = world.getGameRules().getBoolean(EnableFairMode);
            boolean worldMap = world.getGameRules().getBoolean(DisableMiniMap);

            boolean fairModeChanged = fairMode != lastRuleStates.get(EnableFairMode);
            boolean worldMapChanged = worldMap != lastRuleStates.get(DisableMiniMap);

            if (fairModeChanged || worldMapChanged) {
                // Re-run join logic for all online players
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    runJoinLogic(player, world);
                }

                // Update lastRuleStates
                lastRuleStates.put(EnableFairMode, fairMode);
                lastRuleStates.put(DisableMiniMap, worldMap);
            }
        });
    }

    private void runJoinLogic(ServerPlayerEntity player, ServerWorld world) {

        player.sendMessage(Text.literal("§r§e§s§e§t§x§a§e§r§o"), false);

        boolean GetEnableFairMode = world.getGameRules().getBoolean(EnableFairMode);
        boolean GetDisableMiniMap = world.getGameRules().getBoolean(DisableMiniMap);

        if (GetEnableFairMode) {
            player.sendMessage(Text.literal("§f§a§i§r§x§a§e§r§o"), false);
        }
        if (GetDisableMiniMap) {
            player.sendMessage(Text.literal("§n§o§m§i§n§i§m§a§p"), false);
        }
    }
}
