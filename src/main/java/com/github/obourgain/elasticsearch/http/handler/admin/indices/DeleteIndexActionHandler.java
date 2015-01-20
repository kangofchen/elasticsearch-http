package com.github.obourgain.elasticsearch.http.handler.admin.indices;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestAccessor;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.obourgain.elasticsearch.http.HttpClient;
import com.github.obourgain.elasticsearch.http.admin.HttpIndicesAdminClient;
import com.github.obourgain.elasticsearch.http.concurrent.ListenerAsyncCompletionHandler;
import com.github.obourgain.elasticsearch.http.handler.HttpRequestUtils;
import com.github.obourgain.elasticsearch.http.response.admin.indices.delete.DeleteIndexResponse;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

/**
 * @author olivier bourgain
 */
public class DeleteIndexActionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeleteIndexActionHandler.class);

    private final HttpIndicesAdminClient indicesAdminClient;

    public DeleteIndexActionHandler(HttpIndicesAdminClient indicesAdminClient) {
        this.indicesAdminClient = indicesAdminClient;
    }

    public DeleteIndexAction getAction() {
        return DeleteIndexAction.INSTANCE;
    }

    public void execute(DeleteIndexRequest request, final ActionListener<DeleteIndexResponse> listener) {
        logger.debug("delete index request {}", request);
        try {
            String[] indices = DeleteIndexRequestAccessor.indices(request);
            if(indices.length == 0) {
                throw new IllegalArgumentException("missing indices");
            }


            HttpClient httpClient = indicesAdminClient.getHttpClient();
            AsyncHttpClient.BoundRequestBuilder httpRequest = httpClient.asyncHttpClient.prepareDelete(httpClient.getUrl() + "/" + Strings.arrayToCommaDelimitedString(indices));

            if(request.timeout() != null) {
                httpRequest.addQueryParam("timeout", request.timeout().format());
            }
            if(request.masterNodeTimeout() != null) {
                httpRequest.addQueryParam("master_timeout", request.timeout().format());
            }

            // TODO indices options
            HttpRequestUtils.addIndicesOptions(httpRequest, request);
            httpRequest.execute(new ListenerAsyncCompletionHandler<DeleteIndexResponse>(listener) {
                @Override
                protected DeleteIndexResponse convert(Response response) {
                    return DeleteIndexResponse.parse(response);
                }
            });
        } catch (Exception e) {
            listener.onFailure(e);
        }
    }
}
