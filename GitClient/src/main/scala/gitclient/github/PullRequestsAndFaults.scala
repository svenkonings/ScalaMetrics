package gitclient.github

case class PullRequestsAndFaults(pullRequests: List[PullRequest], faults: Set[Int])
