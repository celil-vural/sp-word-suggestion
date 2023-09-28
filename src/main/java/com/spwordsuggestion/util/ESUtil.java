package com.spwordsuggestion.util;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class ESUtil {
    public static Supplier<Query> buildQueryForFieldAndValueWithTerm(String fieldName, String searchValue) {
        return () -> Query.of(q -> q.term(buildTermQueryForFieldAndValue(fieldName, searchValue)));
    }
    public static Supplier<Query> buildQueryForFieldAndValueWithMatch(String fieldName, String searchValue) {
        return () -> Query.of(q -> q.match(buildMatchQueryForFieldAndValue(fieldName, searchValue)));
    }
    public static TermQuery buildTermQueryForFieldAndValue(String fieldName, String searchValue) {
        return new TermQuery.Builder()
                .field(fieldName)
                .value(searchValue)
                .build();
    }
    public static MatchQuery buildMatchQueryForFieldAndValue(String fieldName, String searchValue) {
        return new MatchQuery.Builder()
                .field(fieldName)
                .query(searchValue)
                .build();
    }
}
