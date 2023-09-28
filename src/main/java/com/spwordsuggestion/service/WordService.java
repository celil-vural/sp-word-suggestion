package com.spwordsuggestion.service;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.spwordsuggestion.model.Word;
import com.spwordsuggestion.repository.WordRepository;
import com.spwordsuggestion.util.ESUtil;
import lombok.RequiredArgsConstructor;
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
    public void addText(String text) throws IOException {
        List<Word> words = getWordsByString(text);
        for(Word word:words){
            try{
                Supplier<Query> querySupplier = ESUtil.buildQueryForFieldAndValueWithMatch("word",word.getWord());
                SearchResponse<Word> response=elasticsearchClient.search(q -> q.index("words_index")
                        .query(querySupplier.get()), Word.class);
                List<Word> wordList=extractItemsFromResponse(response);
                Optional<Word> word1=wordList.stream().filter(w-> Objects.equals(w.getWord(), word.getWord())).findFirst();
                if(word1.isPresent()){
                    Integer frequency=word1.get().getFrequency()+1;
                    word1.get().setFrequency(frequency);
                    wordRepository.save(word1.get());
                }
                else{
                    wordRepository.save(word);
                }
            }
            catch (IndexOutOfBoundsException e){
                wordRepository.save(word);
            }
        }
    }
    private List<Word> getWordsByString(String text) {
        text=text.toLowerCase(Locale.of("tr"));
        text=text.replaceAll("[^a-zA-Z0-9ğüşıöçĞÜŞİÖÇ]+", " ");
        String[] words = text.split(" ");
        return Arrays.stream(words).map(
                word->new Word(UUID.randomUUID().toString(),word,1))
                .collect(Collectors.toList());
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
    private List<Word> getWords(SearchResponse<Word> response){
        List<Word> suggestedWords = extractItemsFromResponse(response);
        if (suggestedWords.isEmpty()) {
            return new ArrayList<>();
        }
        suggestedWords.sort(Comparator.comparing(Word::getFrequency).reversed());
        return suggestedWords.subList(0,3);
    }
    public List<Word> getSuggestions(String text) throws IOException {
        String word = text.split(" ")[0];
        SearchResponse<Word> response = getSuggestionsFromElasticsearch(word);
        return getWords(response);
    }
}
