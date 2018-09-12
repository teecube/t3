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
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "project_access",
    "group_access"
})
public class Permissions {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("project_access")
    private Object projectAccess;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("group_access")
    private GroupAccess groupAccess;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("project_access")
    public Object getProjectAccess() {
        return projectAccess;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("project_access")
    public void setProjectAccess(Object projectAccess) {
        this.projectAccess = projectAccess;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("group_access")
    public GroupAccess getGroupAccess() {
        return groupAccess;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("group_access")
    public void setGroupAccess(GroupAccess groupAccess) {
        this.groupAccess = groupAccess;
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
