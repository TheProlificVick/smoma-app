package smoma.controller.model.Service;

public enum AppPermission {
    READ_MISSION("mission:read"),
    CREATE_MISSION("mission:write"),
    APPROVE_MISSION("mission:approve"),
    MANAGE_ROLES("roles:manage"),
    
    // Aligned to match the explicit references inside MissionOrderController
    APPROVE_MISSION_ORDER("mission:order:approve"),
    REJECT_MISSION_ORDER("mission:order:reject");

    private final String permission;

    AppPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}