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

    public Messages() {
        super(FlagWar.getInstance(), "messages.yml");
    }

    @Override
    public String getHeader() {
        return "";
    }
}
