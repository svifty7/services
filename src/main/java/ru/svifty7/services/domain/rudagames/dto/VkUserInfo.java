package ru.svifty7.services.domain.rudagames.dto;

import com.vk.api.sdk.objects.base.Sex;
import com.vk.api.sdk.objects.users.GetNameCase;
import lombok.Builder;

import java.net.URI;

@Builder
public record VkUserInfo(
        Integer id,
        String firstNameNom,
        String firstNameGen,
        String firstNameDat,
        String firstNameAcc,
        String firstNameIns,
        String firstNameAbl,
        String lastNameNom,
        String lastNameGen,
        String lastNameDat,
        String lastNameAcc,
        String lastNameIns,
        String lastNameAbl,
        Sex sex,
        URI photo200
) {

    public String getFirstName(GetNameCase nameCase) {
        return getNameWithCase(nameCase, firstNameNom, firstNameGen, firstNameDat, firstNameAcc, firstNameIns, firstNameAbl);
    }

    public String getLastName(GetNameCase nameCase) {
        return getNameWithCase(nameCase, lastNameNom, lastNameGen, lastNameDat, lastNameAcc, lastNameIns, lastNameAbl);
    }

    private String getNameWithCase(
            GetNameCase nameCase,
            String nameNom,
            String nameGen,
            String nameDat,
            String nameAcc,
            String nameIns,
            String nameAbl
    ) {
        return switch (nameCase) {
            case NOMINATIVE -> nameNom;
            case GENITIVE -> nameGen;
            case DATIVE -> nameDat;
            case ACCUSATIVE -> nameAcc;
            case INSTRUMENTAL -> nameIns;
            case PREPOSITIONAL -> nameAbl;
        };
    }

    public String getFullName(GetNameCase nameCase) {
        return String.format("%s %s", getFirstName(nameCase), getLastName(nameCase));
    }

    public boolean getIsMale() {
        return sex.equals(Sex.MALE);
    }

    public boolean getIsFemale() {
        return sex.equals(Sex.FEMALE);
    }
}
