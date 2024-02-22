package server;

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
    }

    public POPRequest(String request){
        String[] splits = request.split(" ");
        this.command = POPCommand.valueOf(splits[0]);
        args = Arrays.copyOfRange(splits, 1, splits.length);
    }
}
