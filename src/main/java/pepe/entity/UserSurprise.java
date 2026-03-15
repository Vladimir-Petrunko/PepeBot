package pepe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
@Table(name = "user_surprise")
public class UserSurprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_from_id")
    private Long userFromId;

    @Column(name = "user_from_name")
    private String userFromName;

    @Column(name = "user_to_id")
    private Long userToId;

    @Column(name = "user_to_name")
    private String userToName;

    @Column(name = "message")
    private String message;

    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "reverse_message_id")
    private Integer reverseMessageId;

    @Column(name = "sent")
    private Integer sent;

    @Column(name = "can_reply")
    private Integer canReply;
}
