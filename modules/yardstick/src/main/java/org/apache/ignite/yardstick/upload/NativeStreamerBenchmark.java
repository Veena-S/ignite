/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.yardstick.upload;

import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.yardstick.upload.model.Values10;

/**
 * Benchmark that performs single upload of number of entries using {@link IgniteDataStreamer}.
 */
public class NativeStreamerBenchmark extends AbstractNativeBenchmark {
    /**
     * Uploads randomly generated entries to specified cache.
     *
     * @param cacheName - name of the cache.
     * @param insertsCnt - how many entries should be uploaded.
     */
    @Override protected void upload(String cacheName, long insertsCnt) {
        try (IgniteDataStreamer<Long, Values10> streamer = ignite().dataStreamer(cacheName)) {
            if (args.upload.streamerBufSize() != null)
                streamer.perNodeBufferSize(args.upload.streamerBufSize());

            if (args.upload.streamerNodeParOps() != null)
                streamer.perNodeParallelOperations(args.upload.streamerNodeParOps());

            int batchSize = args.upload.streamerLocBatchSize();

            // IgniteDataStreamer.addData(Object, Object) has known performance issue,
            // so we have an option to work it around.
            if (batchSize == 1) {
                for (long i = 1; i <= insertsCnt; i++)
                    streamer.addData(i, new Values10());
            }
            else {
                Map<Long, Values10> buf = new HashMap<>(batchSize);

                for (long i = 1; i <= insertsCnt; i++) {
                    buf.put(i, new Values10());

                    if (i % batchSize == 0 || i == insertsCnt) {
                        streamer.addData(buf);

                        buf.clear();
                    }
                }
            }
        }
    }
}
