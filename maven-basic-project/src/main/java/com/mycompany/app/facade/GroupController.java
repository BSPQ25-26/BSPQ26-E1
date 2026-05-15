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
import com.mycompany.app.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @file GroupController.java
 * @brief REST controller for managing user groups (RMI facade layer).
 *
 * This controller exposes the remote HTTP API endpoints for all group-related
 * operations, acting as the <b>RMI interface</b> between the client and the
 * server-side business logic. It delegates every operation to the appropriate
 * service layer and returns standardised {@link ResponseEntity} responses.
 *
 * <h2>Base URL</h2>
 * All endpoints are mapped under <code>/group</code>.
 *
 * <h2>Security</h2>
 * Every endpoint requires a valid JWT access token. The token may be supplied
 * either as a request-body field ({@code accessToken}) or as a query parameter
 * ({@code token}), depending on the specific endpoint.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Group creation, retrieval, update and deletion (CRUD).</li>
 *   <li>Group membership management (add / remove members).</li>
 *   <li>Clearing settled project expenses for a group.</li>
 * </ul>
 *
 * <h2>Error Handling</h2>
 * All methods catch {@link AuthException} (authentication / authorisation
 * failures) and propagate its HTTP status directly, and catch generic
 * {@link Exception} to return {@code 500 Internal Server Error}.
 *
 * @author BSPQ26-E1 Team
 * @version 1.1
 * @since 2026-05-01
 * @see GroupService
 * @see TransactionService
 * @see GroupInfoDTO
 */
@Tag(
    name = "Groups",
    description = "Endpoints for managing user groups, including creation, "
                + "membership modifications, updates, and deletion"
)
@RestController
@RequestMapping("/group")
public class GroupController {

    // -----------------------------------------------------------------------
    // Dependencies
    // -----------------------------------------------------------------------

    /**
     * Service layer that encapsulates all group business logic, including
     * creation, retrieval, membership management, update and deletion.
     */
    private final GroupService groupService;

    /**
     * Service layer for transaction operations. Used here specifically to
     * clear project expenses ({@code GASTO} type) associated with a group.
     */
    private final TransactionService transactionService;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs a {@code GroupController} with its required service dependencies.
     *
     * <p>Spring Boot will inject both services automatically via constructor
     * injection when the application context is initialised.</p>
     *
     * @param groupService       the service handling group CRUD and membership;
     *                           must not be {@code null}
     * @param transactionService the service handling transaction / expense
     *                           operations; must not be {@code null}
     */
    public GroupController(GroupService groupService, TransactionService transactionService) {
        this.groupService = groupService;
        this.transactionService = transactionService;
    }

    // -----------------------------------------------------------------------
    // Endpoints
    // -----------------------------------------------------------------------

