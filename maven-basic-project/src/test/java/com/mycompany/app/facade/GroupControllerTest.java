package com.mycompany.app.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.app.dto.*;
import com.mycompany.app.exception.AuthException;
import com.mycompany.app.service.GroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupController.class)
@WithMockUser
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupService grupoService;

    // ==================== CREATE GROUP TESTS ====================

    @Test
    void createGrupo_WithValidData_ShouldReturnCreated() throws Exception {
        GroupCreationDTO request = new GroupCreationDTO("valid-token", "Test Group", "Description");
        GroupInfoDTO response = new GroupInfoDTO(
            1,
            "Test Group",
            "Description",
            LocalDateTime.now(),
            Arrays.asList("test@example.com"),
            1
        );

        when(grupoService.createGroup(any(GroupCreationDTO.class))).thenReturn(response);

        mockMvc.perform(post("/group/")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("Test Group"))
            .andExpect(jsonPath("$.descripcion").value("Description"))
            .andExpect(jsonPath("$.numeroMiembros").value(1));
    }

    @Test
    void createGrupo_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        GroupCreationDTO request = new GroupCreationDTO("invalid-token", "Test Group", "Description");
        when(grupoService.createGroup(any(GroupCreationDTO.class)))
            .thenThrow(new AuthException("Invalid token", HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/group/")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("Invalid token"));
    }

    // ==================== UPDATE GROUP TESTS ====================

    @Test
    void updateGroup_WithValidData_ShouldReturnOk() throws Exception {
        UpdateGroupDTO request = new UpdateGroupDTO("valid-token", 1, "Updated Name", "Updated Description");
        GroupInfoDTO response = new GroupInfoDTO(
            1,
            "Updated Name",
            "Updated Description",
            LocalDateTime.now(),
            Arrays.asList("test@example.com"),
            1
        );

        when(grupoService.updateGroup(any(UpdateGroupDTO.class))).thenReturn(response);

        mockMvc.perform(put("/group/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("Updated Name"))
            .andExpect(jsonPath("$.descripcion").value("Updated Description"));
    }

    @Test
    void updateGroup_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        UpdateGroupDTO request = new UpdateGroupDTO("invalid-token", 1, "Updated Name", "Updated Description");
        when(grupoService.updateGroup(any(UpdateGroupDTO.class)))
            .thenThrow(new AuthException("Invalid token", HttpStatus.UNAUTHORIZED));

        mockMvc.perform(put("/group/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("Invalid token"));
    }

    @Test
    void updateGroup_WithNonExistentGroup_ShouldReturnNotFound() throws Exception {
        UpdateGroupDTO request = new UpdateGroupDTO("valid-token", 999, "Updated Name", "Updated Description");
        when(grupoService.updateGroup(any(UpdateGroupDTO.class)))
            .thenThrow(new AuthException("Group not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/group/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Group not found"));
    }

    @Test
    void updateGroup_WhenRequestorNotMember_ShouldReturnForbidden() throws Exception {
        UpdateGroupDTO request = new UpdateGroupDTO("valid-token", 1, "Updated Name", "Updated Description");
        when(grupoService.updateGroup(any(UpdateGroupDTO.class)))
            .thenThrow(new AuthException("Only group members can update the group", HttpStatus.FORBIDDEN));

        mockMvc.perform(put("/group/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(content().string("Only group members can update the group"));
    }

    // ==================== DELETE GROUP TESTS ====================

    @Test
    void deleteGroup_WithValidData_ShouldReturnOk() throws Exception {
        DeleteGroupDTO request = new DeleteGroupDTO("valid-token", 1);
        when(grupoService.deleteGroup(any(DeleteGroupDTO.class))).thenReturn(true);

        mockMvc.perform(delete("/group/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string("Group deleted successfully"));
    }

    @Test
    void deleteGroup_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        DeleteGroupDTO request = new DeleteGroupDTO("invalid-token", 1);
        when(grupoService.deleteGroup(any(DeleteGroupDTO.class)))
            .thenThrow(new AuthException("Invalid token", HttpStatus.UNAUTHORIZED));

        mockMvc.perform(delete("/group/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("Invalid token"));
    }

    @Test
    void deleteGroup_WithNonExistentGroup_ShouldReturnNotFound() throws Exception {
        DeleteGroupDTO request = new DeleteGroupDTO("valid-token", 999);
        when(grupoService.deleteGroup(any(DeleteGroupDTO.class)))
            .thenThrow(new AuthException("Group not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(delete("/group/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Group not found"));
    }

    @Test
    void deleteGroup_WhenRequestorNotMember_ShouldReturnForbidden() throws Exception {
        DeleteGroupDTO request = new DeleteGroupDTO("valid-token", 1);
        when(grupoService.deleteGroup(any(DeleteGroupDTO.class)))
            .thenThrow(new AuthException("Only group members can delete the group", HttpStatus.FORBIDDEN));

        mockMvc.perform(delete("/group/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(content().string("Only group members can delete the group"));
    }

    // ==================== GET GROUP TESTS ====================

    @Test
    void getGrupo_WithValidIdAndToken_ShouldReturnGroup() throws Exception {
        GroupInfoDTO response = new GroupInfoDTO(
            1,
            "Test Group",
            "Description",
            LocalDateTime.now(),
            Arrays.asList("test@example.com"),
            1
        );

        when(grupoService.getGroupInfo(anyInt(), anyString())).thenReturn(response);

        mockMvc.perform(get("/group/1")
                .param("token", "valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("Test Group"))
            .andExpect(jsonPath("$.numeroMiembros").value(1));
    }

    @Test
    void getGrupo_WithNonExistentGroup_ShouldReturnNotFound() throws Exception {
        when(grupoService.getGroupInfo(anyInt(), anyString()))
            .thenThrow(new AuthException("Group not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/group/999")
                .param("token", "valid-token"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Group not found"));
    }

    // ==================== GET USER GROUPS TESTS ====================

    @Test
    void getUserGroups_WithValidToken_ShouldReturnGroups() throws Exception {
        List<GroupInfoDTO> groups = Arrays.asList(
            new GroupInfoDTO(1, "Group 1", "Desc 1", LocalDateTime.now(), Arrays.asList("test@example.com"), 1),
            new GroupInfoDTO(2, "Group 2", "Desc 2", LocalDateTime.now(), Arrays.asList("test@example.com"), 1)
        );

        when(grupoService.getUserGroups(anyString())).thenReturn(groups);

        mockMvc.perform(get("/group/user/groups")
                .param("token", "valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].nombre").value("Group 1"))
            .andExpect(jsonPath("$[1].nombre").value("Group 2"));
    }

    // ==================== ADD USER TO GROUP TESTS ====================

    @Test
    void addUserToGroup_WithValidData_ShouldReturnOk() throws Exception {
        AddUserToGroupDTO request = new AddUserToGroupDTO("valid-token", 1, "newuser@example.com");
        when(grupoService.addUserToGroup(any(AddUserToGroupDTO.class))).thenReturn(true);

        mockMvc.perform(post("/group/addUser")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string("User added to group successfully"));
    }

    // ==================== REMOVE USER FROM GROUP TESTS ====================

    @Test
    void removeUserFromGroup_WithValidData_ShouldReturnOk() throws Exception {
        RemoveUserFromGroupDTO request = new RemoveUserFromGroupDTO("valid-token", 1, "user@example.com");
        when(grupoService.removeUserFromGroup(any(RemoveUserFromGroupDTO.class))).thenReturn(true);

        mockMvc.perform(post("/group/removeUser")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string("User removed from group successfully"));
    }

    @Test
    void removeUserFromGroup_LastMember_ShouldReturnBadRequest() throws Exception {
        RemoveUserFromGroupDTO request = new RemoveUserFromGroupDTO("valid-token", 1, "lastuser@example.com");
        when(grupoService.removeUserFromGroup(any(RemoveUserFromGroupDTO.class)))
            .thenThrow(new AuthException("Cannot remove the last member from a group", HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/group/removeUser")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Cannot remove the last member from a group"));
    }
}
