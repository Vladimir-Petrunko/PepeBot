package pepe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pepe.entity.UserSurprise;
import pepe.repository.UserSurpriseRepository;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@SuppressWarnings("all")
public class UserSurpriseService {
    private final UserSurpriseRepository userSurpriseRepository;
    private final UserEventService userEventService;

    @Transactional
    public void deleteOngoingSurprises(Long userId) {
        userSurpriseRepository.deleteOngoingSurprises(userId);
    }

    @Transactional
    public void deleteOngoingSurprises2(Long userId) {
        userSurpriseRepository.deleteOngoingSurprises2(userId);
    }

    public void beginSurprise(Long userId, String userName) {
        UserSurprise userSurprise = new UserSurprise();
        userSurprise.setUserFromId(userId);
        userSurprise.setUserFromName(userName);
        userSurpriseRepository.save(userSurprise);
    }

    public UserSurprise getActiveSurprise(Long userId) {
        return userSurpriseRepository.getActiveSurprise(userId);
    }

    public void save(UserSurprise userSurprise) {
        if (Objects.nonNull(userSurprise.getUserToName()) && Objects.isNull(userSurprise.getUserToId())) {
            userSurprise.setUserToId(userEventService.getUserId(userSurprise.getUserToName()));
        }
        userSurpriseRepository.save(userSurprise);
    }

    public UserSurprise findByMessageId(Integer messageId) {
        return userSurpriseRepository.findByMessageId(messageId);
    }

    @Transactional
    public void clearTrash(Long userId) {
        userSurpriseRepository.clearTrash(userId);
    }

    public List<Integer> findTrash(Long userId) {
        return userSurpriseRepository.findTrash(userId);
    }

    public void markCanReply(Integer messageId) {
        userSurpriseRepository.markCanReply(messageId);
    }
}
