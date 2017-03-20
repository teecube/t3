/**
 * (C) Copyright 2016-2017 teecube
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

package t3.site.gitlab.project;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "access_level",
    "notification_level"
})
public class GroupAccess {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("access_level")
    private Integer accessLevel;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("notification_level")
    private Integer notificationLevel;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("access_level")
    public Integer getAccessLevel() {
        return accessLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("access_level")
    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("notification_level")
    public Integer getNotificationLevel() {
        return notificationLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("notification_level")
    public void setNotificationLevel(Integer notificationLevel) {
        this.notificationLevel = notificationLevel;
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
