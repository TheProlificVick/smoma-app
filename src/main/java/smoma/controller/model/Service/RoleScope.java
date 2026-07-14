package smoma.controller.model.Service;

import java.util.Set;

public enum RoleScope {
    ADMIN(Set.of(
        AppPermission.READ_MISSION, 
        AppPermission.CREATE_MISSION, 
        AppPermission.APPROVE_MISSION, 
        AppPermission.MANAGE_ROLES,
        AppPermission.APPROVE_MISSION_ORDER,
        AppPermission.REJECT_MISSION_ORDER
    )),
    SUPERVISOR(Set.of(
        AppPermission.READ_MISSION, 
        AppPermission.APPROVE_MISSION,
        AppPermission.APPROVE_MISSION_ORDER,
        AppPermission.REJECT_MISSION_ORDER
    )),
    STAFF(Set.of(
        AppPermission.READ_MISSION, 
        AppPermission.CREATE_MISSION
    ));

    private final Set<AppPermission> permissions;

    RoleScope(Set<AppPermission> permissions) {
        this.permissions = permissions;
    }

    public Set<AppPermission> getPermissions() {
        return permissions;
    }
}