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
package t3.xml;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
* <p>
* Simple wrapper class for org.codehaus.plexus.util.xml.Xpp3Dom to append merged
* children elements instead of prepending them.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public class Xpp3Dom extends org.codehaus.plexus.util.xml.Xpp3Dom {

	private static final long serialVersionUID = 2749643165069298546L;

	public Xpp3Dom(String name) {
		super(name);
    }

	public Xpp3Dom(Xpp3Dom src) {
		super(src);
	}

	public Xpp3Dom(Xpp3Dom src, String name) {
		super(src, name);
	}

	/*
	 * Same private method as org.codehaus.plexus.util.xml.Xpp3Dom:mergeIntoXpp3Dom
	 * but with choice of order merge
	 */
	@SuppressWarnings("unchecked")
	private static void mergeIntoXpp3Dom(org.codehaus.plexus.util.xml.Xpp3Dom dominant, org.codehaus.plexus.util.xml.Xpp3Dom recessive, Boolean childMergeOverride, Boolean childMergeAfter) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (recessive == null) {
			return;
		}

		boolean mergeSelf = true;

		String selfMergeMode = dominant.getAttribute(SELF_COMBINATION_MODE_ATTRIBUTE);

		if (SELF_COMBINATION_OVERRIDE.equals(selfMergeMode)) {
			mergeSelf = false;
		}

		if (mergeSelf) {
			if (isEmpty(dominant.getValue())) {
				dominant.setValue(recessive.getValue());
			}

			String[] recessiveAttrs = recessive.getAttributeNames();
			for (int i = 0; i < recessiveAttrs.length; i++) {
				String attr = recessiveAttrs[i];

				if (isEmpty(dominant.getAttribute(attr))) {
					dominant.setAttribute(attr, recessive.getAttribute(attr));
				}
			}

			if (recessive.getChildCount() > 0) {
				boolean mergeChildren = true;

				if (childMergeOverride != null) {
					mergeChildren = childMergeOverride.booleanValue();
				} else {
					String childMergeMode = dominant.getAttribute(CHILDREN_COMBINATION_MODE_ATTRIBUTE);

					if (CHILDREN_COMBINATION_APPEND.equals(childMergeMode)) {
						mergeChildren = false;
					}
				}

				if (!mergeChildren) {
					org.codehaus.plexus.util.xml.Xpp3Dom[] dominantChildren = dominant.getChildren();
					// remove these now, so we can append them to the recessive
					// list later.
//					dominant.childList.clear();
					Field childList = dominant.getClass().getDeclaredField("childList");
					childList.setAccessible(true);
					List<Xpp3Dom> dominant_childList = (List<Xpp3Dom>) childList.get(dominant);
					dominant_childList.clear();

					if (childMergeAfter) {
						// now, re-add these children so they'll be appended to the
						// recessive list.
						for (int i = 0; i < dominantChildren.length; i++) {
							dominant.addChild(dominantChildren[i]);
						}
					}

					for (int i = 0, recessiveChildCount = recessive.getChildCount(); i < recessiveChildCount; i++) {
						org.codehaus.plexus.util.xml.Xpp3Dom recessiveChild = recessive.getChild(i);
						dominant.addChild(new org.codehaus.plexus.util.xml.Xpp3Dom(recessiveChild));
					}

					if (!childMergeAfter) {
						// now, re-add these children so they'll be appended to the
						// recessive list.
						for (int i = 0; i < dominantChildren.length; i++) {
							dominant.addChild(dominantChildren[i]);
						}
					}
				} else {
					Map<String, Iterator<org.codehaus.plexus.util.xml.Xpp3Dom>> commonChildren = new HashMap<String, Iterator<org.codehaus.plexus.util.xml.Xpp3Dom>>();

//					for (String childName : recessive.childMap.keySet()) {
					Field childMap = recessive.getClass().getDeclaredField("childMap");
					childMap.setAccessible(true);
					Map<String, Xpp3Dom> recessive_childMap = (Map<String, Xpp3Dom>) childMap.get(recessive);
					for (String childName : recessive_childMap.keySet()) {
						org.codehaus.plexus.util.xml.Xpp3Dom[] dominantChildren = dominant.getChildren(childName);
						if (dominantChildren.length > 0) {
							commonChildren.put(childName, Arrays.asList(dominantChildren).iterator());
						}
					}

					for (int i = 0, recessiveChildCount = recessive.getChildCount(); i < recessiveChildCount; i++) {
						org.codehaus.plexus.util.xml.Xpp3Dom recessiveChild = recessive.getChild(i);
						Iterator<org.codehaus.plexus.util.xml.Xpp3Dom> it = commonChildren.get(recessiveChild.getName());
						if (it == null) {
							dominant.addChild(new org.codehaus.plexus.util.xml.Xpp3Dom(recessiveChild));
						} else if (it.hasNext()) {
							org.codehaus.plexus.util.xml.Xpp3Dom dominantChild = it.next();
							mergeIntoXpp3Dom(dominantChild, recessiveChild, childMergeOverride, childMergeAfter);
						}
					}
				}
			}
		}
	}

	// @PseudoOverride
	public static org.codehaus.plexus.util.xml.Xpp3Dom mergeXpp3Dom(org.codehaus.plexus.util.xml.Xpp3Dom dominant, org.codehaus.plexus.util.xml.Xpp3Dom recessive, Boolean childMergeOverride) {
		if (dominant != null) {
			try {
				mergeIntoXpp3Dom(dominant, recessive, childMergeOverride, true);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				// catch ?
			}
			return dominant;
		}
		return recessive;
	}

	// @PseudoOverride
	public static org.codehaus.plexus.util.xml.Xpp3Dom mergeXpp3Dom(org.codehaus.plexus.util.xml.Xpp3Dom dominant, org.codehaus.plexus.util.xml.Xpp3Dom recessive) {
		if (dominant != null) {
			try {
				mergeIntoXpp3Dom(dominant, recessive, null, true);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				// catch ?
			}
			return dominant;
		}
		return recessive;
	}

}
