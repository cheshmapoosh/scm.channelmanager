package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class MapCacheExistsException extends AbstractCacheException {
    private final String mapName;

    public MapCacheExistsException(String source, String mapName) {
        super(source, "map cache exists with this name = " + mapName);
        this.mapName = mapName;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("mapName", mapName)
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
