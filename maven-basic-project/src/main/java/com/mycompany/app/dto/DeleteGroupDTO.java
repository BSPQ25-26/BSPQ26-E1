package com.mycompany.app.dto;

public class DeleteGroupDTO {
    private String accessToken;
    private Integer groupId;

    public DeleteGroupDTO() {
    }

    public DeleteGroupDTO(String accessToken, Integer groupId) {
        this.accessToken = accessToken;
        this.groupId = groupId;
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
}
