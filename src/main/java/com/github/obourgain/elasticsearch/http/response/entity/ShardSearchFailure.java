package com.github.obourgain.elasticsearch.http.response.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.xcontent.XContentParser;
import lombok.Getter;

@Getter
public class ShardSearchFailure {

    @Nullable
    private String index;
    @Nullable
    private String shard;
    private String reason;
    private int status;

     // TODO test

    public static List<ShardSearchFailure> parse(XContentParser parser) throws IOException {
        assert parser.currentToken() == XContentParser.Token.START_ARRAY : "expected a START_ARRAY token but was " + parser.currentToken();
        List<ShardSearchFailure> result = new ArrayList<>();
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token== XContentParser.Token.START_OBJECT) {
                ShardSearchFailure shardFailure = new ShardSearchFailure().doParse(parser);
                result.add(shardFailure);
            }
        }
        return result;
    }

    private ShardSearchFailure doParse(XContentParser parser) throws IOException {
        assert parser.currentToken() == XContentParser.Token.START_OBJECT : "expected a START_OBJECT token but was " + parser.currentToken();
        XContentParser.Token token;
        String currentFieldName = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if ("index".equals(currentFieldName)) {
                    index = parser.text();
                } else if ("shard".equals(currentFieldName)) {
                    shard = parser.text();
                } else if ("reason".equals(currentFieldName)) {
                    reason = parser.text();
                } else if ("status".equals(currentFieldName)) {
                    status = parser.intValue();
                }
            }
        }
        return this;
    }
}
