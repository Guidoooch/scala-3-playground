package com.example.cats.effect

object PathDependentTypes extends App:

  class Organization:
    class Member(val name: String)

    def makeMember(name: String): Member = new Member(name)

    def printMemberInfo(member: Member): Unit = println(s"Member of this org: ${member.name}")

  val org1 = new Organization
  val org2 = new Organization

  val alice = org1.makeMember("Alice")
  val bob = org2.makeMember("Bob")

  org1.printMemberInfo(alice)
//  org1.printMemberInfo(bob)

  class OrganizationG[M](val makeMember: String => M):
    def printMemberInfo(member: M): Unit = println(s"Member: $member")

  class Member1(val name: String) {
    override def toString: String = name
  }

  val org3 = new OrganizationG[Member1](name => new Member1(name))
  val org4 = new OrganizationG[Member1](name => new Member1(name))

  val alice2 = org3.makeMember("Alice")
  val bob2 = org4.makeMember("Bob")

  org3.printMemberInfo(bob2)
