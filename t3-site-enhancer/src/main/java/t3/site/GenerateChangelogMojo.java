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
package t3.site;

import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.aether.util.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

import lombok.ast.libs.com.google.common.collect.Lists;
import t3.site.gitlab.commits.Commit;
import t3.site.gitlab.issues.Issue;
import t3.site.gitlab.merge.MergeRequest;
import t3.site.gitlab.project.Project;
import t3.site.gitlab.tags.Tag;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
@Mojo(name = "generate-changelog", defaultPhase = LifecyclePhase.POST_SITE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateChangelogMojo extends AbstractNewPageMojo {

	@Parameter (property="t3.site.gitlab.apiEndPoint")
	private String apiEndPoint;

	@Parameter (property="t3.site.globalDocumentation.pageName", defaultValue="changelog")
	private String pageName;

	@Parameter (property="t3.site.gitlab.privateToken")
	private String privateToken;

	@Parameter (property="t3.site.gitlab.repository", defaultValue="${project.scm.url}")
	private String gitlabRepository;

	@Parameter
	private List<String> excludedTagsPattern;

	@Override
	public String getPageName() {
		return pageName;
	}

	private List<Project> projects;
	private ObjectMapper mapper;
	private List<String> mergeRequestsAdded = new ArrayList<String>();
	private List<Integer> issuesAdded = new ArrayList<Integer>();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (StringUtils.isEmpty(privateToken) || StringUtils.isEmpty(gitlabRepository) || StringUtils.isEmpty(apiEndPoint)) {
			return;
		}

		if (excludedTagsPattern == null) {
			excludedTagsPattern = new ArrayList<String>();
		}

		super.execute();
	}

	@Override
	public HtmlCanvas getContent(HtmlCanvas html) throws IOException, SAXException, MojoExecutionException, MojoFailureException {
		try {
			init();

			html = html.
				div(class_("row")).
					div(class_("span12")).
						div(class_("body-content")).
							div(class_("section")).
								div(class_("page-header")).
									h2().write("Changelog")._h2()
								._div();

			final Project project = getProjectFromHttpUrl(gitlabRepository+".git");
			final List<Tag> tags = getTags(project.getId());
			final Commit lastCommit = getLatestCommit(project.getId());

			// merge requests and issues
			if (project.getMergeRequestsEnabled() || project.getIssuesEnabled()) {
				final List<MergeRequest> mergeRequests = getMergeRequests(project.getId());
				final List<Issue> issues = getIssues(project.getId());

				// display merged merge requests with their associated tag (= release)
				Renderable tagsWithMergeRequestsAndIssues = new Renderable() {
					@Override
					public void renderOn(HtmlCanvas html) throws IOException {
						List<HtmlCanvas> results = new ArrayList<HtmlCanvas>();
						String lastCommitId = null;
						for (Tag tag : tags) {
							String commitId = tag.getCommit().getId();

							if (isTagExcluded(tag)) {
								lastCommitId = commitId;
								continue;
							}

							HtmlCanvas result = new HtmlCanvas();
							result = result.h3().write(tag.getName() + " (" + tag.getCommit().getCommittedDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd")) + ")")._h3();
							DateTime tagDate = tag.getCommit().getCommittedDate();

							if (lastCommitId != null) {
								result.p().a(href(gitlabRepository + "/compare/" + lastCommitId + "..." + commitId)).write("Full changelog")._a()._p();
							}
							lastCommitId = commitId;

							// merge requests
							boolean hasMergeRequest = false;
							for (MergeRequest mergeRequest : mergeRequests) {
								DateTime mergeDate = mergeRequest.getCreatedAt();
								if (mergeDate.isBefore(tagDate) && !mergeRequestsAdded.contains(mergeRequest.getSha())) {							
									if (!hasMergeRequest) {
										hasMergeRequest = true;
										result = result.h4().write("Merge requests")._h4();
										result = result.ul();
									}
									result = addMergeRequest(result, mergeRequest);
									mergeRequestsAdded.add(mergeRequest.getSha());
								}
							}
							if (hasMergeRequest) {
								result = result._ul();
							} else {
								result = result.p().write("No merge request for this release")._p();
							}

							// issues
							boolean hasIssues = false;
							for (Issue issue : issues) {
								DateTime closeDate = issue.getCreatedAt();
								if (closeDate.isBefore(tagDate) && !issuesAdded.contains(issue.getIid())) {							
									if (!hasIssues) {
										hasIssues = true;
										result = result.h4().write("Closed issues")._h4();
										result = result.ul();
									}
									result = addIssue(result, issue);
									issuesAdded.add(issue.getIid());
								}
							}
							if (hasIssues) {
								result = result._ul();
							} else {
								result = result.p().write("No issue for this release")._p();
							}

							results.add(result);
						}

						for (HtmlCanvas result : Lists.reverse(results)) {
							html.write(result.toHtml(), false);
						}
					}

				};

				DateTime tagDate = null;
				if (!tags.isEmpty()) { // get commit date of latest tag
					tagDate = tags.get(tags.size()-1).getCommit().getCommittedDate();
				}

				// display merged merge requests with no tag (= next release)
				html = html.h3().write("Next release")._h3();
				String lastCommitId = tags.get(tags.size()-1).getCommit().getId();
				if (lastCommitId != null) {
					html.a(href(gitlabRepository + "/compare/" + lastCommitId + "..." + lastCommit.getId())).write("Full changelog")._a();
				}

				// merge requests
				boolean hasMergeRequest = false;
				for (MergeRequest mergeRequest : mergeRequests) {
					DateTime mergeDate = mergeRequest.getCreatedAt();

					if ((tagDate == null || !mergeDate.isBefore(tagDate)) && !mergeRequestsAdded.contains(mergeRequest.getSha())) {
						if (!hasMergeRequest) {
							hasMergeRequest = true;
							html = html.h4().write("Merge requests")._h4();
							html = html.ul();
						}
						html = addMergeRequest(html, mergeRequest);
						mergeRequestsAdded.add(mergeRequest.getSha());
					}
				}
				if (hasMergeRequest) {
					html = html._ul();
				}

				// issues
				boolean hasIssues = false;
				for (Issue issue : issues) {
					DateTime closeDate = issue.getCreatedAt();

					if ((tagDate == null || !closeDate.isBefore(tagDate)) && !issuesAdded.contains(issue.getIid())) {
						if (!hasIssues) {
							hasIssues = true;
							html = html.h4().write("Closed issues")._h4();
							html = html.ul();
						}
						html = addIssue(html, issue);
						issuesAdded.add(issue.getIid());
					}
				}
				if (hasIssues) {
					html = html._ul();
				}

				html.render(tagsWithMergeRequestsAndIssues);
			}

			html = html
							._div()
						._div()
					._div()
				._div();
		} catch (UnirestException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

		return html;
	}

	private boolean isTagExcluded(Tag tag) {
		for (String pattern : excludedTagsPattern) {
			if (Pattern.compile(pattern).matcher(tag.getName()).matches()) {
				return true;
			}
		}

		return false;
	}

	private static boolean getRequestOK(GetRequest getRequest) throws UnirestException {
		return getRequest != null && getRequest.asString() != null && getRequest.asString().getStatus() == 200;
	}

	private HtmlCanvas addMergeRequest(HtmlCanvas html, MergeRequest mergeRequest) throws IOException {
		return html.li().h5().write(mergeRequest.getTitle() + " ").a(href(gitlabRepository + "/merge_requests/"+mergeRequest.getIid()).class_("external")).write("#"+mergeRequest.getIid())._a()._h5().p().write(mergeRequest.getDescription())._p()._li();
	}

	private HtmlCanvas addIssue(HtmlCanvas html, Issue issue) throws IOException {
		return html.li().h5().write(issue.getTitle() + " ").a(href(gitlabRepository + "/issues/"+issue.getIid()).class_("external")).write("#"+issue.getIid())._a()._h5().p().write(issue.getDescription())._p()._li();
	}

	private List<Project> getProjects() throws UnirestException {
		if (projects != null) {
			return projects;
		}

		GetRequest projectsRequest = Unirest.get(apiEndPoint + "/projects").header("PRIVATE-TOKEN", privateToken);

		if (getRequestOK(projectsRequest)) {
			String json = projectsRequest.asJson().getBody().getArray().toString();
			projects = Arrays.asList(mapper.readValue(json, Project[].class));
		}

		return projects;
	}

	private Project getProjectFromHttpUrl(String httpUrl) throws UnirestException {
		List<Project> projects = getProjects();

		for (Project project : projects) {
			if (project.getHttpUrlToRepo().equals(httpUrl)) {
				return project;
			}
		}

		return null;
	}

	private Commit getLatestCommit(Integer projectId) throws UnirestException {
		return getCommits(projectId).get(0);		
	}

	private List<Commit> getCommits(Integer projectId) throws UnirestException {
		GetRequest commitsRequest = Unirest.get(apiEndPoint + "/projects/"+projectId.toString()+"/repository/commits").header("PRIVATE-TOKEN", privateToken);

		if (getRequestOK(commitsRequest)) {
			String json = commitsRequest.asJson().getBody().getArray().toString();
			return Arrays.asList(mapper.readValue(json, Commit[].class));
		}

		return null;
	}

	private List<Issue> getIssues(Integer projectId) throws UnirestException {
		GetRequest mergeRequestsRequest = Unirest.get(apiEndPoint + "/projects/"+projectId.toString()+"/issues?state=closed").header("PRIVATE-TOKEN", privateToken);
		
		if (getRequestOK(mergeRequestsRequest)) {
			String json = mergeRequestsRequest.asJson().getBody().getArray().toString();
			return Arrays.asList(mapper.readValue(json, Issue[].class));
		}
		
		return null;
	}

	private List<MergeRequest> getMergeRequests(Integer projectId) throws UnirestException {
		GetRequest mergeRequestsRequest = Unirest.get(apiEndPoint + "/projects/"+projectId.toString()+"/merge_requests?state=merged").header("PRIVATE-TOKEN", privateToken);
		
		if (getRequestOK(mergeRequestsRequest)) {
			String json = mergeRequestsRequest.asJson().getBody().getArray().toString();
			return Arrays.asList(mapper.readValue(json, MergeRequest[].class));
		}
		
		return null;
	}

	private List<Tag> getTags(Integer projectId) throws UnirestException {
		GetRequest tagsRequest = Unirest.get(apiEndPoint + "/projects/"+projectId.toString()+"/repository/tags").header("PRIVATE-TOKEN", privateToken);

		if (getRequestOK(tagsRequest)) {
			String json = tagsRequest.asJson().getBody().getArray().toString();
			List<Tag> result = Arrays.asList(mapper.readValue(json, Tag[].class));
			Comparator<? super Tag> c = new Comparator<Tag>() {
				@Override
				public int compare(Tag t1, Tag t2) {
					return t1.getCommit().getCommittedDate().compareTo(t2.getCommit().getCommittedDate());
				}
			};
			Collections.sort(result, c); // sort in chronological time
			return result;
		}

		return null;
	}

	private void init() throws IOException, UnirestException {
		mapper = new ObjectMapper() {
		    private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

		    public <T> T readValue(String value, Class<T> valueType) {
		        try {
		        	jacksonObjectMapper.registerModule(new JodaModule());
		            return jacksonObjectMapper.readValue(value, valueType);
		        } catch (IOException e) {
		            throw new RuntimeException(e);
		        }
		    }

		    public String writeValue(Object value) {
		        try {
		            return jacksonObjectMapper.writeValueAsString(value);
		        } catch (JsonProcessingException e) {
		            throw new RuntimeException(e);
		        }
		    }
		};

		Unirest.setObjectMapper(mapper);
	}

}
