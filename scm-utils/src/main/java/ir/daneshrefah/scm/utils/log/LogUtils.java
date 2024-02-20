package ir.daneshrefah.scm.utils.log;

import lombok.Getter;

public class LogUtils {

    @Getter
    public enum Color{
         RESET     ( "\u001B[0m"),
         BLACK     ( "\u001B[30m"),
         RED       ( "\u001B[31m"),
         GREEN     ( "\u001B[32m"),
         YELLOW    ( "\u001B[33m"),
         BLUE      ( "\u001B[34m"),
         PURPLE    ( "\u001B[35m"),
         CYAN      ( "\u001B[36m"),
         WHITE     ( "\u001B[37m");
         @Getter
         private final String code;
         Color(String code){
             this.code = code;
         }

    }

    public static String markWith(Color color,String text){
        return color.code+text+Color.RESET.getCode();
    }
}
