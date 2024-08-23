package io.github.townyadvanced.flagwar.newconfig;

import io.github.townyadvanced.flagwar.FlagWar;

import java.util.List;

public class Messages extends AbstractConfig {

    @Path(path = "command.noArgs")
    public static String noArgs = "&cКоманда неполная, укажите &eгород для атаки";

    @Path(path = "command.consoleSender")
    public static String consoleSender = "Эту команду можно выполнять только от имени игрока!";

    @Path(path = "command.unknownTown")
    public static String unknownTown = "&cУказанный город не существует или находится в руинах";

    @Path(path = "command.noTown")
    public static String noTown = "&cВы должны быть мэром города, чтобы объявить войну";

    @Path(path = "command.notMayor")
    public static String notMayor = "&cТолько мэр города может объявить войну";

    @Path(path = "notification.timeLeftMessage")
    public static String timeLeftMessage = "&eДо начала войны с городом &c{0} &eосталось &c{1}";

    @Path(path = "notification.attackerNotificationTitle")
    public static String attackerNotificationTitle = "&cВнимание! Вы объявили войну!";

    @Path(path = "notification.victimNotificationTitle")
    public static String victimNotificationTitle = "&cВнимание! Вам объявили войну!";

    @Path(path = "command.adminNoTown")
    public static String adminNoTown = "&cУ игрока нет города";

    @Path(path = "command.adminNotMayor")
    public static String adminNotMayor = "&cИгрок не является мэром города";

    @Path(path = "gui.hourGui.disallowedHourLore")
    public static List<String> hourGuiDisallowedHourLore = List.of("&cНельзя начать", "&cвойну в это время!");

    @Path(path = "gui.hourGui.allowedHourLore")
    public static List<String> hourGuiAllowedHourLore = List.of("&aНажмите, чтобы выбрать", "&aэтот час для начала войны!");

    @Path(path = "gui.dayGui.disallowedDayLore")
    public static List<String> dayGuiDisallowedDayLore = List.of("&cНельзя начать", "&cвойну в этот день!");

    @Path(path = "gui.dayGui.today")
    public static String dayGuiToday = "§r§fСегодня";

    @Path(path = "gui.dayGui.allowedDayLore")
    public static List<String> dayGuiAllowedDayLore = List.of("&aНажмите, чтобы выбрать", "&aэтот день для начала войны!");

    @Path(path = "war.tooManyActiveFlags")
    public static String tooManyActiveFlags = "&cВы превысили лимит одновременных захватов чанков";

    @Path(path = "war.warStartedTitle")
    public static String warStartedTitle = "&cВойна началась!";

    @Path(path = "war.warStartedSubTitleAttackers")
    public static String warStartedSubTitleAttackers = "Вы будете телепортированы на поле битвы";

    @Path(path = "war.warStartedSubTitleDefenders")
    public static String warStartedSubTitleDefenders = "Защитите свой город от нападения";

    @Path(path = "war.respawnTitle")
    public static String respawnTitle = "Вы возродились!";

    @Path(path = "war.respawnSubTitle")
    public static String respawnSubTitle = "Используйте команду /returnwar, чтобы вернуться на поле боя";

    @Path(path = "war.reminderTitle")
    public static String reminderTitle = "Напоминание!";

    @Path(path = "war.reminderSubTitle")
    public static String reminderSubTitle = "Вы можете вернуться на поле боя с помощью команды /returnwar";

    @Path(path = "war.autoTeleportMessage")
    public static String autoTeleportMessage = "Вы автоматически вернулись на поле боя!";

    @Path(path = "war.returnBattleTitle")
    public static String returnBattleTitle = "Телепортация";

    @Path(path = "war.returnBattleSubTitle")
    public static String returnBattleSubTitle = "Вы были возвращены на поле боя";

    @Path(path = "war.teleportBattleMessage")
    public static String teleportBattleMessage = "Вы были телепортированы на поле боя";

    @Path(path = "war.cauldronNotificationAttacker")
    public static String cauldronNotificationAttacker = "Вы успешно захватили {0} чанков.";

    @Path(path = "war.cauldronNotificationDefender")
    public static String cauldronNotificationDefender = "Вы потеряли {0} чанков.";

    @Path(path = "war.lostMessageDefender")
    public static String lostMessageDefender = "Вы проиграли и были автоматически перемещены в жители города {0}. Предыдущий город был разорен и превращен в руины.";

    @Path(path = "war.lostMessageAttacker")
    public static String lostMessageAttacker = "Вы проиграли в этой войне. Часть ваших жителей было перемещено в плен города {0}";

    @Path(path = "war.winMessageAttacker")
    public static String winMessageAttacker = "Вы выиграли войну, жители вражеского города стали вашими подданными! Баланс вашего города увеличился на {0}";

    @Path(path = "war.winMessageDefender")
    public static String winMessageDefender = "Вы выиграли войну, часть жителей вражеского города попала в плен! Баланс вашего города увеличился на {0}";

    @Path(path = "war.joinNotificationTitle")
    public static String joinNotificationTitle = "Внимание";

    @Path(path = "war.joinNotificationSubTitle")
    public static String joinNotificationSubTitle = "В вашем городе идет бой!";

    @Path(path = "war.occupiedHomeBlockMessage")
    public static String occupiedHomeBlockMessage = "Вы захватили главный чанк вражеского города";

    @Path(path = "war.leaveBattleMessage")
    public static String leaveBattleMessage = "Игрок {0} покинул битву!";

    @Path(path = "war.looseMessageAttacker")
    public static String looseMessageAttacker = "Вы не смогли победить в войне с городом {0}";

    @Path(path = "war.townRestrictedLeave")
    public static String townRestrictedLeave = "&cВы не можете покинуть город во время войны!";

    @Path(path = "war.getTownRestrictedJoin")
    public static String getTownRestrictedJoin = "&cВы не можете вступить в город во время войны!";

    @Path(path = "prefix")
    public static String prefix = "&7[&fTownyWar&7] ";

    public Messages() {
        super(FlagWar.getFlagWar(), "messages.yml");
    }

    @Override
    public String getHeader() {
        return "";
    }
}
