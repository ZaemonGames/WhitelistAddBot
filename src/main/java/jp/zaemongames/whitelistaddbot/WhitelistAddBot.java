package jp.zaemongames.whitelistaddbot;

import net.dv8tion.jda.api.JDA;
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
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class WhitelistAddBot extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        JDABuilder builder = JDABuilder.createDefault(getConfig().getString("token"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.competing("ホワイトリスト"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);
        JDA jda = builder.build();
        jda.addEventListener(new Listener());

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }


    @EventHandler
    public void onPlayer(AsyncPlayerPreLoginEvent e) {
        if (!Bukkit.getOfflinePlayer(e.getUniqueId()).isWhitelisted()) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, "Discordサーバー内 " + ChatColor.GOLD + "#ざえもん鯖" + ChatColor.WHITE + " にて " + ChatColor.AQUA + "/whitelist" + ChatColor.WHITE + " コマンドでホワイトリストに登録してください。");
        }
    }

    private static class Listener extends ListenerAdapter {
        @Override
        public void onGuildReady(@NotNull GuildReadyEvent event) {
            if (event.getGuild().getId().equals("1087726461315731510")) {
                List<CommandData> commandData = new ArrayList<>();
                commandData.add(Commands.slash("whitelist", "ホワイトリストにプレイヤーを追加します"));
                event.getGuild().updateCommands().addCommands(commandData).queue();
            }
        }

        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
            super.onSlashCommandInteraction(event);

            if (event.getChannel().getId().equals("1088773480754982932")) {
                if (event.getName().equals("whitelist")) {
                    TextInput playerNameInput = TextInput.create("playerName", "プレイヤー名", TextInputStyle.SHORT).setRequired(true).setMinLength(1).setValue(event.getUser().getName()).build();

                    Modal modal = Modal.create("whitelistModal", "プレイヤーを追加する").setTitle("プレイヤーを追加する").addActionRow(playerNameInput).build();

                    event.replyModal(modal).queue();
                }
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onModalInteraction(@NotNull ModalInteractionEvent event) {
            super.onModalInteraction(event);

            if (event.getModalId().equals("whitelistModal")) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(event.getInteraction().getValue("playerName").getAsString());
                if (player == null) {
                    event.reply("プレイヤーIDが異なるため、正常に登録ができませんでした。").setEphemeral(true).queue();
                } else {
                    if (player.isWhitelisted()) {
                        event.reply("そのプレイヤーはすでに登録されています").setEphemeral(true).queue();
                    } else {
                        player.setWhitelisted(true);
                        event.reply("登録しました。").setEphemeral(true).queue();
                    }
                }
            }
        }
    }
}
