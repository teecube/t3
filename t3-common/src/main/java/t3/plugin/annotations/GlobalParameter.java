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
package t3.plugin.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * This annotation is used to add a global parameter to one or several Mojos.
 * </p>
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD } )
@Inherited
public @interface GlobalParameter {
    String property();
    String defaultValue() default "";
    boolean required() default false;
    String description() default "";
    String category() default "";
    boolean valueGuessedByDefault() default true;
}