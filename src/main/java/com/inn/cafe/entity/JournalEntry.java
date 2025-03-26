package com.inn.cafe.entity;



import com.inn.cafe.enums.Sentiment;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="journal_entry")
@Data
@NoArgsConstructor
public class JournalEntry implements Serializable {
    @Id
    private Long id;
    @NonNull
    private String title;
    private String content;
    private LocalDateTime date;
    private Sentiment sentiment;
}
