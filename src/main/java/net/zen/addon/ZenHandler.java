package net.zen.addon;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.zen.addon.features.modules.movement.*;
import net.zen.addon.features.modules.utility.*;
import net.zen.addon.features.modules.render.*;
import net.zen.addon.features.commands.commands.info.*;
import net.zen.addon.features.commands.commands.utility.*;
import net.zen.addon.utils.ZenChatPrefix;

import org.slf4j.Logger;

public class ZenHandler extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Zen");
    public static final HudGroup HUD_GROUP = new HudGroup("Zen");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Zen Additions");

        ZenChatPrefix.register();

        // Modules
        //Modules.get().add(new ModuleExample());
        Modules.get().add(new EasyBedrockBreaker());
        Modules.get().add(new AccurateBlockPlacement());
        Modules.get().add(new Moses());
        Modules.get().add(new Freeze());
        Modules.get().add(new NoGravity());
        Modules.get().add(new ProjectileTrails());
        Modules.get().add(new GhostBlockFly());
        //Modules.get().add(new EnhancedStorageESP());


        // Commands
        //Commands.add(new CommandExample());
        Commands.add(new WorldBorderCommand());
        Commands.add(new PingCommand());
        Commands.add(new IpCommand());
        Commands.add(new EntityNbtCommand());
        Commands.add(new UuidCommand());
        Commands.add(new StatsCommand());
        Commands.add(new EquipCommand());

        // HUD
        //Hud.get().register(HudExample.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "net.zen.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("yesEngineer", "zen-additions");
    }
}
