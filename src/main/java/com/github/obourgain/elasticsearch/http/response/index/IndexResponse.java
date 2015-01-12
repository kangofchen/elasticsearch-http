package com.github.obourgain.elasticsearch.http.response.index;

import lombok.Getter;
import lombok.experimental.Builder;

@Builder
@Getter
public class IndexResponse {

    private String index;
    private String type;
    private String id;
    private long version;
    private boolean created;

}
