package io.github.townyadvanced.flagwar.newconfig;

import io.github.townyadvanced.flagwar.FlagWar;

import java.util.List;

public class Messages extends AbstractConfig {

    @Path(path = "command.noArgs")
    public static String noArgs = "&cНеполная команда, пожалуйста укажите &eгород атаки";

    @Path(path = "command.consoleSender")
    public static String consoleSender = "Данную команду можно выполнять только от имени игрока!";

    @Path(path = "command.unknownTown")
    public static String unknownTown = "&cГород, который вы указали, не существует или находится в состоянии руин";

    @Path(path = "command.noTown")
    public static String noTown = "&cВы должны быть мером города, что бы обьявить войну";

    @Path(path = "command.notMayor")
    public static String notMayor = "&cВы должны быть мером города, что бы обьявить войну";

    @Path(path = "notification.timeLeftMessage")
    public static String timeLeftMessage = "&eДо начала войны с &c {0} &eосталось &c {1}";

    @Path(path = "notification.attackerNotificationTitle")
    public static String attackerNotificationTitle = "&cВнимание! Ваш город объявил войну!";

    @Path(path = "notification.victimNotificationTitle")
    public static String victimNotificationTitle = "&cВнимание! Вам объявили войну!";

    @Path(path = "command.noTown")
    public static String adminNoTown = "&cИгрок не имеет города";

    @Path(path = "command.notMayor")
    public static String adminNotMayor = "&cИгрок не мэр города";

    @Path(path= "gui.hourGui.disallowedHourLore")
    public static List<String> hourGuiDisallowedHourLore = List.of("&cВы не можете начать", "&cвойну в этот час!");

    @Path(path= "gui.hourGui.allowedHourLore")
    public static List<String> hourGuiAllowedHourLore = List.of("&aНажмите, чтобы выбрать", "&aэтот час войны!");

    @Path(path= "gui.dayGui.disallowedDayLore")
    public static List<String> dayGuiDisallowedDayLore = List.of("&cВы не можете начать", "&cВойну в этот день!");

    @Path(path= "gui.dayGui.today")
    public static String dayGuiToday = "§r§f" + "Сегодня";

    @Path(path= "gui.dayGui.allowedDayLore")
    public static List<String> dayGuiAllowedDayLore = List.of("&aНажмите, чтобы выбрать", "&aдень начала войны!");

    @Path(path = "war.tooManyActiveFlags")
    public static String tooManyActiveFlags = "&cВы превысили лимит на одновременный захват чанков";

    @Path(path = "war.warStartedTitle")
    public static String warStartedTitle = "&cНачалась война";

    @Path(path = "war.warStartedSubTitleAttackers")
    public static String warStartedSubTitleAttackers = "Вы будете телепортированы на поле битвы";

    @Path(path = "war.warStartedSubTitleDefenders")
    public static String warStartedSubTitleDefenders = "Защитите свой город от противников";

    @Path(path = "war.respawnTitle")
    public static String respawnTitle = "Вы возродились!";

    @Path(path = "war.respawnSubTitle")
    public static String respawnSubTitle = "Используйте команду /returnwar, чтобы вернуться на поле боя";

    @Path(path = "war.reminderTitle")
    public static String reminderTitle = "Не забывайте!";

    @Path(path = "war.reminderSubTitle")
    public static String reminderSubTitle = "Вы можете вернуться на поле боя командой /returnwar";

    @Path(path = "war.autoTeleportMessage")
    public static String autoTeleportMessage = "Вы автоматически возвращены на поле боя!";

    @Path(path = "war.returnBattleTitle")
    public static String returnBattleTitle = "Телепортация";

    @Path(path = "war.returnBattleSubTitle")
    public static String returnBattleSubTitle = "Вы были возвращены на поле боя";

    @Path(path = "war.teleportBattleMessage")
    public static String teleportBattleMessage = "Вы были телепортированы на поле боя";

    @Path(path = "war.cauldronNotificationAttacker")
    public static String cauldronNotificationAttacker = "Вы успешно сделали котел на {0} чанков.";

    @Path(path = "war.cauldronNotificationDefender")
    public static String cauldronNotificationDefender = "Вы потеряли в котле {0} чанков.";

    @Path(path = "war.lostMessageDefender")
    public static String lostMessageDefender = "Вы проиграли и были автоматически перемещены в жители города {0}";

    @Path(path = "war.lostMessageAttacker")
    public static String lostMessageAttacker = "Вы проиграли в этой войне. Часть ваших жителей было перемещено в плен города {0}";

    @Path(path = "war.winMessageAttacker")
    public static String winMessageAttacker = "Вы выиграли в войне, жители вражеского города перешли в ваше владение! Также баланс города увеличился на {0}";

    @Path(path = "war.winMessageDefender")
    public static String winMessageDefender = "Вы выиграли в войне, часть жителей вражеского города попало в плен! Также баланс города увеличился на {0}";

    @Path(path = "war.joinNotificationTitle")
    public static String joinNotificationTitle = "Внимание";

    @Path(path = "war.joinNotificationSubTitle")
    public static String joinNotificationSubTitle = "В вашем городе идет бой!";

    @Path(path = "war.occupiedHomeBlockMessage")
    public static String occupiedHomeBlockMessage = "Вы захватили главный чанк вражеского города";

    @Path(path = "war.leaveBattleMessage")
    public static String leaveBattleMessage = "Игрок {0} покинул битву!";

    @Path(path = "war.looseMessageAttacker")
    public static String looseMessageAttacker = "Вы не смогли победить в войне с {0}";

    public Messages() {
        super(FlagWar.getFlagWar(), "messages.yml");
    }

    @Override
    public String getHeader() {
        return "";
    }
}
