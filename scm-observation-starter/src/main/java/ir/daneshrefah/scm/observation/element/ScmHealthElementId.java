package ir.daneshrefah.scm.observation.element;

public record ScmHealthElementId(
        String component,
        String type,
        String name
) {
    public ScmHealthElementId {
        component = required(component, "component");
        type = required(type, "type");
        name = required(name, "name");
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SCM health element " + fieldName + " must not be blank");
        }
        return value.trim();
    }
}