    /**
     * Creates a new user group.
     *
     * <p>The authenticated user identified by the JWT {@code accessToken}
     * inside the request body is automatically added as the first member
     * (and implicit owner) of the newly created group.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>POST /group/</pre>
     *
     * <h3>Request Body</h3>
     * A JSON-serialised {@link GroupCreationDTO} with the following fields:
     * <ul>
     *   <li>{@code nombre}      – the display name for the group (required)</li>
     *   <li>{@code descripcion} – a short description of the group (optional)</li>
     *   <li>{@code accessToken} – a valid JWT identifying the creator (required)</li>
     * </ul>
     *
     * <h3>Response Body</h3>
     * On success, returns a {@link GroupInfoDTO} containing:
     * {@code id}, {@code nombre}, {@code descripcion}, {@code fechaCreacion},
     * {@code miembrosEmails}, and {@code numeroMiembros}.
     *
     * @param groupCreationDTO the DTO carrying group name, description and the
     *                         creator's JWT access token
     * @return <ul>
     *   <li>{@code 201 Created} with the created {@link GroupInfoDTO} on success</li>
     *   <li>{@code 401 Unauthorized} if the token is missing or invalid</li>
     *   <li>{@code 500 Internal Server Error} on unexpected failures</li>
     * </ul>
     * @throws AuthException propagated from {@link GroupService#createGroup} when
     *                       authentication or authorisation fails
     * @see GroupCreationDTO
     * @see GroupInfoDTO
     * @see GroupService#createGroup(GroupCreationDTO)
     */
    @Operation(
            summary = "Create a new group",
            description = "Creates a new user group based on the provided details "
                        + "and assigns the creator as the admin/owner.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Group successfully created. Returns the group details."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Invalid or missing authentication credentials."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error. An unexpected error occurred while creating the group.")
            }
    )
    @PostMapping("/")
    public ResponseEntity<?> createGroup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Data transfer object containing the necessary details to create a new group",
                required = true
            )
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

    /**
     * Retrieves detailed information about a specific group.
     *
     * <p>The caller must supply a valid JWT token as a query parameter.
     * The service verifies the token before returning any data.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>GET /group/{id}?token=&lt;jwt&gt;</pre>
     *
     * <h3>Response Body</h3>
     * A {@link GroupInfoDTO} containing the group's id, name, description,
     * creation timestamp, member e-mail list and member count.
     *
     * @param id    the unique integer identifier of the target group
     * @param token a valid JWT authorization token belonging to a member or
     *              administrator of the group
     * @return <ul>
     *   <li>{@code 200 OK} with {@link GroupInfoDTO} on success</li>
     *   <li>{@code 401 Unauthorized} if the token is invalid or expired</li>
     *   <li>{@code 404 Not Found} if no group with the given {@code id} exists</li>
     *   <li>{@code 500 Internal Server Error} on unexpected failures</li>
     * </ul>
     * @throws AuthException if the token is invalid or the group is not found
     * @see GroupInfoDTO
     * @see GroupService#getGroupInfo(Integer, String)
     */
    @Operation(
            summary = "Get group details",
            description = "Retrieves detailed information about a specific group by its unique ID. "
                        + "Requires a valid authorization token.",
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

    /**
     * Retrieves all groups that a specific user belongs to.
     *
     * <p>Returns every group in which the given user is registered as a
     * member, regardless of their role within that group.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>GET /group/user/{userId}/groups?token=&lt;jwt&gt;</pre>
     *
     * <h3>Response Body</h3>
     * A JSON array of {@link GroupInfoDTO} objects, one per group. Returns
     * an empty array if the user belongs to no groups.
     *
     * @param userId the unique integer identifier of the user whose groups
     *               are to be retrieved
     * @param token  a valid JWT authorization token; must correspond to the
     *               requesting user or an administrator
     * @return <ul>
     *   <li>{@code 200 OK} with a (possibly empty) {@code List<GroupInfoDTO>}</li>
     *   <li>{@code 401 Unauthorized} if the token is invalid or expired</li>
     *   <li>{@code 500 Internal Server Error} on unexpected failures</li>
     * </ul>
     * @throws AuthException if the token is invalid or the user is not found
     * @see GroupInfoDTO
     * @see GroupService#getUserGroups(Integer, String)
     */
    @Operation(
            summary = "Get all groups for a specific user",
            description = "Retrieves a list of all the groups that a specific user "
                        + "is currently a member of or manages.",
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

    /**
     * Adds an existing user to a group.
     *
     * <p>The target user is identified by their e-mail address
     * ({@code userEmail}) inside the request body. The operation is
     * idempotent-safe: attempting to add a user who is already a member
     * returns {@code 400 Bad Request} rather than modifying state.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>POST /group/addUser</pre>
     *
     * <h3>Request Body – {@link AddUserToGroupDTO}</h3>
     * <ul>
     *   <li>{@code groupId}     – the ID of the target group (required)</li>
     *   <li>{@code userEmail}   – e-mail of the user to add (required)</li>
     *   <li>{@code accessToken} – JWT of the requester (required)</li>
     * </ul>
     *
     * @param addUserDTO the DTO carrying the group ID, target user e-mail and
     *                   the requester's JWT access token
     * @return <ul>
     *   <li>{@code 200 OK} with a success message if the user was added</li>
     *   <li>{@code 400 Bad Request} if the user is already a member</li>
     *   <li>{@code 401 Unauthorized} if the token is invalid or lacks permission</li>
     *   <li>{@code 500 Internal Server Error} on unexpected failures</li>
     * </ul>
     * @pre  The target user must exist in the system and must not already be a
     *       member of the group.
     * @post The user is added to the group's member set and the member count
     *       ({@code numeroMiembros}) is incremented by one.
     * @throws AuthException if the requester is not authorised to add members
     * @see AddUserToGroupDTO
     * @see GroupService#addUserToGroup(AddUserToGroupDTO)
     */
    @Operation(
            summary = "Add a user to a group",
            description = "Assigns an existing user to a specific group. "
                        + "Fails if the user is already a member of the target group.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully added to the group."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The user is already a member of the group."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The requester lacks permissions or the token is invalid."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    @PostMapping("/addUser")
    public ResponseEntity<?> addUserToGroup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Data containing the group ID, user email, and authorization token",
                required = true
            )
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

    /**
     * Removes a user from a group.
     *
     * <p>The last remaining member of a group cannot be removed; at least one
     * member must remain. Use {@link #deleteGroup} to fully dismantle a group
     * once all members have left.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>POST /group/removeUser</pre>
     *
     * <h3>Request Body – {@link RemoveUserFromGroupDTO}</h3>
     * <ul>
     *   <li>{@code groupId}     – the ID of the target group (required)</li>
     *   <li>{@code userEmail}   – e-mail of the user to remove (required)</li>
     *   <li>{@code accessToken} – JWT of the requester (required)</li>
     * </ul>
     *
     * @param removeUserDTO the DTO carrying the group ID, target user e-mail
     *                      and the requester's JWT access token
     * @return <ul>
     *   <li>{@code 200 OK} with a success message if the user was removed</li>
     *   <li>{@code 400 Bad Request} if the user is not a member of the group</li>
     *   <li>{@code 401 Unauthorized} if the token is invalid or lacks permission</li>
     *   <li>{@code 500 Internal Server Error} on unexpected failures</li>
     * </ul>
     * @pre  The target user must be a current member of the group and the group
     *       must have more than one member.
     * @post The user is removed from the group's member set and the member count
     *       ({@code numeroMiembros}) is decremented by one.
     * @throws AuthException if the requester is not authorised to remove members
     * @see RemoveUserFromGroupDTO
     * @see GroupService#removeUserFromGroup(RemoveUserFromGroupDTO)
     */
    @Operation(
            summary = "Remove a user from a group",
            description = "Removes a specific user from an existing group. "
                        + "Fails if the user is not currently a member.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully removed from the group."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The user is not a member of the specified group."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The requester lacks permissions or the token is invalid."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    @PostMapping("/removeUser")
    public ResponseEntity<?> removeUserFromGroup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Data containing the group ID, user email to be removed, and authorization token",
                required = true
            )
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

    /**
     * Updates the properties of an existing group.
     *
     * <p>This is a <em>partial update</em>: only fields that are non-{@code null}
     * and non-empty in the supplied {@link UpdateGroupDTO} are applied.
     * Fields left {@code null} retain their current values.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>PUT /group/{id}</pre>
     *
     * <h3>Request Body – {@link UpdateGroupDTO}</h3>
     * <ul>
     *   <li>{@code nombre}      – new display name (optional)</li>
     *   <li>{@code descripcion} – new description (optional)</li>
     *   <li>{@code accessToken} – JWT of the requester (required)</li>
     * </ul>
     * The {@code groupId} field inside the DTO is overridden by the path
     * variable {@code id} before delegation to the service.
     *
     * <h3>Response Body</h3>
     * The updated {@link GroupInfoDTO} reflecting the persisted state.
     *
     * @param id             the unique integer identifier of the group to update
     * @param updateGroupDTO the DTO carrying the fields to update and the
     *                       requester's JWT access token
     * @return <ul>
     *   <li>{@code 200 OK} with the updated {@link GroupInfoDTO} on success</li>
     *   <li>{@code 401 Unauthorized} if the token is invalid or the caller is
     *       not allowed to modify the group</li>
     *   <li>{@code 500 Internal Server Error} on unexpected failures</li>
     * </ul>
     * @throws AuthException if the token is invalid or the group does not exist
     * @see UpdateGroupDTO
     * @see GroupInfoDTO
     * @see GroupService#updateGroup(UpdateGroupDTO)
     */
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

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Data transfer object containing the updated properties for the group",
                required = true
            )
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

    /**
     * Permanently deletes a group from the system.
     *
     * <p>This operation cascades: all membership records and associated data
     * for the group are removed. There is no soft-delete; the action cannot
     * be undone through the API.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>POST /group/delete</pre>
     *
     * <h3>Request Body – {@link DeleteGroupDTO}</h3>
     * <ul>
     *   <li>{@code groupId}     – ID of the group to delete (required)</li>
     *   <li>{@code accessToken} – JWT of the requester (required)</li>
     * </ul>
     *
     * @param deleteGroupDTO the DTO carrying the target group ID and the
     *                       requester's JWT access token
     * @return <ul>
     *   <li>{@code 200 OK} with a confirmation message on success</li>
     *   <li>{@code 401 Unauthorized} if the token is invalid or the caller is
     *       not the group owner / administrator</li>
     *   <li>{@code 500 Internal Server Error} if the deletion fails or on
     *       unexpected errors</li>
     * </ul>
     * @warning This operation is <strong>irreversible</strong>. Once deleted,
     *          the group and all its associated records are permanently removed.
     * @throws AuthException if the requester is not authorised to delete the group
     * @see DeleteGroupDTO
     * @see GroupService#deleteGroup(DeleteGroupDTO)
     */
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
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Data transfer object containing the target group ID and authorization token",
                required = true
            )
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

    /**
     * Clears all settled expenses ({@code GASTO} type) associated with a group.
     *
     * <p>This endpoint delegates to
     * {@link TransactionService#clearProjectExpenses(Integer)}, which iterates
     * over every {@code GASTO} transaction linked to the given group and deletes
     * those that have no pending debts ({@code PENDIENTE} state). Transactions
     * that still have outstanding debts are <em>skipped</em> and counted.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>POST /group/{id}/clear-expenses</pre>
     *
     * <h3>Request Body – {@link DeleteGroupDTO}</h3>
     * Used here only to carry the {@code accessToken} for authentication;
     * the {@code groupId} field in the body is ignored in favour of the
     * path variable {@code id}.
     *
     * <h3>Return Value Semantics</h3>
     * The underlying service returns:
     * <ul>
     *   <li>A non-negative integer – the number of expenses skipped because
     *       they still have pending debts (0 means all were cleared).</li>
     *   <li>{@code -1} – an internal error prevented the operation.</li>
     * </ul>
     *
     * @param groupId the unique integer identifier of the group whose expenses
     *                are to be cleared
     * @param request a {@link DeleteGroupDTO} used solely to carry the caller's
     *                JWT {@code accessToken}
     * @return <ul>
     *   <li>{@code 200 OK} with a message reporting the number of skipped
     *       expenses when the operation succeeds ({@code skipped >= 0})</li>
     *   <li>{@code 400 Bad Request} if the service returns {@code -1}, indicating
     *       the group could not be processed</li>
     *   <li>{@code 500 Internal Server Error} on unexpected exceptions</li>
     * </ul>
     * @note Expenses with at least one debt in {@code PENDIENTE} state will
     *       <strong>not</strong> be deleted and are reflected in the skipped count.
     * @see TransactionService#clearProjectExpenses(Integer)
     * @see DeleteGroupDTO
     */
    @Operation(
            summary = "Clear all expenses in a group",
            description = "Deletes all transactions of type 'GASTO' associated with a specific "
                        + "group/project. Expenses with pending debts are skipped and counted.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Group expenses successfully cleared. Response includes the count of skipped expenses."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. Expenses could not be cleared for the specified group."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    @PostMapping("/{id}/clear-expenses")
    public ResponseEntity<?> clearGroupExpenses(
            @Parameter(description = "The unique identifier of the group whose expenses will be cleared", required = true)
            @PathVariable("id") Integer groupId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "DTO used to supply the caller's authorization token",
                required = true
            )
            @RequestBody DeleteGroupDTO request
    ) {
        try {
            // TransactionService returns the number of skipped expenses, or -1 on error
            int skipped = transactionService.clearProjectExpenses(groupId);

            if (skipped >= 0) {
                return ResponseEntity.ok("Project expenses cleared successfully. Skipped " + skipped + " expenses with pending debts.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Could not clear expenses for the specified group.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error clearing expenses: " + e.getMessage());
        }
    }
}