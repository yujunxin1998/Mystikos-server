package com.mystikos.common.level;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 纯函数测试——LevelResolver 是 Membership(VIP)/Relationship(亲密度) 两套阶梯共用的
 * 唯一解析算法，边界情况在这里一次性穷举验证，两边不用各自重复写。
 */
class LevelResolverTest {

    private record Tier(String code, int sortOrder, BigDecimal threshold) implements LevelTier {
        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getDisplayName() {
            return code;
        }

        @Override
        public int getSortOrder() {
            return sortOrder;
        }

        @Override
        public BigDecimal getThreshold() {
            return threshold;
        }
    }

    private final List<Tier> tiers = List.of(
            new Tier("BASE", 1, BigDecimal.ZERO),
            new Tier("MID", 2, BigDecimal.valueOf(100)),
            new Tier("TOP", 3, BigDecimal.valueOf(1000)));

    @ParameterizedTest
    @CsvSource({
            "0,BASE",
            "99,BASE",
            "100,MID",
            "999,MID",
            "1000,TOP",
            "999999,TOP",
    })
    void resolvesHighestSatisfiedTier(String cumulative, String expectedCode) {
        Tier resolved = LevelResolver.resolve(tiers, new BigDecimal(cumulative));
        assertThat(resolved.getCode()).isEqualTo(expectedCode);
    }

    @Test
    void tierOrderInInputDoesNotMatter() {
        List<Tier> shuffled = List.of(tiers.get(2), tiers.get(0), tiers.get(1));
        Tier resolved = LevelResolver.resolve(shuffled, BigDecimal.valueOf(500));
        assertThat(resolved.getCode()).isEqualTo("MID");
    }

    @Test
    void emptyTierListThrows() {
        assertThatThrownBy(() -> LevelResolver.resolve(List.<Tier>of(), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingBaselineTierThrows() {
        List<Tier> noBaseline = List.of(new Tier("MID", 2, BigDecimal.valueOf(100)));
        assertThatThrownBy(() -> LevelResolver.resolve(noBaseline, BigDecimal.ZERO))
                .isInstanceOf(IllegalStateException.class);
    }
}
