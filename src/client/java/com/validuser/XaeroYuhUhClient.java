package com.validuser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
public class XaeroYuhUhClient implements ClientModInitializer {
 public static final String MOD_ID = "xaeroyuhuh";
 public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
 private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
 private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("xaeroyuhuh.json");
 // all lowercase, trimmed
 private static final Set<String> BLOCK_PATTERNS = new HashSet<>();
 private static Config CONFIG;
 // ========================================================================
 // MESSAGE HANDLING
 // ========================================================================
 private static boolean handleIncomingMessage(Text message, String channel, boolean overlay) {
  // Raw as it comes from the server (including '§')
  String raw = message.getString();
  String rawLower = raw.toLowerCase(Locale.ROOT);
  boolean blocked = shouldBlock(rawLower);
  String prefix = blocked ? "§c[BLOCKED]" : "§a[ALLOW]";
  String rawShown = sanitizeForChat(raw);
  if (CONFIG.verbose) {
   // Show what the server tried to send
   sendClientMessage(prefix + " §2[" + channel.toUpperCase(Locale.ROOT) + "]§r \"§e" + rawShown + "§r\"");
  }
  if (blocked) {
   sendClientMessage(prefix + " §2[" + channel.toUpperCase(Locale.ROOT) + "]§r \"§e" + rawShown + "§r\"");
   return false; // prevent other mods and the chat HUD from seeing it
  }
  return true;
 }
 private static boolean shouldBlock(String rawLower) {
  // User-configured patterns (substring match)
  for (String pattern : BLOCK_PATTERNS) {
   if (pattern.isEmpty()) continue;
   if (rawLower.contains(pattern)) return true;
  }
  return false;
 }
 // ========================================================================
 // COMMAND IMPLEMENTATIONS
 // ========================================================================
 private static void toggleVerbose() {
  CONFIG.verbose = !CONFIG.verbose;
  saveConfig();
  sendClientMessage("Verbose mode is now " +
    (CONFIG.verbose ? "§aENABLED" : "§cDISABLED") + "§r.");
 }
 private static void addPattern(String patternRaw) {
  String norm = normalize(patternRaw);
  if (norm.isEmpty()) {
   sendClientMessage("§cPattern cannot be empty.");
   return;
  }
  if (BLOCK_PATTERNS.contains(norm)) {
   sendClientMessage("§ePattern already in block list: §f\"" + norm + "\"");
   return;
  }
  CONFIG.blockedSubstrings.add(norm);
  reloadPatternsFromConfig();
  saveConfig();
  sendClientMessage("§aAdded block pattern: §f\"" + norm + "\"");
 }
 private static void removePattern(String patternRaw) {
  String norm = normalize(patternRaw);
  if (!BLOCK_PATTERNS.contains(norm)) {
   sendClientMessage("§ePattern not found in block list: §f\"" + norm + "\"");
   return;
  }
  CONFIG.blockedSubstrings.removeIf(s -> s.equals(norm));
  reloadPatternsFromConfig();
  saveConfig();
  sendClientMessage("§aRemoved block pattern: §f\"" + norm + "\"");
 }
 private static void listPatterns() {
  if (BLOCK_PATTERNS.isEmpty()) {
   sendClientMessage("Block list is empty.");
   return;
  }
  sendClientMessage("§6Current XaeroYuhUh block patterns (§f" + BLOCK_PATTERNS.size() + "§6):");
  for (String pattern : BLOCK_PATTERNS) {
   sendClientMessage("  §7- §f\"" + pattern + "\"");
  }
 }
 private static void loadConfig() {
  if (Files.exists(CONFIG_PATH)) {
   try {
    String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
    CONFIG = GSON.fromJson(json, Config.class);
    if (CONFIG == null) throw new IllegalStateException("Config parsed as null");
    LOGGER.info("[XaeroYuhUh] Loaded config from {}", CONFIG_PATH);
   } catch (Exception e) {
    LOGGER.error("[XaeroYuhUh] Failed to read config, using defaults.", e);
    CONFIG = createDefaultConfig();
    saveConfig();
   }
  } else {
   CONFIG = createDefaultConfig();
   saveConfig();
  }
  reloadPatternsFromConfig();
 }
 private static void saveConfig() {
  try {
   Files.createDirectories(CONFIG_PATH.getParent());
   String json = GSON.toJson(CONFIG);
   Files.writeString(CONFIG_PATH, json, StandardCharsets.UTF_8);
   LOGGER.info("[XaeroYuhUh] Saved config to {}", CONFIG_PATH);
  } catch (IOException e) {
   LOGGER.error("[XaeroYuhUh] Failed to save config!", e);
  }
 }
 private static void reloadPatternsFromConfig() {
  BLOCK_PATTERNS.clear();
  if (CONFIG.blockedSubstrings != null) {
   for (String s : CONFIG.blockedSubstrings) {
    String n = normalize(s);
    if (!n.isEmpty()) BLOCK_PATTERNS.add(n);
   }
  }
 }
 private static Config createDefaultConfig() {
  Config cfg = new Config();
  cfg.verbose = false;
  // Plain
  cfg.blockedSubstrings.add("resetxaero");
  cfg.blockedSubstrings.add("fairxaero");
  cfg.blockedSubstrings.add("nominimap");
  // Special
  cfg.blockedSubstrings.add("§r§e§s§e§t§x§a§e§r§o");
  cfg.blockedSubstrings.add("§f§a§i§r§x§a§e§r§o");
  cfg.blockedSubstrings.add("§n§o§m§i§n§i§m§a§p");
  // Variants
  cfg.blockedSubstrings.add("reset xaero");
  cfg.blockedSubstrings.add("fair xaero");
  cfg.blockedSubstrings.add("no minimap");
  return cfg;
 }
 // ========================================================================
 // UTILS
 // ========================================================================
 private static String normalize(String s) {
  return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
 }
 /**
  * Remove '§x' formatting codes (like Minecraft does when rendering).
  */
 private static String stripFormatting(String raw) {
  if (raw == null || raw.isEmpty()) return "";

  StringBuilder out = new StringBuilder(raw.length());
  int len = raw.length();

  for (int i = 0; i < len; i++) {
   char c = raw.charAt(i);
   if (c == '§' && i + 1 < len) {  // skip next safe
    i++;
   } else {
    out.append(c);
   }
  }
  return out.toString();
 }
 /**
  * Replace '§' so it doesn't mess up the chat formatting in our debug output.
  */
 private static String sanitizeForChat(String raw) {
  if (raw == null || raw.isEmpty()) return "";
  return raw.replace('§', '&');
 }
 private static String sanitizeForLog(String raw) {
  if (raw == null) return "null";
  return raw.replace("\n", "\\n").replace("\r", "\\r");
 }
 private static void sendClientMessage(String msg) {
  MinecraftClient client = MinecraftClient.getInstance();
  if (client == null || client.inGameHud == null) return;
  client.execute(() ->
    client.inGameHud.getChatHud().addMessage(
      Text.literal("§6[Ξ] §r" + msg)
    ));
 }
 @Override
 public void onInitializeClient() {
  loadConfig();
  // === MESSAGE FILTERS (server -> client) ===
  // Game messages: (player.sendMessage(Text, false))
  ClientReceiveMessageEvents.ALLOW_GAME.register((Text message, boolean overlay) -> handleIncomingMessage(message, "game", overlay));
  // Chat messages:
  ClientReceiveMessageEvents.ALLOW_CHAT.register((
    Text message,
    SignedMessage signedMessage,
    GameProfile sender,
    MessageType.Parameters params,
    Instant receptionTimestamp
  ) -> handleIncomingMessage(message, "chat", false));
  ClientReceiveMessageEvents.GAME_CANCELED.register((Text message, boolean overlay) -> LOGGER.info("[XaeroYuhUh] Game message canceled: {}", sanitizeForLog(message.getString())));
  ClientReceiveMessageEvents.CHAT_CANCELED.register((
    Text message,
    SignedMessage signedMessage,
    GameProfile sender,
    MessageType.Parameters params,
    Instant receptionTimestamp
  ) -> LOGGER.info("[XaeroYuhUh] Chat message canceled: {}", sanitizeForLog(message.getString())));
  // === CLIENT COMMANDS ===
  ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
    literal("xaeroyuhuh")
      .then(literal("verbose").executes(ctx -> {
       toggleVerbose();
       return 1;
      }))
      .then(literal("add")
        .then(argument("pattern", StringArgumentType.greedyString())
          .executes(ctx -> {
           addPattern(StringArgumentType.getString(ctx, "pattern"));
           return 1;
          })))
      .then(literal("remove")
        .then(argument("pattern", StringArgumentType.greedyString())
          .executes(ctx -> {
           removePattern(StringArgumentType.getString(ctx, "pattern"));
           return 1;
          })))
      .then(literal("list").executes(ctx -> {
       listPatterns();
       return 1;
      }))
      .then(literal("help").executes(ctx -> {
       sendClientMessage("Commands:");
       sendClientMessage("  /xaeroyuhuh verbose  - toggle verbose logging of all server messages");
       sendClientMessage("  /xaeroyuhuh add <text>  - add a substring to block (case-insensitive)");
       sendClientMessage("  /xaeroyuhuh remove <text>  - remove a substring from the block list");
       sendClientMessage("  /xaeroyuhuh list  - show current block patterns");
       return 1;
      }))
  ));
  LOGGER.info("[XaeroYuhUh] Initialized.");
 }
 // ========================================================================
 // CONFIG
 // ========================================================================
 public static class Config {
  public boolean verbose = false;
  public List<String> blockedSubstrings = new ArrayList<>();
 }
}