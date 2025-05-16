// CreateProjectRequest.java (DTO untuk pembuatan proyek)

package com.manajemennilai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO untuk request pembuatan proyek.
 */
public class CreateProjectRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @Size(max = 255, message = "Description can't exceed 255 characters")
    private String description;

    private List<Long> memberIds;

    public CreateProjectRequest() {
        this.memberIds = new ArrayList<>();
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }
}