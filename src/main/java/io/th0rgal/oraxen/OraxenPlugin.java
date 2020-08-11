package io.th0rgal.oraxen;

import io.th0rgal.oraxen.command.CommandProvider;
import io.th0rgal.oraxen.compatibilities.CompatibilitiesManager;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.language.FallbackHandler;
import io.th0rgal.oraxen.language.Translations;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.pack.generation.ResourcePack;
import io.th0rgal.oraxen.pack.upload.UploadManager;
import io.th0rgal.oraxen.recipes.RecipesManager;
import io.th0rgal.oraxen.settings.ConfigsManager;
import io.th0rgal.oraxen.settings.MessageOld;
import io.th0rgal.oraxen.settings.Plugin;
import io.th0rgal.oraxen.utils.OS;
import io.th0rgal.oraxen.utils.armorequipevent.ArmorListener;
import io.th0rgal.oraxen.utils.fastinv.FastInvManager;
import io.th0rgal.oraxen.utils.input.InputProvider;
import io.th0rgal.oraxen.utils.input.chat.ChatInputProvider;
import io.th0rgal.oraxen.utils.input.sign.SignMenuFactory;
import io.th0rgal.oraxen.utils.logs.Logs;

import java.util.function.Supplier;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OraxenPlugin extends JavaPlugin {

    private Supplier<InputProvider> inputProvider;
    private UploadManager uploadManager;

    private static OraxenPlugin oraxen;

    public OraxenPlugin() throws Exception {
        oraxen = this;
        Logs.enableFilter();
    }

    private void postLoading(ResourcePack resourcePack, ConfigsManager configsManager) {
        CommandProvider.register(this);
        (this.uploadManager = new UploadManager(this)).uploadAsyncAndSendToPlayers(resourcePack);
        new Metrics(this, 5371);
        pluginDependent();
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> OraxenItems.loadItems(configsManager));
    }

    private void pluginDependent() {
        PluginManager manager = Bukkit.getPluginManager();
        if (manager.getPlugin("ProtocolLib") != null) {
            this.inputProvider = () -> new SignMenuFactory(this).newProvider();
        } else {
            ChatInputProvider.load(this);
            this.inputProvider = () -> ChatInputProvider.getFree();
        }
    }

    public void onEnable() {
        ConfigsManager configsManager = new ConfigsManager(this);
        if (!configsManager.validatesConfig()) {
            MessageOld.CONFIGS_VALIDATION_FAILED.logError();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(new FallbackHandler(), this);
        Translations.MANAGER.reloadCatch();
        MechanicsManager.registerNativeMechanics();
        CompatibilitiesManager.enableNativeCompatibilities();
        OraxenItems.loadItems(configsManager);
        ResourcePack resourcePack = new ResourcePack(this);
        RecipesManager.load(this);
        FastInvManager.register(this);
        new ArmorListener(Plugin.ARMOR_EQUIP_EVENT_BYPASS.getAsStringList()).registerEvents(this);
        postLoading(resourcePack, configsManager);
        Logs.log(ChatColor.GREEN + "Successfully loaded on " + OS.getOs().getPlatformName());
    }

    public void onDisable() {
        unregisterListeners();
        CompatibilitiesManager.disableCompatibilities();
        Logs.log(ChatColor.GREEN + "Successfully unloaded");
    }

    private void unregisterListeners() {
        MechanicsManager.unloadListeners();
        if (ChatInputProvider.LISTENER != null)
            HandlerList.unregisterAll(ChatInputProvider.LISTENER);
        CommandProvider.unregister();
        HandlerList.unregisterAll(this);
    }

    public static OraxenPlugin get() {
        return oraxen;
    }

    public InputProvider getInputProvider() {
        return inputProvider.get();
    }

    public UploadManager getUploadManager() {
        return uploadManager;
    }

}
