package com.mycompany.app.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mycompany.app.dto.AddUserToGroupDTO;
import com.mycompany.app.dto.DeleteGroupDTO;
import com.mycompany.app.dto.GroupCreationDTO;
import com.mycompany.app.dto.GroupInfoDTO;
import com.mycompany.app.dto.RemoveUserFromGroupDTO;
import com.mycompany.app.dto.UpdateGroupDTO;
import com.mycompany.app.exception.AuthException;
import com.mycompany.app.service.GroupService;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    @Test
    void createGroup_WithValidRequest_ReturnsCreated() {
        GroupCreationDTO dto = new GroupCreationDTO("token", "Trip", "desc");
        GroupInfoDTO info = new GroupInfoDTO(1, "Trip", "desc", LocalDateTime.now(), List.of("ana@mail.com"), 1);

        when(groupService.createGroup(dto)).thenReturn(info);

        ResponseEntity<?> response = groupController.createGroup(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createGroup_WhenAuthException_ReturnsMappedStatus() {
        GroupCreationDTO dto = new GroupCreationDTO("bad", "Trip", "desc");
        when(groupService.createGroup(dto)).thenThrow(new AuthException("Invalid token", HttpStatus.UNAUTHORIZED));

        ResponseEntity<?> response = groupController.createGroup(dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void getGroup_WhenServiceThrowsRuntime_ReturnsInternalServerError() {
        when(groupService.getGroupInfo(1, "ok")).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> response = groupController.getGroup(1, "ok");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void addUserToGroup_WhenAlreadyMember_ReturnsBadRequest() {
        AddUserToGroupDTO dto = new AddUserToGroupDTO("token", 1, "user@mail.com");
        when(groupService.addUserToGroup(dto)).thenReturn(false);

        ResponseEntity<?> response = groupController.addUserToGroup(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void removeUserFromGroup_WhenRemoved_ReturnsOk() {
        RemoveUserFromGroupDTO dto = new RemoveUserFromGroupDTO("token", 1, "user@mail.com");
        when(groupService.removeUserFromGroup(dto)).thenReturn(true);

        ResponseEntity<?> response = groupController.removeUserFromGroup(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateGroup_WhenAuthException_ReturnsAuthStatus() {
        UpdateGroupDTO dto = new UpdateGroupDTO("token", 1, "new", "new-desc");
        when(groupService.updateGroup(dto)).thenThrow(new AuthException("Group not found", HttpStatus.NOT_FOUND));

        ResponseEntity<?> response = groupController.updateGroup(1, dto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteGroup_WhenServiceReturnsTrue_ReturnsOk() {
        DeleteGroupDTO dto = new DeleteGroupDTO("token", 5);
        when(groupService.deleteGroup(dto)).thenReturn(true);

        ResponseEntity<?> response = groupController.deleteGroup(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getUserGroups_WithValidServiceResponse_ReturnsOk() {
        GroupInfoDTO info = new GroupInfoDTO(1, "Trip", "desc", LocalDateTime.now(), List.of("ana@mail.com"), 1);
        when(groupService.getUserGroups(2, "ok")).thenReturn(List.of(info));

        ResponseEntity<?> response = groupController.getUserGroups(2, "ok");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
