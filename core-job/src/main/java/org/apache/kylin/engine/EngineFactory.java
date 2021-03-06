/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.kylin.engine;

import static org.apache.kylin.metadata.model.IEngineAware.ID_MR_V1;
import static org.apache.kylin.metadata.model.IEngineAware.ID_MR_V2;

import java.util.HashMap;
import java.util.Map;

import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.util.ImplementationSwitch;
import org.apache.kylin.cube.CubeSegment;
import org.apache.kylin.job.execution.DefaultChainedExecutable;
import org.apache.kylin.metadata.model.IEngineAware;

public class EngineFactory {

    private static ImplementationSwitch<IBatchCubingEngine> batchEngines;
    private static ImplementationSwitch<IStreamingCubingEngine> streamingEngines;
    static {
        Map<Integer, String> impls = new HashMap<>();
        impls.put(ID_MR_V1, KylinConfig.getInstanceFromEnv().getMRBatchEngineV1Class());
        impls.put(ID_MR_V2, KylinConfig.getInstanceFromEnv().getMRBatchEngineV2Class());
        batchEngines = new ImplementationSwitch<IBatchCubingEngine>(impls, IBatchCubingEngine.class);

        impls.clear();
        streamingEngines = new ImplementationSwitch<IStreamingCubingEngine>(impls, IStreamingCubingEngine.class); // TODO
    }

    public static IBatchCubingEngine batchEngine(IEngineAware aware) {
        return batchEngines.get(aware.getEngineType());
    }

    public static IStreamingCubingEngine streamingEngine(IEngineAware aware) {
        return streamingEngines.get(aware.getEngineType());
    }

    /** Build a new cube segment, typically its time range appends to the end of current cube. */
    public static DefaultChainedExecutable createBatchCubingJob(CubeSegment newSegment, String submitter) {
        return batchEngine(newSegment).createBatchCubingJob(newSegment, submitter);
    }

    /** Merge multiple small segments into a big one. */
    public static DefaultChainedExecutable createBatchMergeJob(CubeSegment mergeSegment, String submitter) {
        return batchEngine(mergeSegment).createBatchMergeJob(mergeSegment, submitter);
    }

    public static Runnable createStreamingCubingBuilder(CubeSegment seg) {
        return streamingEngine(seg).createStreamingCubingBuilder(seg);
    }

}
