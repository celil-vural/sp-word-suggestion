package com.spwordsuggestion.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Setter
@Document(indexName = "words_index")
@Setting(settingPath = "static/es-settings.json")
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"word"})
public class Word {
    @Id
    private String uuid;
    @Field(name = "word", type = FieldType.Text, analyzer = "custom_index", searchAnalyzer = "custom_search")
    private String word;
}