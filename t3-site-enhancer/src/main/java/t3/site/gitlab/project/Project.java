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
package t3.site.gitlab.project;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "description",
    "default_branch",
    "tag_list",
    "public",
    "archived",
    "visibility_level",
    "ssh_url_to_repo",
    "http_url_to_repo",
    "web_url",
    "name",
    "name_with_namespace",
    "path",
    "path_with_namespace",
    "container_registry_enabled",
    "issues_enabled",
    "merge_requests_enabled",
    "wiki_enabled",
    "builds_enabled",
    "snippets_enabled",
    "created_at",
    "last_activity_at",
    "shared_runners_enabled",
    "lfs_enabled",
    "creator_id",
    "namespace",
    "avatar_url",
    "star_count",
    "forks_count",
    "open_issues_count",
    "public_builds",
    "shared_with_groups",
    "only_allow_merge_if_build_succeeds",
    "request_access_enabled",
    "only_allow_merge_if_all_discussions_are_resolved",
    "permissions"
})
public class Project {

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
    @JsonProperty("description")
    private String description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("default_branch")
    private String defaultBranch;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tag_list")
    private List<Object> tagList = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("public")
    private Boolean _public;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("archived")
    private Boolean archived;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("visibility_level")
    private Integer visibilityLevel;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ssh_url_to_repo")
    private String sshUrlToRepo;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("http_url_to_repo")
    private String httpUrlToRepo;
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
    @JsonProperty("name")
    private String name;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name_with_namespace")
    private String nameWithNamespace;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path_with_namespace")
    private String pathWithNamespace;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("container_registry_enabled")
    private Boolean containerRegistryEnabled;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("issues_enabled")
    private Boolean issuesEnabled;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_requests_enabled")
    private Boolean mergeRequestsEnabled;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("wiki_enabled")
    private Boolean wikiEnabled;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("builds_enabled")
    private Boolean buildsEnabled;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("snippets_enabled")
    private Boolean snippetsEnabled;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("created_at")
    private String createdAt;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("last_activity_at")
    private String lastActivityAt;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("shared_runners_enabled")
    private Boolean sharedRunnersEnabled;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lfs_enabled")
    private Boolean lfsEnabled;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("creator_id")
    private Integer creatorId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("namespace")
    private Namespace namespace;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("avatar_url")
    private Object avatarUrl;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("star_count")
    private Integer starCount;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("forks_count")
    private Integer forksCount;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("open_issues_count")
    private Integer openIssuesCount;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("public_builds")
    private Boolean publicBuilds;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("shared_with_groups")
    private List<Object> sharedWithGroups = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("only_allow_merge_if_build_succeeds")
    private Boolean onlyAllowMergeIfBuildSucceeds;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("request_access_enabled")
    private Boolean requestAccessEnabled;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("only_allow_merge_if_all_discussions_are_resolved")
    private Boolean onlyAllowMergeIfAllDiscussionsAreResolved;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("permissions")
    private Permissions permissions;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
    @JsonProperty("default_branch")
    public String getDefaultBranch() {
        return defaultBranch;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("default_branch")
    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tag_list")
    public List<Object> getTagList() {
        return tagList;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tag_list")
    public void setTagList(List<Object> tagList) {
        this.tagList = tagList;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("public")
    public Boolean getPublic() {
        return _public;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("public")
    public void setPublic(Boolean _public) {
        this._public = _public;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("archived")
    public Boolean getArchived() {
        return archived;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("archived")
    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("visibility_level")
    public Integer getVisibilityLevel() {
        return visibilityLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("visibility_level")
    public void setVisibilityLevel(Integer visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ssh_url_to_repo")
    public String getSshUrlToRepo() {
        return sshUrlToRepo;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ssh_url_to_repo")
    public void setSshUrlToRepo(String sshUrlToRepo) {
        this.sshUrlToRepo = sshUrlToRepo;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("http_url_to_repo")
    public String getHttpUrlToRepo() {
        return httpUrlToRepo;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("http_url_to_repo")
    public void setHttpUrlToRepo(String httpUrlToRepo) {
        this.httpUrlToRepo = httpUrlToRepo;
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
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name_with_namespace")
    public String getNameWithNamespace() {
        return nameWithNamespace;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name_with_namespace")
    public void setNameWithNamespace(String nameWithNamespace) {
        this.nameWithNamespace = nameWithNamespace;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path_with_namespace")
    public String getPathWithNamespace() {
        return pathWithNamespace;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path_with_namespace")
    public void setPathWithNamespace(String pathWithNamespace) {
        this.pathWithNamespace = pathWithNamespace;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("container_registry_enabled")
    public Boolean getContainerRegistryEnabled() {
        return containerRegistryEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("container_registry_enabled")
    public void setContainerRegistryEnabled(Boolean containerRegistryEnabled) {
        this.containerRegistryEnabled = containerRegistryEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("issues_enabled")
    public Boolean getIssuesEnabled() {
        return issuesEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("issues_enabled")
    public void setIssuesEnabled(Boolean issuesEnabled) {
        this.issuesEnabled = issuesEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_requests_enabled")
    public Boolean getMergeRequestsEnabled() {
        return mergeRequestsEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("merge_requests_enabled")
    public void setMergeRequestsEnabled(Boolean mergeRequestsEnabled) {
        this.mergeRequestsEnabled = mergeRequestsEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("wiki_enabled")
    public Boolean getWikiEnabled() {
        return wikiEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("wiki_enabled")
    public void setWikiEnabled(Boolean wikiEnabled) {
        this.wikiEnabled = wikiEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("builds_enabled")
    public Boolean getBuildsEnabled() {
        return buildsEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("builds_enabled")
    public void setBuildsEnabled(Boolean buildsEnabled) {
        this.buildsEnabled = buildsEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("snippets_enabled")
    public Boolean getSnippetsEnabled() {
        return snippetsEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("snippets_enabled")
    public void setSnippetsEnabled(Boolean snippetsEnabled) {
        this.snippetsEnabled = snippetsEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("last_activity_at")
    public String getLastActivityAt() {
        return lastActivityAt;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("last_activity_at")
    public void setLastActivityAt(String lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("shared_runners_enabled")
    public Boolean getSharedRunnersEnabled() {
        return sharedRunnersEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("shared_runners_enabled")
    public void setSharedRunnersEnabled(Boolean sharedRunnersEnabled) {
        this.sharedRunnersEnabled = sharedRunnersEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lfs_enabled")
    public Boolean getLfsEnabled() {
        return lfsEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lfs_enabled")
    public void setLfsEnabled(Boolean lfsEnabled) {
        this.lfsEnabled = lfsEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("creator_id")
    public Integer getCreatorId() {
        return creatorId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("creator_id")
    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("namespace")
    public Namespace getNamespace() {
        return namespace;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("namespace")
    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("avatar_url")
    public Object getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("avatar_url")
    public void setAvatarUrl(Object avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("star_count")
    public Integer getStarCount() {
        return starCount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("star_count")
    public void setStarCount(Integer starCount) {
        this.starCount = starCount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("forks_count")
    public Integer getForksCount() {
        return forksCount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("forks_count")
    public void setForksCount(Integer forksCount) {
        this.forksCount = forksCount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("open_issues_count")
    public Integer getOpenIssuesCount() {
        return openIssuesCount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("open_issues_count")
    public void setOpenIssuesCount(Integer openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("public_builds")
    public Boolean getPublicBuilds() {
        return publicBuilds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("public_builds")
    public void setPublicBuilds(Boolean publicBuilds) {
        this.publicBuilds = publicBuilds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("shared_with_groups")
    public List<Object> getSharedWithGroups() {
        return sharedWithGroups;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("shared_with_groups")
    public void setSharedWithGroups(List<Object> sharedWithGroups) {
        this.sharedWithGroups = sharedWithGroups;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("only_allow_merge_if_build_succeeds")
    public Boolean getOnlyAllowMergeIfBuildSucceeds() {
        return onlyAllowMergeIfBuildSucceeds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("only_allow_merge_if_build_succeeds")
    public void setOnlyAllowMergeIfBuildSucceeds(Boolean onlyAllowMergeIfBuildSucceeds) {
        this.onlyAllowMergeIfBuildSucceeds = onlyAllowMergeIfBuildSucceeds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("request_access_enabled")
    public Boolean getRequestAccessEnabled() {
        return requestAccessEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("request_access_enabled")
    public void setRequestAccessEnabled(Boolean requestAccessEnabled) {
        this.requestAccessEnabled = requestAccessEnabled;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("only_allow_merge_if_all_discussions_are_resolved")
    public Boolean getOnlyAllowMergeIfAllDiscussionsAreResolved() {
        return onlyAllowMergeIfAllDiscussionsAreResolved;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("only_allow_merge_if_all_discussions_are_resolved")
    public void setOnlyAllowMergeIfAllDiscussionsAreResolved(Boolean onlyAllowMergeIfAllDiscussionsAreResolved) {
        this.onlyAllowMergeIfAllDiscussionsAreResolved = onlyAllowMergeIfAllDiscussionsAreResolved;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("permissions")
    public Permissions getPermissions() {
        return permissions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("permissions")
    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
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
