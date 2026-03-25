package com.mycompany.app.facade;

import com.mycompany.app.dto.AddUserToGroupDTO;
import com.mycompany.app.dto.DeleteGroupDTO;
import com.mycompany.app.dto.GroupCreationDTO;
import com.mycompany.app.dto.GroupInfoDTO;
import com.mycompany.app.dto.RemoveUserFromGroupDTO;
import com.mycompany.app.dto.UpdateGroupDTO;
import com.mycompany.app.exception.AuthException;
import com.mycompany.app.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    private GroupService grupoService;

    /**
     * Create a new group
     * POST /grupo/
     */
    @PostMapping("/")
    public ResponseEntity<?> createGrupo(@RequestBody GroupCreationDTO grupoCreationDTO) {
        try {
            GroupInfoDTO grupoInfo = grupoService.createGroup(grupoCreationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(grupoInfo);
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating group: " + e.getMessage());
        }
    }

    /**
     * Get group information by ID
     * GET /grupo/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getGrupo(@PathVariable Integer id, @RequestParam String token) {
        try {
            GroupInfoDTO grupoInfo = grupoService.getGroupInfo(id, token);
            return ResponseEntity.ok(grupoInfo);
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving group: " + e.getMessage());
        }
    }

    /**
     * Get all groups for the authenticated user
     * GET /grupo/user/groups
     */
    @GetMapping("/user/groups")
    public ResponseEntity<?> getUserGroups(@RequestParam String token) {
        try {
            List<GroupInfoDTO> grupos = grupoService.getUserGroups(token);
            return ResponseEntity.ok(grupos);
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user groups: " + e.getMessage());
        }
    }

    /**
     * Add a user to a group
     * POST /grupo/addUser
     */
    @PostMapping("/addUser")
    public ResponseEntity<?> addUserToGroup(@RequestBody AddUserToGroupDTO addUserDTO) {
        try {
            boolean added = grupoService.addUserToGroup(addUserDTO);
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

    /**
     * Remove a user from a group
     * POST /grupo/removeUser
     */
    @PostMapping("/removeUser")
    public ResponseEntity<?> removeUserFromGroup(@RequestBody RemoveUserFromGroupDTO removeUserDTO) {
        try {
            boolean removed = grupoService.removeUserFromGroup(removeUserDTO);
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

    /**
     * Update group information
     * PUT /group/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Integer id, @RequestBody UpdateGroupDTO updateGroupDTO) {
        try {
            updateGroupDTO.setGroupId(id);
            GroupInfoDTO updatedGroup = grupoService.updateGroup(updateGroupDTO);
            return ResponseEntity.ok(updatedGroup);
        } catch (AuthException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating group: " + e.getMessage());
        }
    }

    /**
     * Delete a group
     * DELETE /group/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer id, @RequestBody DeleteGroupDTO deleteGroupDTO) {
        try {
            deleteGroupDTO.setGroupId(id);
            boolean deleted = grupoService.deleteGroup(deleteGroupDTO);
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
