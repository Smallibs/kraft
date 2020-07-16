# kraft

A Kotlin multiplatform Raft consensus implementation base on:
- agnostic network layer
- event based algorithm i.e. can be used with a actor based approach
- agnotic database i.e. base on abstract command execution

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

## Work in progress ...

A proof of this implementation should be done.

## License 

```
                  GNU LESSER GENERAL PUBLIC LICENSE
                       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.
```
