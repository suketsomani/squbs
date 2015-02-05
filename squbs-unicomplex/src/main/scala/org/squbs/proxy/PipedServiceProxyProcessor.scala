/*
 * Licensed to Typesafe under one or more contributor license agreements.
 * See the AUTHORS file distributed with this work for
 * additional information regarding copyright ownership.
 * This file is licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.squbs.proxy

import scala.concurrent.{ExecutionContext, Promise, Future}

trait PipelineHandler {

  def process(reqCtx: RequestContext): Future[RequestContext]

}

case class PipelineConfig(
                           inbound: Seq[PipelineHandler] = Seq.empty,
                           outbound: Seq[PipelineHandler] = Seq.empty
                           )

class PipedServiceProxyProcessor(pipeConfig: PipelineConfig) extends ServiceProxyProcessor {
  //inbound processing
  def inbound(reqCtx: RequestContext)(implicit executor: ExecutionContext): Future[RequestContext] = {
    pipeConfig.inbound.foldLeft(Promise.successful(reqCtx).future)((ctx, handler) => ctx.flatMap(handler.process(_)))
  }

  //outbound processing
  def outbound(reqCtx: RequestContext)(implicit executor: ExecutionContext): Future[RequestContext] = {
    pipeConfig.outbound.foldLeft(Promise.successful(reqCtx).future)((ctx, handler) => ctx.flatMap(handler.process(_)))
  }
}
