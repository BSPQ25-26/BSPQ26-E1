package com.mycompany.app.facade;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.dto.AddUserToGroupDTO;
import com.mycompany.app.dto.DeleteGroupDTO;
import com.mycompany.app.dto.GroupCreationDTO;
import com.mycompany.app.dto.GroupInfoDTO;
import com.mycompany.app.dto.RemoveUserFromGroupDTO;
import com.mycompany.app.dto.UpdateGroupDTO;
import com.mycompany.app.exception.AuthException;
import com.mycompany.app.service.GroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Groups", description = "Endpoints for managing user groups, including creation, membership modifications, updates, and deletion")
@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Operation(
            summary = "Create a new group",
            description = "Creates a new user group based on the provided details and assigns the creator as the admin/owner.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Group successfully created. Returns the group details."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Invalid or missing authentication credentials."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error. An unexpected error occurred while creating the group.")
            }
    )
    @PostMapping("/")
    public ResponseEntity<?> createGroup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data transfer object containing the necessary details to create a new group", required = true)
            @RequestBody GroupCreationDTO groupCreationDTO
    ) {
        try {
            GroupInfoDTO groupInfo = groupService.createGroup(groupCreationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(groupInfo);
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating group: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get group details",
            description = "Retrieves detailed information about a specific group by its unique ID. Requires a valid authorization token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Group information retrieved successfully."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or expired."),
                    @ApiResponse(responseCode = "404", description = "Not Found. The group with the specified ID does not exist."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(
            @Parameter(description = "The unique identifier of the group", required = true)
            @PathVariable Integer id,
            
            @Parameter(description = "A valid authorization token to verify user access", required = true)
            @RequestParam String token
    ) {
        try {
            GroupInfoDTO groupInfo = groupService.getGroupInfo(id, token);
            return ResponseEntity.ok(groupInfo);
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving group: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get all groups for a specific user",
            description = "Retrieves a list of all the groups that a specific user is currently a member of or manages.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User groups retrieved successfully. Returns a list of groups."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or expired."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    @GetMapping("/user/{userId}/groups")
    public ResponseEntity<?> getUserGroups(
            @Parameter(description = "The unique identifier of the user", required = true)
            @PathVariable Integer userId,
            
            @Parameter(description = "A valid authorization token to verify user access", required = true)
            @RequestParam String token
    ) {
        try {
            List<GroupInfoDTO> groups = groupService.getUserGroups(userId, token);
            return ResponseEntity.ok(groups);
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user groups: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Add a user to a group",
            description = "Assigns an existing user to a specific group. Fails if the user is already a member of the target group.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully added to the group."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The user is already a member of the group."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The requester lacks permissions or the token is invalid."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    @PostMapping("/addUser")
    public ResponseEntity<?> addUserToGroup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data containing the group ID, user ID, and authorization token", required = true)
            @RequestBody AddUserToGroupDTO addUserDTO
    ) {
        try {
            boolean added = groupService.addUserToGroup(addUserDTO);
            if (added) {
                return ResponseEntity.ok("User added to group successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User is already a member of the group");
            }
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding user to group: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Remove a user from a group",
            description = "Removes a specific user from an existing group. Fails if the user is not currently a member.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully removed from the group."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The user is not a member of the specified group."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The requester lacks permissions or the token is invalid."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    @PostMapping("/removeUser")
    public ResponseEntity<?> removeUserFromGroup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data containing the group ID, user ID to be removed, and authorization token", required = true)
            @RequestBody RemoveUserFromGroupDTO removeUserDTO
    ) {
        try {
            boolean removed = groupService.removeUserFromGroup(removeUserDTO);
            if (removed) {
                return ResponseEntity.ok("User removed from group successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User is not a member of the group");
            }
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing user from group: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Update group details",
            description = "Modifies the properties (such as name or description) of an existing group.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Group successfully updated. Returns the updated group details."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The requester lacks permissions or the token is invalid."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(
            @Parameter(description = "The unique identifier of the group to be updated", required = true)
            @PathVariable Integer id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data transfer object containing the updated properties for the group", required = true)
            @RequestBody UpdateGroupDTO updateGroupDTO
    ) {
        try {
            updateGroupDTO.setGroupId(id);
            GroupInfoDTO updatedGroup = groupService.updateGroup(updateGroupDTO);
            return ResponseEntity.ok(updatedGroup);
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating group: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Delete a group",
            description = "Permanently removes an existing group from the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Group successfully deleted."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The requester lacks permissions or the token is invalid."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error. The group could not be deleted.")
            }
    )
    @PostMapping("/delete")
    public ResponseEntity<?> deleteGroup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data transfer object containing the target group ID and authorization token", required = true)
            @RequestBody DeleteGroupDTO deleteGroupDTO
    ) {
        try {
            boolean deleted = groupService.deleteGroup(deleteGroupDTO);
            if (deleted) {
                return ResponseEntity.ok("Group deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error deleting group");
            }
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting group: " + e.getMessage());
        }
    }
}