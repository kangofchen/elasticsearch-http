package com.github.obourgain.elasticsearch.http.handler.document;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.mlt.MoreLikeThisAction;
import org.elasticsearch.action.mlt.MoreLikeThisRequest;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.obourgain.elasticsearch.http.HttpClient;
import com.github.obourgain.elasticsearch.http.concurrent.ListenerAsyncCompletionHandler;
import com.github.obourgain.elasticsearch.http.response.search.SearchResponse;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

/**
 * @author olivier bourgain
 */
public class MoreLikeThisActionHandler {

    private static final Logger logger = LoggerFactory.getLogger(MoreLikeThisActionHandler.class);

    private final HttpClient httpClient;

    public MoreLikeThisActionHandler(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public MoreLikeThisAction getAction() {
        return MoreLikeThisAction.INSTANCE;
    }

    public void execute(final MoreLikeThisRequest request, final ActionListener<SearchResponse> listener) {
        logger.debug("MoreLikeThis request {}", request);
        try {
            StringBuilder url = new StringBuilder(httpClient.getUrl()).append("/");
            url.append(request.index()).append("/");
            url.append(request.type()).append("/");
            url.append(request.id()).append("/");
            url.append("_mlt");

            AsyncHttpClient.BoundRequestBuilder httpRequest = httpClient.asyncHttpClient.prepareGet(url.toString());

            // TODO test options

            if (request.fields() != null) {
                httpRequest.addQueryParam("fields", Strings.arrayToCommaDelimitedString(request.fields()));
            }
            if (request.routing() != null) {
                httpRequest.addQueryParam("routing", request.routing());
            }
            if (request.percentTermsToMatch() != -1) {
                httpRequest.addQueryParam("percent_terms_to_match", String.valueOf(request.percentTermsToMatch()));
            }

            if (request.minTermFreq() != -1) {
                httpRequest.addQueryParam("min_term_freq", String.valueOf(request.minTermFreq()));
            }
            if (request.maxQueryTerms() != -1) {
                httpRequest.addQueryParam("max_query_terms", String.valueOf(request.maxQueryTerms()));
            }

            if (request.stopWords() != null) {
                httpRequest.addQueryParam("stop_words", Strings.arrayToCommaDelimitedString(request.stopWords()));
            }

            if (request.minDocFreq() != -1) {
                httpRequest.addQueryParam("min_doc_freq", String.valueOf(request.minDocFreq()));
            }
            if (request.maxDocFreq() != -1) {
                httpRequest.addQueryParam("max_doc_freq", String.valueOf(request.maxDocFreq()));
            }

            if (request.minWordLength() != -1) {
                httpRequest.addQueryParam("min_word_len", String.valueOf(request.minWordLength()));
            }
            if (request.maxWordLength() != -1) {
                httpRequest.addQueryParam("max_word_len", String.valueOf(request.maxWordLength()));
            }

            if (request.boostTerms() != -1) {
                httpRequest.addQueryParam("boost_terms", String.valueOf(request.boostTerms()));
            }

            if (request.searchIndices() != null) {
                httpRequest.addQueryParam("search_indices", Strings.arrayToCommaDelimitedString(request.searchIndices()));
            }

            if (request.searchTypes() != null) {
                httpRequest.addQueryParam("search_types", Strings.arrayToCommaDelimitedString(request.searchTypes()));
            }

            if (request.searchSize() != 0) {
                httpRequest.addQueryParam("search_size", String.valueOf(request.searchSize()));
            }
            if (request.searchScroll() != null) {
                httpRequest.addQueryParam("search_scroll", request.searchScroll().keepAlive().toString());
            }
            if (request.searchFrom() != 0) {
                httpRequest.addQueryParam("search_from", String.valueOf(request.searchFrom()));
            }
            if (request.include()) {
                httpRequest.addQueryParam("include", String.valueOf(request.include()));
            }
            if (request.fields() != null) {
                httpRequest.addQueryParam("mlt_fields", Strings.arrayToCommaDelimitedString(request.fields()));
            }
            switch (request.searchType()) {
                case COUNT:
                case QUERY_AND_FETCH:
                case QUERY_THEN_FETCH:
                case DFS_QUERY_AND_FETCH:
                case DFS_QUERY_THEN_FETCH:
                case SCAN:
                    httpRequest.addQueryParam("search_type", request.searchType().name().toLowerCase());
                    break;
                default:
                    throw new IllegalStateException("search_type " + request.searchType() + " is not supported");
            }

            // TODO it is almost like a search request, add all options

            if (request.searchSource() != null) {
                httpRequest.setBody(request.searchSource().toBytes());
            }

            httpRequest.execute(new ListenerAsyncCompletionHandler<SearchResponse>(listener) {
                @Override
                protected SearchResponse convert(Response response) {
                    return SearchResponse.parse(response);
                }
            });
        } catch (Exception e) {
            listener.onFailure(e);
        }
    }
}
