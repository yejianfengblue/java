package com.yejianfengblue.java.jgit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

class JGitDiffUpstreamTest {

    Logger log = LoggerFactory.getLogger(getClass());

    private static final String GIT_REPOS_PATH = "/home/k/test/";

    @Test
    void diffUpstream() {

        File dotGitFile = new File(GIT_REPOS_PATH + ".git");

        try {

            try (Repository repos = new FileRepositoryBuilder()
                    .setGitDir(dotGitFile)
                    .build()) {

                Git git = new Git(repos);

                AbstractTreeIterator treeIterator;
                try {
                    treeIterator = prepareTreeParser(repos, "@{upstream}");
                } catch (IOException e) {
                    log.error("Fail to prepare TreeParser from branch @{upstream}", e);
                    throw new IllegalStateException("Fail to refer to upstream branch", e);
                }

                List<DiffEntry> diffEntryList = git.diff()
                        .setOldTree(treeIterator)
                        .call();
                log.info("diffEntryList = {}", diffEntryList);

            } catch (GitAPIException e) {
                log.error("Fail to call Git API", e);
            }
        } catch (IOException e) {
            log.error("Fail to read .git directory from '{}'", dotGitFile, e);
        }
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repos, String branchName) throws IOException {

        CanonicalTreeParser treeParser = new CanonicalTreeParser();

        // I don't know what the magic string ^{tree} is, just copy from https://www.codeaffine.com/2016/06/16/jgit-diff/
        ObjectId treeId = repos.resolve(branchName + "^{tree}");

        try (ObjectReader reader = repos.newObjectReader()) {
            treeParser.reset(reader, treeId);
        }

        return treeParser;
    }
}
