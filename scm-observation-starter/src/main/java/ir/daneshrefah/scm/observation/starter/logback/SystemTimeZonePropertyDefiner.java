package ir.daneshrefah.scm.observation.starter.logback;

import ch.qos.logback.core.PropertyDefinerBase;

import java.time.ZoneId;

public class SystemTimeZonePropertyDefiner extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        return ZoneId.systemDefault().getId();
    }
}
