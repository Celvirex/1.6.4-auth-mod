package net.legacyfabric.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.*;

public class AuthMod implements ModInitializer {
    
    private static DatabaseManager databaseManager;
    private static final Map<UUID, Boolean> authenticatedPlayers = new HashMap<>();
    
    @Override
    public void onInitialize() {
        System.out.println("AuthMod initializing...");
        
        // Initialize database connection when server starts
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                databaseManager = new DatabaseManager();
                databaseManager.initializeDatabase();
                System.out.println("AuthMod database initialized successfully");
            } catch (Exception e) {
                System.err.println("Failed to initialize database: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // Clean up when server stops
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (databaseManager != null) {
                databaseManager.close();
            }
        });
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }
    
    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Register command
        dispatcher.register(literal("register")
            .then(argument("password", StringArgumentType.word())
                .then(argument("confirmPassword", StringArgumentType.word())
                    .executes(context -> {
                        String password = StringArgumentType.getString(context, "password");
                        String confirmPassword = StringArgumentType.getString(context, "confirmPassword");
                        ServerCommandSource source = context.getSource();
                        ServerPlayerEntity player = source.getPlayer();
                        
                        if (player == null) {
                            source.sendError(Text.literal("This command can only be used by players"));
                            return 0;
                        }
                        
                        return registerPlayer(player, password, confirmPassword);
                    })
                )
            )
        );
        
        // Login command
        dispatcher.register(literal("login")
            .then(argument("password", StringArgumentType.word())
                .executes(context -> {
                    String password = StringArgumentType.getString(context, "password");
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity player = source.getPlayer();
                    
                    if (player == null) {
                        source.sendError(Text.literal("This command can only be used by players"));
                        return 0;
                    }
                    
                    return loginPlayer(player, password);
                })
            )
        );
        
        // Change password command
        dispatcher.register(literal("changepassword")
            .then(argument("oldPassword", StringArgumentType.word())
                .then(argument("newPassword", StringArgumentType.word())
                    .executes(context -> {
                        String oldPassword = StringArgumentType.getString(context, "oldPassword");
                        String newPassword = StringArgumentType.getString(context, "newPassword");
                        ServerCommandSource source = context.getSource();
                        ServerPlayerEntity player = source.getPlayer();
                        
                        if (player == null) {
                            source.sendError(Text.literal("This command can only be used by players"));
                            return 0;
                        }
                        
                        return changePassword(player, oldPassword, newPassword);
                    })
                )
            )
        );
    }
    
    private int registerPlayer(ServerPlayerEntity player, String password, String confirmPassword) {
        UUID playerId = player.getUuid();
        String playerName = player.getGameProfile().getName();
        
        if (!password.equals(confirmPassword)) {
            player.sendMessage(Text.literal("§cPasswords do not match!"), false);
            return 0;
        }
        
        if (password.length() < 6) {
            player.sendMessage(Text.literal("§cPassword must be at least 6 characters long!"), false);
            return 0;
        }
        
        try {
            if (databaseManager.isPlayerRegistered(playerId)) {
                player.sendMessage(Text.literal("§cYou are already registered! Use /login <password>"), false);
                return 0;
            }
            
            boolean success = databaseManager.registerPlayer(playerId, playerName, password);
            if (success) {
                authenticatedPlayers.put(playerId, true);
                player.sendMessage(Text.literal("§aSuccessfully registered and logged in!"), false);
                return 1;
            } else {
                player.sendMessage(Text.literal("§cRegistration failed. Please try again."), false);
                return 0;
            }
        } catch (SQLException e) {
            player.sendMessage(Text.literal("§cDatabase error. Please contact an administrator."), false);
            e.printStackTrace();
            return 0;
        }
    }
    
    private int loginPlayer(ServerPlayerEntity player, String password) {
        UUID playerId = player.getUuid();
        
        try {
            if (!databaseManager.isPlayerRegistered(playerId)) {
                player.sendMessage(Text.literal("§cYou are not registered! Use /register <password> <confirmPassword>"), false);
                return 0;
            }
            
            boolean success = databaseManager.authenticatePlayer(playerId, password);
            if (success) {
                authenticatedPlayers.put(playerId, true);
                player.sendMessage(Text.literal("§aSuccessfully logged in!"), false);
                return 1;
            } else {
                player.sendMessage(Text.literal("§cInvalid password!"), false);
                return 0;
            }
        } catch (SQLException e) {
            player.sendMessage(Text.literal("§cDatabase error. Please contact an administrator."), false);
            e.printStackTrace();
            return 0;
        }
    }
    
    private int changePassword(ServerPlayerEntity player, String oldPassword, String newPassword) {
        UUID playerId = player.getUuid();
        
        if (!isPlayerAuthenticated(playerId)) {
            player.sendMessage(Text.literal("§cYou must be logged in to change your password!"), false);
            return 0;
        }
        
        if (newPassword.length() < 6) {
            player.sendMessage(Text.literal("§cNew password must be at least 6 characters long!"), false);
            return 0;
        }
        
        try {
            boolean success = databaseManager.changePassword(playerId, oldPassword, newPassword);
            if (success) {
                player.sendMessage(Text.literal("§aPassword changed successfully!"), false);
                return 1;
            } else {
                player.sendMessage(Text.literal("§cOld password is incorrect!"), false);
                return 0;
            }
        } catch (SQLException e) {
            player.sendMessage(Text.literal("§cDatabase error. Please contact an administrator."), false);
            e.printStackTrace();
            return 0;
        }
    }
    
    public static boolean isPlayerAuthenticated(UUID playerId) {
        return authenticatedPlayers.getOrDefault(playerId, false);
    }
    
    public static void playerLoggedOut(UUID playerId) {
        authenticatedPlayers.remove(playerId);
    }
}
