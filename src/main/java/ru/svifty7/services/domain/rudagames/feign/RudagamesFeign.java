package ru.svifty7.services.domain.rudagames.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.svifty7.services.domain.rudagames.config.RudagamesFeignConfig;
import ru.svifty7.services.domain.rudagames.dto.AcceptedGame;
import ru.svifty7.services.domain.rudagames.dto.CityEvent;

import java.util.List;

@FeignClient(name = "rudagames-feign", url = "${rudagames.api.base-url}", configuration = RudagamesFeignConfig.class)
public interface RudagamesFeign {

    @GetMapping("${rudagames.api.events}/{cityId}")
    List<CityEvent> getEvents(@PathVariable Integer cityId);

    @GetMapping("${rudagames.api.accepted-games}")
    List<AcceptedGame> getAcceptedGames(@RequestParam Integer cityId);

}
