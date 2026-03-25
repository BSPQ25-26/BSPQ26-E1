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

@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/")
    public ResponseEntity<?> createGroup(@RequestBody GroupCreationDTO groupCreationDTO) {
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(@PathVariable Integer id, @RequestParam String token) {
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

    @GetMapping("/user/{userId}/groups")
    public ResponseEntity<?> getUserGroups(@PathVariable Integer userId, @RequestParam String token) {
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

    @PostMapping("/addUser")
    public ResponseEntity<?> addUserToGroup(@RequestBody AddUserToGroupDTO addUserDTO) {
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

    @PostMapping("/removeUser")
    public ResponseEntity<?> removeUserFromGroup(@RequestBody RemoveUserFromGroupDTO removeUserDTO) {
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Integer id, @RequestBody UpdateGroupDTO updateGroupDTO) {
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

    @PostMapping("/delete")
    public ResponseEntity<?> deleteGroup(@RequestBody DeleteGroupDTO deleteGroupDTO) {
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