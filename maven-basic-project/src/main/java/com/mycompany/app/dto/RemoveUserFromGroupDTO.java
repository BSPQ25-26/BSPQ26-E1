package com.mycompany.app.dto;

public class RemoveUserFromGroupDTO {
    private String accessToken;
    private Integer groupId;
    private String userEmail;

    public RemoveUserFromGroupDTO() {
    }

    public RemoveUserFromGroupDTO(String accessToken, Integer groupId, String userEmail) {
        this.accessToken = accessToken;
        this.groupId = groupId;
        this.userEmail = userEmail;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
