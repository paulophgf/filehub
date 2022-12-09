package br.com.p8projects.filehub.test;

import org.springframework.stereotype.Component;

@Component
public class TestProperties {

    private String gitRepositoryUrl;
    private String gitRepositoryToken;

    public TestProperties() {
        this.gitRepositoryUrl = System.getenv("CONFIG_GIT_FILE_URL");
        this.gitRepositoryToken = System.getenv("CONFIG_GIT_TOKEN");
    }


    public String getGitRepositoryUrl() {
        if(this.gitRepositoryUrl == null || this.gitRepositoryUrl.isEmpty()) {
            throw new TestPropertiesException("The property CONFIG_GIT_FILE_URL is undefined");
        }
        return gitRepositoryUrl;
    }

    public String getGitRepositoryToken() {
        if(this.gitRepositoryToken == null || this.gitRepositoryToken.isEmpty()) {
            throw new TestPropertiesException("The property CONFIG_GIT_TOKEN is undefined");
        }
        return gitRepositoryToken;
    }

}
