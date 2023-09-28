package com.spwordsuggestion.repository;

import com.spwordsuggestion.model.Word;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface WordRepository extends ElasticsearchRepository<Word,String> {

}
