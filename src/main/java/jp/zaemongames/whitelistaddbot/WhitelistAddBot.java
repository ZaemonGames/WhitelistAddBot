package jp.zaemongames.whitelistaddbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class WhitelistAddBot extends JavaPlugin {

    @Override
    public void onEnable() {

        Collection<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.MESSAGE_CONTENT);

        JDABuilder builder = JDABuilder.create(getConfig().getString("token"), intents);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.competing("ホワイトリスト"));
        builder.addEventListeners(new Listener());
        builder.build();
    }

    @Override
    public void onDisable() {
    }

    private static class Listener extends ListenerAdapter {
        @Override
        public void onGuildReady(@NotNull GuildReadyEvent event) {
            super.onGuildReady(event);

            List<CommandData> commandData = new ArrayList<>();
            commandData.add(Commands.slash("whitelist", "ホワイトリストにプレイヤーを追加します"));
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }

        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
            super.onSlashCommandInteraction(event);

            if (event.getName().equals("whitelist")) {
                TextInput playerNameInput = TextInput.create("playerNameInput", "プレイヤー名", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setMinLength(1)
                        .setValue(event.getUser().getName())
                        .build();

                Modal modal = Modal.create("whitelistModal", "プレイヤーを追加する")
                        .setTitle("プレイヤーを追加する")
                        .addActionRow(playerNameInput)
                        .build();

                event.replyModal(modal).queue();
            }
        }

        @Override
        public void onModalInteraction(@NotNull ModalInteractionEvent event) {
            super.onModalInteraction(event);

            if (event.getModalId().equals("whitelistModal")) {
                Player player = Bukkit.getPlayer(String.valueOf(event.getInteraction().getValue("playerNameInput")));
                if (player == null) {
                    event.reply("プレイヤーIDが異なるため、正常に登録ができませんでした。").queue();
                } else {
                    if (player.isWhitelisted()) {
                        event.reply("そのプレイヤーはすでに登録されています").queue();
                    } else {
                        player.setWhitelisted(true);
                    }
                }
            }
        }
    }
}
