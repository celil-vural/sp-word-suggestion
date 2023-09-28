package com.spwordsuggestion.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.spwordsuggestion.model.Word;
import com.spwordsuggestion.repository.WordRepository;
import com.spwordsuggestion.util.ESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;
    private final ElasticsearchClient elasticsearchClient;
    public void addText(String text){
        Set<Word> words = getWordsByString(text);
        wordRepository.saveAll(words);
    }
    private Set<Word> getWordsByString(String text) {
        String[] words = text.split(" ");
        return Arrays.stream(words).map(
                word->new Word(UUID.randomUUID().toString(),word))
                .collect(Collectors.toSet());
    }
    public List<Word> extractItemsFromResponse(SearchResponse<Word> response) {
        return response
                .hits()
                .hits()
                .stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }
    private SearchResponse<Word> getSuggestionsFromElasticsearch(String word) throws IOException {
        Supplier<Query> querySupplier = ESUtil.buildQueryForFieldAndValueWithMatch("word", word);
        return elasticsearchClient.search(q -> q.index("words_index")
                .query(querySupplier.get()), Word.class);
    }

    private List<String> sortedList(List<Word> suggestedWords){
        Map<String, Integer> stringFrequencyMap = new HashMap<>();
        for (String str : suggestedWords.stream().map(Word::getWord).toList()) {
            stringFrequencyMap.put(str, stringFrequencyMap.getOrDefault(str, 0) + 1);
        }
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(stringFrequencyMap.entrySet());
        sortedEntries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        List<String> sortedStringList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            for (int i = 0; i < entry.getValue(); i++) {
                sortedStringList.add(entry.getKey());
            }
        }
        return sortedStringList;
    }
    private Set<String> getWords(SearchResponse<Word> response){
        List<Word> suggestedWords = extractItemsFromResponse(response);
        if (suggestedWords.isEmpty()) {
            return new HashSet<>();
        }
        List<String> sortedStringList = sortedList(suggestedWords);
        Set<String> topThreeWords = new HashSet<>();
        for (String word : sortedStringList) {
            if (topThreeWords.size() < 3) {
                topThreeWords.add(word);
            } else {
                break;
            }
        }
        return topThreeWords;
    }
    public Set<String> getSuggestions(String text) throws IOException {
        String word = text.split(" ")[0];
        SearchResponse<Word> response = getSuggestionsFromElasticsearch(word);
        return getWords(response);
    }
}
