package org.elasticsearch.action.admin.indices.validate.query;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.common.bytes.BytesReference;

import java.util.List;

/**
 * @author olivier bourgain
 */
public class ValidateRequestAccessor {

    /**
     * This exposes the package visible constructor {@link ValidateQueryResponse#ValidateQueryResponse(boolean, java.util.List, int, int, int, java.util.List)}.
     */
    public static ValidateQueryResponse build(boolean valid, List<QueryExplanation> queryExplanations, int totalShards, int successfulShards, int failedShards, List<ShardOperationFailedException> shardFailures) {
        return new ValidateQueryResponse(valid, queryExplanations, totalShards, successfulShards, failedShards, shardFailures);
    }

    public static BytesReference getSource(ValidateQueryRequest validateQueryRequest) {
        return validateQueryRequest.source();
    }

}