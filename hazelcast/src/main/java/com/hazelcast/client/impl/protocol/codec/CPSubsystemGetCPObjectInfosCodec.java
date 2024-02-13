/*
 * Copyright (c) 2008-2023, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.client.impl.protocol.codec;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.Generated;
import com.hazelcast.client.impl.protocol.codec.builtin.*;
import com.hazelcast.client.impl.protocol.codec.custom.*;

import javax.annotation.Nullable;

import static com.hazelcast.client.impl.protocol.ClientMessage.*;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.*;

/*
 * This file is auto-generated by the Hazelcast Client Protocol Code Generator.
 * To change this file, edit the templates or the protocol
 * definitions on the https://github.com/hazelcast/hazelcast-client-protocol
 * and regenerate it.
 */

/**
 * Returns all active CP structures that belong to the group with the provided CPGroupId and service name.
 * A snapshot is used to retrieve the result.
 */
@Generated("a7f6f9fb8ac88b27409b9b2133e8c90d")
public final class CPSubsystemGetCPObjectInfosCodec {
    //hex: 0x220600
    public static final int REQUEST_MESSAGE_TYPE = 2229760;
    //hex: 0x220601
    public static final int RESPONSE_MESSAGE_TYPE = 2229761;
    private static final int REQUEST_TOMBSTONE_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_INITIAL_FRAME_SIZE = REQUEST_TOMBSTONE_FIELD_OFFSET + BOOLEAN_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + BYTE_SIZE_IN_BYTES;

    private CPSubsystemGetCPObjectInfosCodec() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class RequestParameters {

        /**
         * Defines the cp group to return cp structures from
         */
        public com.hazelcast.cp.internal.RaftGroupId groupId;

        /**
         * The service name of the cp structures to return
         */
        public java.lang.String serviceName;

        /**
         * Whether to return cp tombstones. If true, only tombstones will be returned. If false, 
         * only non-tombstone cp structures will be returned.
         */
        public boolean tombstone;
    }

    public static ClientMessage encodeRequest(com.hazelcast.cp.internal.RaftGroupId groupId, java.lang.String serviceName, boolean tombstone) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setRetryable(true);
        clientMessage.setOperationName("CPSubsystem.GetCPObjectInfos");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        encodeBoolean(initialFrame.content, REQUEST_TOMBSTONE_FIELD_OFFSET, tombstone);
        clientMessage.add(initialFrame);
        RaftGroupIdCodec.encode(clientMessage, groupId);
        StringCodec.encode(clientMessage, serviceName);
        return clientMessage;
    }

    public static CPSubsystemGetCPObjectInfosCodec.RequestParameters decodeRequest(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        RequestParameters request = new RequestParameters();
        ClientMessage.Frame initialFrame = iterator.next();
        request.tombstone = decodeBoolean(initialFrame.content, REQUEST_TOMBSTONE_FIELD_OFFSET);
        request.groupId = RaftGroupIdCodec.decode(iterator);
        request.serviceName = StringCodec.decode(iterator);
        return request;
    }

    public static ClientMessage encodeResponse(java.util.Collection<java.lang.String> response) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        clientMessage.add(initialFrame);

        ListMultiFrameCodec.encode(clientMessage, response, StringCodec::encode);
        return clientMessage;
    }

    /**
     * List of names of CP structures that belong to the specified cp group and service.
     */
    public static java.util.List<java.lang.String> decodeResponse(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        //empty initial frame
        iterator.next();
        return ListMultiFrameCodec.decode(iterator, StringCodec::decode);
    }
}
