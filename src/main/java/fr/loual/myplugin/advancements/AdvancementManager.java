package fr.loual.myplugin.advancements;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class AdvancementManager {

    private final JavaPlugin plugin;

    public AdvancementManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void installDatapack() {
        File datapackFolder = new File(
                Bukkit.getWorldContainer(),
                "world/datapacks/elisashorses"
        );

        // Supprimer l'ancien dossier myplugin s'il existe
        File oldDatapack = new File(
                Bukkit.getWorldContainer(),
                "world/datapacks/myplugin"
        );
        if (oldDatapack.exists()) {
            deleteDirectory(oldDatapack);
        }

        boolean copied = copyDatapack("datapack", datapackFolder);
        if (copied) {
            plugin.getLogger().info("Datapack elisashorses copié avec succès !");
            Bukkit.reloadData();
        }
    }

    public void award(Player player, String advancementId) {

        NamespacedKey key = new NamespacedKey(plugin, advancementId);
        Advancement advancement = Bukkit.getAdvancement(key);

        if (advancement == null) {
            plugin.getLogger().warning("Advancement introuvable : " + key);
            return;
        }

        AdvancementProgress progress = player.getAdvancementProgress(advancement);

        for (String criterion : progress.getRemainingCriteria()) {
            progress.awardCriteria(criterion);
        }
    }

    public void discoverRecipe(Player player, String recipeId) {
        NamespacedKey key = new NamespacedKey(plugin, recipeId);
        player.discoverRecipe(key);
    }

    private boolean copyDatapack(String resourcePath, File destination) {
        try {
            destination.mkdirs();

            java.net.URL codeSource = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            File sourceFile = new File(codeSource.toURI());

            if (sourceFile.isFile()) {
                try (java.util.jar.JarFile jar = new java.util.jar.JarFile(sourceFile)) {
                    var entries = jar.entries();
                    String prefix = resourcePath + "/";

                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement();
                        String name = entry.getName();

                        if (name.startsWith(prefix)) {
                            String relative = name.substring(prefix.length());
                            if (relative.isEmpty()) continue;

                            File target = new File(destination, relative);

                            if (entry.isDirectory()) {
                                target.mkdirs();
                            } else {
                                target.getParentFile().mkdirs();

                                try (InputStream in = jar.getInputStream(entry)) {
                                    Files.copy(
                                            in,
                                            target.toPath(),
                                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                                    );
                                }
                            }
                        }
                    }
                    return true;
                }
            } else if (sourceFile.isDirectory()) {
                File sourceDir = new File(sourceFile, resourcePath);
                if (sourceDir.exists() && sourceDir.isDirectory()) {
                    try (var stream = Files.walk(sourceDir.toPath())) {
                        stream.forEach(path -> {
                            try {
                                java.nio.file.Path dest = destination.toPath().resolve(sourceDir.toPath().relativize(path).toString());
                                if (Files.isDirectory(path)) {
                                    Files.createDirectories(dest);
                                } else {
                                    Files.createDirectories(dest.getParent());
                                    Files.copy(path, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe(
                    "Impossible d'installer le datapack : " + e.getMessage()
            );
        }
        return false;
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}

