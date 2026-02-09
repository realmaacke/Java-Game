package builder.enums;

public enum GameObjectType {
    TERRAIN,
    ENVIRONMENT,
    NPC,
    UNDEFINED;

    public static GameObjectType fromString(String value) {
        if(value == null) return UNDEFINED;

        return switch (value.toUpperCase()) {
            case "TERRAIN" -> TERRAIN;
            case "NPC" -> NPC;
            case "ENVIRONMENT" -> ENVIRONMENT;
            default ->  UNDEFINED;
        };
    }
}
