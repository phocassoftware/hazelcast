/*
 * Copyright 2026 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.kinesis.impl.source;

import com.hazelcast.jet.retry.RetryStrategy;
import com.hazelcast.logging.ILogger;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorResponse;
import software.amazon.awssdk.services.kinesis.model.Shard;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class ShardReaderTest {

    @Test
    public void emptyReadRespectsPollingInterval() {
        KinesisAsyncClient client = mock(KinesisAsyncClient.class);
        when(client.getShardIterator(any(GetShardIteratorRequest.class)))
                .thenReturn(completedFuture(GetShardIteratorResponse.builder().shardIterator("iterator").build()));
        when(client.getRecords(any(GetRecordsRequest.class)))
                .thenReturn(completedFuture(GetRecordsResponse.builder().nextShardIterator("next").build()));

        ShardReader reader = new ShardReader(client, "stream", Shard.builder().shardId("shard").build(),
                "1", mock(RetryStrategy.class), new InitialShardIterators(), mock(ILogger.class));
        long start = System.nanoTime();
        reader.probe(start); // Request the shard iterator.
        reader.probe(start); // Accept the iterator.
        reader.probe(start); // Send the first GetRecords request.
        reader.probe(start); // Accept the empty response.

        // The first randomized interval is between 100 and 300 ms at five reads per second.
        reader.probe(start + MILLISECONDS.toNanos(99));
        verify(client).getRecords(any(GetRecordsRequest.class));
        reader.probe(start + MILLISECONDS.toNanos(300));
        verify(client, times(2)).getRecords(any(GetRecordsRequest.class));
    }
}
