package io.th0rgal.oraxen.command;

import static io.th0rgal.oraxen.language.Translations.translate;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.oraxen.chimerate.commons.command.dispatcher.Dispatcher;
import com.oraxen.chimerate.commons.command.tree.nodes.Argument;
import com.oraxen.chimerate.commons.command.tree.nodes.Literal;
import com.oraxen.chimerate.commons.command.tree.nodes.Literal.Builder;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.command.commands.CommandListener;
import io.th0rgal.oraxen.event.command.OraxenCommandEvent;
import io.th0rgal.oraxen.language.DescriptionType;
import io.th0rgal.oraxen.language.Language;
import io.th0rgal.oraxen.language.LanguageProvider;
import io.th0rgal.oraxen.language.Message;
import io.th0rgal.oraxen.utils.general.Placeholder;

public class CommandProvider {

    public static final InfoProvider INFO_PROVIDER = new InfoProvider();
    public static final Listener LISTENER = new CommandListener();

    /*
     * 
     */

    public static void register(OraxenPlugin plugin) {

        Bukkit.getPluginManager().registerEvents(LISTENER, plugin);

        Dispatcher dispatcher = Dispatcher.of(plugin);

        OraxenCommandEvent event = new OraxenCommandEvent(dispatcher, "oraxen", "oxn", "o");

        Bukkit.getPluginManager().callEvent(event);

        oraxenCommand(dispatcher, event.getAliases(), event.getCommandInfos());

        dispatcher.update();

    }

    /*
     * 
     */

    public static void unregister() {
        HandlerList.unregisterAll(LISTENER);
    }

    /*
     * 
     */

    @SuppressWarnings("unchecked")
    private static CommandNode<CommandSender>[] oraxenCommand(Dispatcher dispatcher, List<String> aliases,
        List<CommandInfo> commandInfos) {

        //
        // Create Oraxen main command

        Builder<CommandSender> oraxenNode = Literal.of("oraxen").alias("oxn", "o");

        //
        // Loop through infos

        for (CommandInfo info : commandInfos) {

            //
            // Add nodes to commands

            oraxenNode.then(info.getNode());

        }

        //
        // Create help command

        Builder<CommandSender> help = Literal.of("help");
        help.alias("?");

        INFO_PROVIDER.addAll(commandInfos);

        help
            .optionally(Argument
                .of("page", IntegerArgumentType.integer(1, INFO_PROVIDER.getPageCount()))
                .executes((sender, context) -> {

                    int page = context.getOptionalArgument("page", int.class, 1);

                    List<CommandInfo> infos = INFO_PROVIDER.getInfos(page);

                    Language language = LanguageProvider.getLanguageOf(sender);

                    for (CommandInfo info : infos) {

                        Message.COMMAND_HELP_INFO_SHORT
                            .send(sender, language, new Placeholder("name", info.getName()),
                                new Placeholder("description", translate(language, info, DescriptionType.SIMPLE)));

                    }

                }));

        help.optionally(Argument.of("command", StringArgumentType.word()).executes((sender, context) -> {

            String command = context.getOptionalArgument("command", String.class, "");

            CommandInfo info = INFO_PROVIDER.getInfo(command.toLowerCase());
            if (info == null) {
                Message.COMMAND_NOT_EXIST.send(sender, new Placeholder("name", command));
                return;
            }

            Language language = LanguageProvider.getLanguageOf(sender);

            Message.COMMAND_HELP_INFO_DETAILED
                .send(sender, language, new Placeholder("name", info.getName()),
                    new Placeholder("description", translate(language, info, DescriptionType.DETAILED)));

        }));

        //
        // Register help command

        CommandNode<CommandSender> helpNode = dispatcher.register(help);
        oraxenNode.redirect(helpNode);

        //
        // Register oraxen command and push result

        return new CommandNode[] { helpNode, dispatcher.register(oraxenNode) };

    }

    /*
     * 
     */

}
