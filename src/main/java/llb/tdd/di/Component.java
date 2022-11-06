package llb.tdd.di;

import java.lang.annotation.Annotation;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: Component
 * @date 2022-11-06 上午8:13
 * @ProjectName di-explained
 * @Version V1.0
 */
public record Component(Class<?> type, Annotation qualifiers) {
}
