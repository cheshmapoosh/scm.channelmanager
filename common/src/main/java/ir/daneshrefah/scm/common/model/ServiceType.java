package ir.daneshrefah.scm.common.model;

public enum ServiceType {
    TYPICAL(0), FINANCIAL(1);

    private int code;

    ServiceType(int code) {
        this.code = code;
    }

    public static ServiceType findByCode(int code) {
        for (ServiceType serviceType : ServiceType.values()) {
            if (serviceType.code == code) {
                return serviceType;
            }
        }
        return null;
    }
}
