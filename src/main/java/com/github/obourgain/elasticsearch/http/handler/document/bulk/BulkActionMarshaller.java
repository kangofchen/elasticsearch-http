package com.github.obourgain.elasticsearch.http.handler.document.bulk;

import java.io.IOException;
import java.util.List;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.VersionType;
import com.github.obourgain.elasticsearch.http.handler.document.update.UpdateHelper;
import com.google.common.base.Charsets;
import rx.Observable;
import rx.functions.Func1;

public class BulkActionMarshaller {

    public static final byte[] LINE_BREAK = "\n".getBytes(Charsets.US_ASCII);

    public static Observable<byte[]> lazyConvertToBytes(List<ActionRequest> actions) {
        return Observable.from(actions)
                .flatMap(new Func1<ActionRequest, Observable<byte[]>>() {
                    @Override
                    public Observable<byte[]> call(ActionRequest actionRequest) {
                        try {
                            if (actionRequest instanceof IndexRequest) {
                                IndexRequest indexRequest = (IndexRequest) actionRequest;
                                return Observable.just(buildIndexCommand(indexRequest), LINE_BREAK, indexRequest.source().toBytes(), LINE_BREAK);

                            } else if (actionRequest instanceof DeleteRequest) {
                                DeleteRequest deleteRequest = (DeleteRequest) actionRequest;
                                return Observable.just(buildDeleteCommand(deleteRequest), LINE_BREAK);

                            } else if (actionRequest instanceof UpdateRequest) {
                                UpdateRequest updateRequest = (UpdateRequest) actionRequest;
                                return Observable.just(buildUpdateCommand(updateRequest), LINE_BREAK, UpdateHelper.buildRequestBody(updateRequest), LINE_BREAK);
                            } else {
                                throw new IllegalArgumentException("action type " + actionRequest.getClass().getName() + " not supported");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private static byte[] buildIndexCommand(IndexRequest indexRequest) throws IOException {
        try (XContentBuilder builder = XContentFactory.jsonBuilder().startObject()) {
            addCommonOptions(builder, "index", indexRequest.index(), indexRequest.type(), indexRequest.id(),
                    indexRequest.version(), indexRequest.versionType(), indexRequest.routing(),
                    indexRequest.consistencyLevel(), indexRequest.refresh());
            String parent = indexRequest.parent();
            String timestamp = indexRequest.timestamp();
            long ttl = indexRequest.ttl();
            if (parent != null) {
                builder.field("_parent", parent);
            }
            if (timestamp != null) {
                builder.field("_timestamp", timestamp);
            }
            if (ttl != -1) {
                builder.field("_ttl", ttl);
            }
            if (indexRequest.opType() != null) {
                builder.field("op_type", indexRequest.opType().toString().toLowerCase());
            }
            if (indexRequest.parent() != null) {
                builder.field("op_type", indexRequest.parent());
            }
            return builder.bytes().toBytes();
        }
    }

    private static byte[] buildDeleteCommand(DeleteRequest deleteRequest) throws IOException {
        try(XContentBuilder builder = XContentFactory.jsonBuilder().startObject()) {
            addCommonOptions(builder, "delete", deleteRequest.index(), deleteRequest.type(), deleteRequest.id(),
                    deleteRequest.version(), deleteRequest.versionType(), deleteRequest.routing(),
                    deleteRequest.consistencyLevel(), deleteRequest.refresh());
            return builder.bytes().toBytes();
        }
    }


    private static byte[] buildUpdateCommand(UpdateRequest updateRequest) throws IOException {
        try (XContentBuilder builder = XContentFactory.jsonBuilder().startObject()) {
            addCommonOptions(builder, "update", updateRequest.index(), updateRequest.type(), updateRequest.id(),
                    updateRequest.version(), updateRequest.versionType(), updateRequest.routing(),
                    updateRequest.consistencyLevel(), updateRequest.refresh());

            String timestamp = updateRequest.doc() != null ? updateRequest.doc().timestamp() : null;
            long ttl = updateRequest.doc() != null ? updateRequest.doc().ttl() : -1;

            if (timestamp != null) {
                builder.field("_timestamp", timestamp);
            }
            if (ttl != -1) {
                builder.field("_ttl", ttl);
            }
            if (updateRequest.retryOnConflict() != -1) {
                builder.field("_retry_on_conflict", updateRequest.retryOnConflict());
            }
            if (updateRequest.fields() != null && updateRequest.fields().length > 0) {
                builder.field("_fields", Strings.arrayToCommaDelimitedString(updateRequest.fields()));
            }
            if (updateRequest.doc() != null && updateRequest.doc().parent() != null) {
                builder.field("_parent", updateRequest.doc().parent());
            } else if (updateRequest.routing() != null) {
                builder.field("_parent", updateRequest.routing());
            }
            return builder.bytes().toBytes();
        }
    }

    private static void addCommonOptions(XContentBuilder builder, String command, String index, String type, String id,
                                         @Nullable long version, VersionType versionType, // may be Versions.MATCH_ANY and null
                                         @Nullable String routing,
                                         @Nullable WriteConsistencyLevel consistencyLevel,
                                         boolean refresh

    ) throws IOException {
        builder.startObject(command)
                .field("_index", index)
                .field("_type", type)
                .field("_id", id);
        if (version != Versions.MATCH_ANY) {
            builder.field("_version", version);
            builder.field("_version_type", versionType.toString().toLowerCase());
        }
        if (routing != null) {
            builder.field("_routing", routing);
        }
        if (consistencyLevel != WriteConsistencyLevel.DEFAULT) {
            builder.field("_consistency", consistencyLevel.toString().toLowerCase());
        }
        if (refresh) {
            builder.field("_refresh", true);
        }
    }

}
