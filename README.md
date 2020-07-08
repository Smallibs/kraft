# kraft

Kotlin Raft consensus

##  Node Kind

The NodeKind denotes the behaviors linked to a node. We have:
- an Elector i.e. a Follower without a Leader in the Raft specification
- a Candidate
- a Follower and
- a Leader.

Each oriented arrow is defined by a message, a condition if necessary and a set of 
reactions to be performed by the system.
 
For instance, when an Elector accepts a RequestVote message it becomes a Follower, and
an AcceptVote reaction should be performed. This reaction highlights the acceptance to
be send to the corresponding Candidate. This AcceptVote is managed by a Candidate node.
 
```
                                        TimeOut | ArmElectionTimeOut
                 +-------------------------------------------------------------------------+
                 |                                                                         |
                 v                   RequestVote | AcceptVote                              |                                                  |
          +-> Elector ------------------------------------------------------------+        |
          |      |                                                                |        |
          |      |  TimeOut | ArmElectionTimeOut StartElection                    v        |
          |      V                                                             Follower ---+
          +–+ Candidate <-±                                                       |
 Higher   |      |    |   | TimeOut | ArmElection TimeOut StartElection           +--------+
  Term    |      |    |   | AcceptVote if No Quorum                                        |
          |      |    +---+                                                                |
          |      |                                                                         |
          |      | AcceptVote if Quorum | ArmHeartBeatTime SynchronizeLog InsertMarkInLog  |
          |      V                                                                         |
          +-+ Leader <-+                                                                   |
          |        |   | TimeOut | ArmHeartBeatTime SynchronizeLog                         |
          |        +---+                                                                   |
          +--------------------------------------------------------------------------------+
```