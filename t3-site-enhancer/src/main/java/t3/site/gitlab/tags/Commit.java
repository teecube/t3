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
package t3.site.gitlab.tags;

import com.fasterxml.jackson.annotation.*;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "author_name",
    "author_email",
    "authored_date",
    "committed_date",
    "committer_name",
    "committer_email",
    "id",
    "message",
    "parents_ids"
})
public class Commit {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("author_name")
    private String authorName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("author_email")
    private String authorEmail;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("authored_date")
    private String authoredDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("committed_date")
    private DateTime committedDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("committer_name")
    private String committerName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("committer_email")
    private String committerEmail;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    private String message;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("parents_ids")
    private List<String> parentsIds = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("author_name")
    public String getAuthorName() {
        return authorName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("author_name")
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("author_email")
    public String getAuthorEmail() {
        return authorEmail;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("author_email")
    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("authored_date")
    public String getAuthoredDate() {
        return authoredDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("authored_date")
    public void setAuthoredDate(String authoredDate) {
        this.authoredDate = authoredDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("committed_date")
    public DateTime getCommittedDate() {
        return committedDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("committed_date")
    public void setCommittedDate(DateTime committedDate) {
        this.committedDate = committedDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("committer_name")
    public String getCommitterName() {
        return committerName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("committer_name")
    public void setCommitterName(String committerName) {
        this.committerName = committerName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("committer_email")
    public String getCommitterEmail() {
        return committerEmail;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("committer_email")
    public void setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("parents_ids")
    public List<String> getParentsIds() {
        return parentsIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("parents_ids")
    public void setParentsIds(List<String> parentsIds) {
        this.parentsIds = parentsIds;
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
