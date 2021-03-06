package com.atomist.rug.functions.travis

import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}

/**
  *  Use Travis API to encrypt strings using the repository public key
  */
class EncryptFunction
  extends AnnotatedRugFunction
    with TravisFunction {

  /** Encrypts a value using Travis CI repo public key
    *
    * @param owner   GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo    name of the repo to enable
    * @param content content to encrypt
    * @param githubToken   GitHub token with proper scopes for Travis CI
    * @return `content` encrypted using the Travis CI repo public key
    */
  @RugFunction(name = "travis-encrypt", description = "Encrypts a value using Travis CI repo public key",
    tags = Array(new Tag(name = "travis-ci"), new Tag(name = "ci")))
  def encrypt(@Parameter(name = "owner") owner: String,
              @Parameter(name = "repo") repo: String,
              @Parameter(name = "content") content: String,
              @Secret(name = "githubToken", path = TravisFunction.githubTokenPath) githubToken: String
             ): FunctionResponse = {
    val repoSlug = RepoSlug(owner, repo)
    Encrypt(travisEndpoints, gitHubRepo).tryEncrypt(repoSlug, content, GitHubToken(githubToken))
  }

}
