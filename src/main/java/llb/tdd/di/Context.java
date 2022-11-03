package llb.tdd.di;

import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: Context
 * @date 2022-11-01 下午7:04
 * @ProjectName 01-di-container
 * @Version V1.0
 */
public interface Context {
    <Type> Optional<Type> get(Class<Type> type);

    Optional get(ParameterizedType type);
}
