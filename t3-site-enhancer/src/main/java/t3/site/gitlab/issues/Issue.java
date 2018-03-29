/**
 * (C) Copyright 2016-2018 teecube
 * (http://teecu.be) and others.
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
package t3.site.gitlab.issues;

import com.fasterxml.jackson.annotation.*;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "assignee",
    "author",
    "confidential",
    "created_at",
    "description",
    "downvotes",
    "due_date",
    "id",
    "iid",
    "labels",
    "milestone",
    "project_id",
    "state",
    "subscribed",
    "title",
    "updated_at",
    "upvotes",
    "user_notes_count",
    "web_url"
})
public class Issue {

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
    @JsonProperty("author")
    private Author author;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("confidential")
    private Boolean confidential;
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
    @JsonProperty("description")
    private String description;
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
    @JsonProperty("due_date")
    private Object dueDate;
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
    @JsonProperty("iid")
    private Integer iid;
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
    private Milestone milestone;
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
    @JsonProperty("state")
    private String state;
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
    @JsonProperty("title")
    private String title;
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
    @JsonProperty("upvotes")
    private Integer upvotes;
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
    @JsonProperty("web_url")
    private String webUrl;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
    @JsonProperty("confidential")
    public Boolean getConfidential() {
        return confidential;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("confidential")
    public void setConfidential(Boolean confidential) {
        this.confidential = confidential;
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
    @JsonProperty("due_date")
    public Object getDueDate() {
        return dueDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("due_date")
    public void setDueDate(Object dueDate) {
        this.dueDate = dueDate;
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
    public Milestone getMilestone() {
        return milestone;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("milestone")
    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
