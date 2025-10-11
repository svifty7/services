package ru.svifty7.services.domain.rudagames.service;

import com.vk.api.sdk.objects.users.GetNameCase;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.svifty7.services.domain.rudagames.dto.VkUserInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VkCacheService {

    private final VkService vkService;

    @Cacheable(value = "vkUsers", cacheManager = "vkCacheManager", key = "#userId")
    public VkUserInfo getUserInfo(int userId) {
        log.debug("Fetching user from VK API: userId={}", userId);

        // Делаем 6 запросов параллельно
        List<GetNameCase> cases = Arrays.asList(
                GetNameCase.NOMINATIVE,
                GetNameCase.GENITIVE,
                GetNameCase.DATIVE,
                GetNameCase.ACCUSATIVE,
                GetNameCase.INSTRUMENTAL,
                GetNameCase.PREPOSITIONAL
        );

        Map<GetNameCase, GetResponse> responses = cases.parallelStream()
                .collect(Collectors.toMap(
                        nameCase -> nameCase,
                        nameCase -> {
                            try {
                                return vkService.getUserById(userId, nameCase);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                ));

        GetResponse base = responses.get(GetNameCase.NOMINATIVE);

        return VkUserInfo.builder()
                .id(base.getId())
                .firstNameNom(responses.get(GetNameCase.NOMINATIVE).getFirstName())
                .firstNameGen(responses.get(GetNameCase.GENITIVE).getFirstName())
                .firstNameDat(responses.get(GetNameCase.DATIVE).getFirstName())
                .firstNameAcc(responses.get(GetNameCase.ACCUSATIVE).getFirstName())
                .firstNameIns(responses.get(GetNameCase.INSTRUMENTAL).getFirstName())
                .firstNameAbl(responses.get(GetNameCase.PREPOSITIONAL).getFirstName())
                .lastNameNom(responses.get(GetNameCase.NOMINATIVE).getLastName())
                .lastNameGen(responses.get(GetNameCase.GENITIVE).getLastName())
                .lastNameDat(responses.get(GetNameCase.DATIVE).getLastName())
                .lastNameAcc(responses.get(GetNameCase.ACCUSATIVE).getLastName())
                .lastNameIns(responses.get(GetNameCase.INSTRUMENTAL).getLastName())
                .lastNameAbl(responses.get(GetNameCase.PREPOSITIONAL).getLastName())
                .sex(base.getSex())
                .photo200(base.getPhoto200())
                .build();
    }


    /**
     * Очистить кэш пользователя
     */
    @CacheEvict(value = "vkUsers", cacheManager = "vkCacheManager", key = "#userId")
    public void evictUser(Integer userId) {
        log.info("Evicted user from cache: userId={}", userId);
    }

    /**
     * Очистить весь кэш пользователей
     */
    @CacheEvict(value = "vkUsers", cacheManager = "vkCacheManager", allEntries = true)
    public void evictAllUsers() {
        log.info("Evicted all users from cache");
    }
}
