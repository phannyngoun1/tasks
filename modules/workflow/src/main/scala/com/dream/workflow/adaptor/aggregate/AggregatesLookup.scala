package com.dream.workflow.adaptor.aggregate

import akka.actor.{Actor, ActorContext}
import com.dream.workflow.entity.account.AccountEntity
import com.dream.workflow.entity.account.AccountProtocol.AccountCmdRequest
import com.dream.workflow.entity.item.ItemEntity
import com.dream.workflow.entity.item.ItemProtocol.ItemCmdRequest
import com.dream.workflow.entity.participant.ParticipantEntity
import com.dream.workflow.entity.participant.ParticipantProtocol.ParticipantCmdRequest
import com.dream.workflow.entity.processinstance.ProcessInstanceEntity
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol.ProcessInstanceCmdRequest
import com.dream.workflow.entity.workflow.WorkflowEntity
import com.dream.workflow.entity.workflow.WorkflowProtocol.WorkFlowCmdRequest


trait AggregatesLookup {

  implicit def context: ActorContext

  def forwardToEntityAggregate: Actor.Receive = {
    case cmd: ProcessInstanceCmdRequest =>
      context
        .child(ProcessInstanceEntity.name(cmd.id))
        .fold(
          context.actorOf(ProcessInstanceEntity.prop, ProcessInstanceEntity.name(cmd.id )) forward cmd
        )(_ forward cmd)

    case cmd: WorkFlowCmdRequest =>
      context
        .child(WorkflowEntity.name(cmd.id))
        .fold(
          context.actorOf(WorkflowEntity.prop, WorkflowEntity.name(cmd.id)) forward cmd
        )(_ forward cmd)
    case cmd: ItemCmdRequest =>
      context
      .child(ItemEntity.name(cmd.id))
      .fold (
        context.actorOf(ItemEntity.prop, ItemEntity.name(cmd.id)) forward cmd
      )(_ forward cmd)

    case cmd: AccountCmdRequest =>
      context
        .child(AccountEntity.name(cmd.id))
        .fold(
          context.actorOf(AccountEntity.prop, AccountEntity.name(cmd.id)) forward cmd
        )(_ forward cmd)

    case cmd: ParticipantCmdRequest =>
      context
        .child(ParticipantEntity.name(cmd.id))
        .fold(
          context.actorOf(ParticipantEntity.prop, ParticipantEntity.name(cmd.id)) forward cmd
        )(_ forward cmd)
  }

}
