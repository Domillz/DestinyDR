package net.dungeonrealms.common.game.command;

/**
 * Created by Nick on 10/24/2015.
 */
public class CommandManager {

    public void registerCommand(BaseCommand command) {
        command.register();
    }

}
