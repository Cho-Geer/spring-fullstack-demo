package main.java.mysql_jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api")
@Tag(name = "Role Management", description = "API endpoints for role management operations")
public class RoleController {
    @Autowired
    private RoleService roleService;
    @Operation(summary = "Get all roles", description = "Retrieves a list of all roles in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all roles",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = RoleEntity.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/roles")
    public ResponseEntity<List<RoleEntity>> getAllRoles() {
        List<RoleEntity> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Get all roles", description = "Retrieves a list of all roles in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all roles",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = RoleEntity.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/roles/search/{roleName}")
    public ResponseEntity<List<RoleEntity>> findByRoleNameLike(
        @Parameter(description = "role_name of the role to be retrieved", required = true)
        @PathVariable String roleName) {
        List<RoleEntity> roles = roleService.findByRoleNameLike(roleName);
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Create a new role", description = "Creates a new role in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role successfully created",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = RoleEntity.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/roles")
    public ResponseEntity<RoleEntity> createRole(
            @Parameter(description = "Role object to be created", required = true)
            @RequestBody RoleEntity roleEntity) {
        RoleEntity createdRole = roleService.save(roleEntity);
        return ResponseEntity.ok(createdRole);
    }
    @Operation(summary = "Get role by ID", description = "Retrieves a role by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the role",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = RoleEntity.class))),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/roles/{roleId}")
    public ResponseEntity<RoleEntity> getRoleById(
            @Parameter(description = "ID of the role to be retrieved", required = true)
            @PathVariable Long roleId) {
        RoleEntity role = roleService.findById(roleId);
        if (role != null) {
            return ResponseEntity.ok(role);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @Operation(summary = "Update a role", description = "Updates an existing role by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role successfully updated",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = RoleEntity.class))),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/roles/{roleId}")
    public ResponseEntity<RoleEntity> updateRole(
            @Parameter(description = "ID of the role to be updated", required = true)
            @PathVariable Long roleId,
            @Parameter(description = "Updated role object", required = true)
            @RequestBody RoleEntity roleEntity) {
        RoleEntity updatedRole = roleService.update(roleId, roleEntity);
        if (updatedRole != null) {
            return ResponseEntity.ok(updatedRole);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @Operation(summary = "Delete a role", description = "Deletes a role by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "ID of the role to be deleted", required = true)
            @PathVariable Long roleId) {
        roleService.deleteById(roleId);
        return ResponseEntity.noContent().build();
    }

}
