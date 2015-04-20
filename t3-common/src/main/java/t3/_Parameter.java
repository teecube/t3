/**
 * (C) Copyright 2014-2015 T3Soft
 * (http://t3soft.org) and others.
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
package t3;

//import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation is a clone of the
 * {@link org.apache.maven.plugins.annotations.Parameter} annotation but with
 * a <b>RUNTIME retention policy</b>.
 * </p>
 * <p>
 * The <b>RUNTIME retention policy</b> allows to read the annotation parameters
 * at run-time and to inject properly the default values for builtin properties
 * in the Maven model.
 * </p>
 * 
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */

//@Documented
@Retention( RetentionPolicy.RUNTIME ) // RUNTIME retention policy
@Target( { ElementType.FIELD } )
@Inherited
public @interface _Parameter
{
    String alias() default "";

    String property();

    String defaultValue();

    boolean required() default false;

    boolean readonly() default false;
}
