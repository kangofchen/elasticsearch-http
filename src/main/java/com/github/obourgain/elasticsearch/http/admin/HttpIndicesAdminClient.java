package com.github.obourgain.elasticsearch.http.admin;

import java.util.concurrent.Future;
import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.GenericAction;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesAction;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesAction;
import org.elasticsearch.action.admin.indices.close.CloseIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsAction;
import org.elasticsearch.action.admin.indices.flush.FlushAction;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsAction;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingAction;
import org.elasticsearch.action.admin.indices.open.OpenIndexAction;
import org.elasticsearch.action.admin.indices.optimize.OptimizeAction;
import org.elasticsearch.action.admin.indices.refresh.RefreshAction;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsAction;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsAction;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesAction;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryRequest;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.client.IndicesAdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.obourgain.elasticsearch.http.HttpClientImpl;
import com.github.obourgain.elasticsearch.http.handler.ActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.CreateIndexActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.DeleteIndexActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.FlushActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.GetAliasesActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.GetMappingsActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.GetSettingsActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.GetTemplatesActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.IndicesAliasesActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.IndicesExistsActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.OptimizeActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.PutIndexTemplateActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.RefreshActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.ValidateQueryActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.close.CloseIndexActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.mapping.put.PutMappingActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.open.OpenIndexActionHandler;
import com.github.obourgain.elasticsearch.http.handler.admin.indices.settings.UpdateSettingsActionHandler;
import com.github.obourgain.elasticsearch.http.response.validate.ValidateQueryResponse;
import com.google.common.collect.ImmutableMap;

/**
 * @author olivier bourgain
 */
public class HttpIndicesAdminClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpIndicesAdminClient.class);

    private final HttpClientImpl httpClient;
    private final ImmutableMap<GenericAction, ActionHandler> actionHandlers;

    private ValidateQueryActionHandler validateQueryActionHandler = new ValidateQueryActionHandler(this);

    public HttpIndicesAdminClient(HttpClientImpl httpClient) {
        this.httpClient = httpClient;
        ImmutableMap.Builder<GenericAction, ActionHandler> tempActionHandlers = ImmutableMap.builder();
        tempActionHandlers.put(PutIndexTemplateAction.INSTANCE, new PutIndexTemplateActionHandler(this));
        tempActionHandlers.put(GetIndexTemplatesAction.INSTANCE, new GetTemplatesActionHandler(this));
        tempActionHandlers.put(CreateIndexAction.INSTANCE, new CreateIndexActionHandler(this));
        tempActionHandlers.put(IndicesExistsAction.INSTANCE, new IndicesExistsActionHandler(this));
        tempActionHandlers.put(UpdateSettingsAction.INSTANCE, new UpdateSettingsActionHandler(this));
        tempActionHandlers.put(DeleteIndexAction.INSTANCE, new DeleteIndexActionHandler(this));
        tempActionHandlers.put(GetMappingsAction.INSTANCE, new GetMappingsActionHandler(this));
        tempActionHandlers.put(GetSettingsAction.INSTANCE, new GetSettingsActionHandler(this));
        tempActionHandlers.put(IndicesAliasesAction.INSTANCE, new IndicesAliasesActionHandler(this));
        tempActionHandlers.put(GetAliasesAction.INSTANCE, new GetAliasesActionHandler(this));
        tempActionHandlers.put(RefreshAction.INSTANCE, new RefreshActionHandler(this));
        tempActionHandlers.put(FlushAction.INSTANCE, new FlushActionHandler(this));
        tempActionHandlers.put(OptimizeAction.INSTANCE, new OptimizeActionHandler(this));
        tempActionHandlers.put(OpenIndexAction.INSTANCE, new OpenIndexActionHandler(this));
        tempActionHandlers.put(CloseIndexAction.INSTANCE, new CloseIndexActionHandler(this));
        tempActionHandlers.put(PutMappingAction.INSTANCE, new PutMappingActionHandler(this));
        this.actionHandlers = tempActionHandlers.build();
    }

    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder, IndicesAdminClient>> ActionFuture<Response> execute(Action<Request, Response, RequestBuilder, IndicesAdminClient> action, Request request) {
        PlainActionFuture<Response> future = PlainActionFuture.newFuture();
        ActionRequestValidationException validationException = request.validate();
        if (validationException != null) {
            future.onFailure(validationException);
            return future;
        }
        ActionHandler<Request, Response, RequestBuilder> handler = actionHandlers.get(action);
        if(handler == null) {
            throw new IllegalStateException("no handler found for action " + action);
        }
        handler.execute(request, future);
        return future;
    }

    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder, IndicesAdminClient>> void execute(Action<Request, Response, RequestBuilder, IndicesAdminClient> action, Request request, ActionListener<Response> listener) {
        ActionHandler<Request, Response, RequestBuilder> handler = actionHandlers.get(action);
        if(handler == null) {
            throw new IllegalStateException("no handler found for action " + action);
        }
        handler.execute(request, listener);
    }

    public HttpClientImpl getHttpClient() {
        return httpClient;
    }


    public void validateQuery(ValidateQueryRequest request, ActionListener<ValidateQueryResponse> listener) {
        validateQueryActionHandler.execute(request, listener);
    }

    public Future<ValidateQueryResponse> exists(ValidateQueryRequest request) {
        PlainActionFuture<ValidateQueryResponse> future = PlainActionFuture.newFuture();
        validateQuery(request, future);
        return future;
    }

}
