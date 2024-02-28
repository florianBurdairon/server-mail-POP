package pop;

import java.util.Arrays;

public class POPRequest {
    public final POPCommand command;

    public final String[] args;

    public enum POPCommand {
        USER,
        PASS,
        STAT,
        LIST,
        RETR,
        TOP,
        DELE,
        RSET,
        NOOP,
        QUIT,
        ERR
    }

    public POPRequest(String request){
        POPCommand command1;
        String[] splits = request.split(" ");
        try {
            command1 = POPCommand.valueOf(splits[0]);
        } catch (IllegalArgumentException e) {
            command1 = POPCommand.ERR;
        }
        this.command = command1;
        args = Arrays.copyOfRange(splits, 1, splits.length);
    }
}
