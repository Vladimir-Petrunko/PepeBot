package pepe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
@Table(name = "user_event")
public class UserEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "chat_name")
    private String chatName;

    @Column(name = "inner_id")
    private String innerId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "messageLength")
    private Integer messageLength;

    @Column(name = "type")
    private String type;

    @Column(name = "innerType")
    private String innerType;
}
