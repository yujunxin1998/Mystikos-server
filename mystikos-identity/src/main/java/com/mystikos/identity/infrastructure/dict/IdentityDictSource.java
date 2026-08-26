package com.mystikos.identity.infrastructure.dict;

import com.mystikos.common.dict.DictCatalog;
import com.mystikos.common.dict.DictSource;
import com.mystikos.identity.domain.model.CompanionIdentityApplicationStatus;
import com.mystikos.identity.domain.model.CompanionStatus;
import com.mystikos.identity.domain.model.Gender;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.UserStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/** Identity 上下文贡献进全局字典接口的枚举，见 {@code mystikos-system-operation} 的 DictionaryController。 */
@Component
public class IdentityDictSource implements DictSource {

    @Override
    public List<DictCatalog> dictCatalogs() {
        return List.of(
                DictCatalog.of("ROLE", "角色", Role.values()),
                DictCatalog.of("GENDER", "性别", Gender.values()),
                DictCatalog.of("USER_STATUS", "账号状态", UserStatus.values()),
                DictCatalog.of("COMPANION_STATUS", "陪玩接单状态", CompanionStatus.values()),
                DictCatalog.of("COMPANION_IDENTITY_APPLICATION_STATUS", "陪玩身份申请状态",
                        CompanionIdentityApplicationStatus.values()));
    }
}
