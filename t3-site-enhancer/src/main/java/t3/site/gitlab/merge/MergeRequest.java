/**
 * (C) Copyright 2016-2018 teecube
 * (https://teecu.be) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package t3.site.gitlab.merge;

import com.fasterxml.jackson.annotation.*;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "upvotes",
    "iid",
    "description",
    "created_at",
    "title",
    "source_branch",
    "subscribed",
    "updated_at",
    "project_id",
    "merge_commit_sha",
    "id",
    "state",
    "work_in_progress",
    "author",
    "target_branch",
    "source_project_id",
    "downvotes",
    "should_remove_source_branch",
    "sha",
    "labels",
    "milestone",
    "web_url",
    "merge_status",
    "merge_when_build_succeeds",
    "user_notes_count",
    "assignee",
    "target_project_id",
    "force_remove_source_branch"
})
public class MergeRequest {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("upvotes")
    private Integer upvotes;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("iid")
    private Integer iid;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("description")
    private String description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("created_at")
    private DateTime createdAt;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source_branch")
    private String sourceBranch;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subscribed")
    private Boolean subscribed;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("updated_at")
    private String updatedAt;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("project_id")
    private Integer projectId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_commit_sha")
    private String mergeCommitSha;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private Integer id;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private String state;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("work_in_progress")
    private Boolean workInProgress;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("author")
    private Author author;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("target_branch")
    private String targetBranch;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source_project_id")
    private Integer sourceProjectId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("downvotes")
    private Integer downvotes;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("should_remove_source_branch")
    private Object shouldRemoveSourceBranch;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sha")
    private String sha;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("labels")
    private List<Object> labels = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("milestone")
    private Object milestone;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("web_url")
    private String webUrl;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_status")
    private String mergeStatus;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_when_build_succeeds")
    private Boolean mergeWhenBuildSucceeds;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("user_notes_count")
    private Integer userNotesCount;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("assignee")
    private Assignee assignee;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("target_project_id")
    private Integer targetProjectId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("force_remove_source_branch")
    private Boolean forceRemoveSourceBranch;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("upvotes")
    public Integer getUpvotes() {
        return upvotes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("upvotes")
    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("iid")
    public Integer getIid() {
        return iid;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("iid")
    public void setIid(Integer iid) {
        this.iid = iid;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("created_at")
    public DateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("created_at")
    public void setCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source_branch")
    public String getSourceBranch() {
        return sourceBranch;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source_branch")
    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subscribed")
    public Boolean getSubscribed() {
        return subscribed;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subscribed")
    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("project_id")
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("project_id")
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_commit_sha")
    public String getMergeCommitSha() {
        return mergeCommitSha;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_commit_sha")
    public void setMergeCommitSha(String mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("work_in_progress")
    public Boolean getWorkInProgress() {
        return workInProgress;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("work_in_progress")
    public void setWorkInProgress(Boolean workInProgress) {
        this.workInProgress = workInProgress;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("author")
    public Author getAuthor() {
        return author;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("author")
    public void setAuthor(Author author) {
        this.author = author;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("target_branch")
    public String getTargetBranch() {
        return targetBranch;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("target_branch")
    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source_project_id")
    public Integer getSourceProjectId() {
        return sourceProjectId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source_project_id")
    public void setSourceProjectId(Integer sourceProjectId) {
        this.sourceProjectId = sourceProjectId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("downvotes")
    public Integer getDownvotes() {
        return downvotes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("downvotes")
    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("should_remove_source_branch")
    public Object getShouldRemoveSourceBranch() {
        return shouldRemoveSourceBranch;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("should_remove_source_branch")
    public void setShouldRemoveSourceBranch(Object shouldRemoveSourceBranch) {
        this.shouldRemoveSourceBranch = shouldRemoveSourceBranch;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sha")
    public String getSha() {
        return sha;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sha")
    public void setSha(String sha) {
        this.sha = sha;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("labels")
    public List<Object> getLabels() {
        return labels;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("labels")
    public void setLabels(List<Object> labels) {
        this.labels = labels;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("milestone")
    public Object getMilestone() {
        return milestone;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("milestone")
    public void setMilestone(Object milestone) {
        this.milestone = milestone;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("web_url")
    public String getWebUrl() {
        return webUrl;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("web_url")
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_status")
    public String getMergeStatus() {
        return mergeStatus;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_status")
    public void setMergeStatus(String mergeStatus) {
        this.mergeStatus = mergeStatus;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_when_build_succeeds")
    public Boolean getMergeWhenBuildSucceeds() {
        return mergeWhenBuildSucceeds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_when_build_succeeds")
    public void setMergeWhenBuildSucceeds(Boolean mergeWhenBuildSucceeds) {
        this.mergeWhenBuildSucceeds = mergeWhenBuildSucceeds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("user_notes_count")
    public Integer getUserNotesCount() {
        return userNotesCount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("user_notes_count")
    public void setUserNotesCount(Integer userNotesCount) {
        this.userNotesCount = userNotesCount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("assignee")
    public Assignee getAssignee() {
        return assignee;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("assignee")
    public void setAssignee(Assignee assignee) {
        this.assignee = assignee;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("target_project_id")
    public Integer getTargetProjectId() {
        return targetProjectId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("target_project_id")
    public void setTargetProjectId(Integer targetProjectId) {
        this.targetProjectId = targetProjectId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("force_remove_source_branch")
    public Boolean getForceRemoveSourceBranch() {
        return forceRemoveSourceBranch;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("force_remove_source_branch")
    public void setForceRemoveSourceBranch(Boolean forceRemoveSourceBranch) {
        this.forceRemoveSourceBranch = forceRemoveSourceBranch;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
