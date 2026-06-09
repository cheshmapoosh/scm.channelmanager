package ir.daneshrefah.scm.uaa.utils;

import java.io.Serializable;

public record CachedAccessToken<T>(
        T token,
        long expiresAtEpochMillis
) implements Serializable {
    public boolean isValid(long nowEpochMillis, long refreshSkewMillis) {
        if(token instanceof String tk){
            return !tk.isBlank() &&
                    nowEpochMillis < (expiresAtEpochMillis - refreshSkewMillis);
        }

        return  token != null &&  nowEpochMillis < (expiresAtEpochMillis - refreshSkewMillis);
    }


    public boolean isExpired(long nowEpochMillis) {
        if(token == null)
            return  true;

        if(token instanceof String tk){
            return tk.isBlank() ||
                    nowEpochMillis < expiresAtEpochMillis;
        }

        return   nowEpochMillis > expiresAtEpochMillis;
    }



}